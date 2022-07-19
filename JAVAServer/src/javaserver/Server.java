package javaserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Server extends Thread
{
    List<ClientConnection> connects = new ArrayList<>(); // здесь хранятся соединения с клиентами
    List<String> usernames = new ArrayList<>(); //здесь хранятся занятые ники
    
    public String serverPassword = "";
    private final int port = 3002;
    private SSLServerSocket serverSocket;
    
    public Server(String serverPassword) throws IOException {
        //-------ЗАПУСК СЕРВЕРА--------------
        System.out.println("Запуск сервера...");
        this.serverPassword = serverPassword;
        if(!serverPassword.equals(""))
        {
            System.out.println("Установлен пароль сервера: " + serverPassword);
        }
        else
        {
            System.out.println("Разрешен доступ без пароля");
        }
        
        SSLServerSocketFactory serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        this.serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port); //создание SSL сокета на порту 3002
        
        String[] supportedProtocols = serverSocket.getEnabledProtocols(); //Вывод поддерживаемых протоколов
        System.out.println("Поддерживаемые протоколы:");
        for(int i = 0; i < supportedProtocols.length; i++)
        {
            System.out.println(supportedProtocols[i]);
        }
        
        if(Arrays.asList(supportedProtocols).contains("TLSv1"))// запрос минимального протокола от сервера.
        {
            System.out.println("JRE поддерживает TLSv1.");
        }
        else
        {
            System.out.println("JRE не поддерживает TLSv1.  Возможны проблемы с совместимостью сервера со старыми версиями Android.");
        }
        serverSocket.setNeedClientAuth(false);
        start();
    }

    @Override
    public void run() {
        System.out.println("Сервер запущен и ожидает соединений");
        System.out.println("");
        while (true) {
            try {
                ClientConnection clientConnection = new ClientConnection((SSLSocket) serverSocket.accept(), this);
                System.out.println("Добавлен: " + clientConnection.toString());
                synchronized(connects) //mutex
                {
                    connects.add(clientConnection); 
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendMessageAll(String msg) {
        //synchronized(connects) //mutex
        //{
            for(ClientConnection client : connects) {
                client.send(msg);
            }
        //}
    }
    
    public boolean sendMessageTo(String send_to, String msg, String from) {
        synchronized(usernames)
        {
            if(!usernames.contains(send_to))
            {
                return false;
            }
        }
        //synchronized(connects) //mutex
        //{
            if(send_to.equals("user")) //запрещённые ники
            {
                return false;
            }
            for(ClientConnection client : connects) {
                if(client.username.equals(send_to))
                {
                    client.send("receive from " + from + " " + msg);
                    return true;
                }
            }
            return false;
        //}
    }
    
    public void removeClientConnection(ClientConnection client) {
        synchronized(connects) //mutex
        {
            connects.remove(client);
        }
    }
    
    public boolean addUsername(String username) {
        synchronized(usernames)
        {
            if(usernames.contains(username))
            {
                return false;
            }
            usernames.add(username);
            return true;
        }
    }
    
    public void removeUsername(String username) {
        synchronized(usernames)
        {
            usernames.remove(username);
        }
    }
}
