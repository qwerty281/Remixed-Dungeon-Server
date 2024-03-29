package javaserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.*;
import java.util.Base64;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Server extends Thread
{
    List<ClientConnection> connects = new ArrayList<>(); // здесь хранятся соединения с клиентами
    List<String> usernames = new ArrayList<>(); //здесь хранятся занятые ники
    
    public String serverPassword = "";
    public boolean print_errors = false;
    public Pattern base64pattern = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");
    private final int port = 3002;
    private SSLServerSocket serverSocket;
    
    public Server(String serverPassword, boolean print_errors) throws IOException {
        //-------ЗАПУСК СЕРВЕРА--------------
        System.out.println("Запуск сервера...");
        this.print_errors = print_errors;
        this.serverPassword = serverPassword;
        if(!serverPassword.equals(""))
        {
            this.serverPassword = new String(Base64.getEncoder().encode(serverPassword.getBytes()));
            System.out.println("Установлен пароль сервера: " + serverPassword);
            System.out.println("Base64: " + this.serverPassword);
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
                synchronized(connects) //mutex
                {
                    connects.add(clientConnection); 
                }
            } catch (IOException ex) {
                if(this.print_errors)
                {
                    ex.printStackTrace();
                }
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
    
    public boolean sendMessageTo(String send_to, String prefix, String msg, String from) {
        synchronized(usernames)
        {
            if(!usernames.contains(send_to))
            {
                return false;
            }
        }
        if(send_to.equals("user") || send_to.equals(from)) //запрещённые ники
        {
            return false;
        }
        for(ClientConnection client : connects) {
            if(client.username.equals(send_to))
            {
                long CurrentTime = System.currentTimeMillis();
                if(client.lastRecieveTime.get() <= CurrentTime - 2800)
                {
                    client.send(prefix + from + " " + msg);
                    client.lastRecieveTime.set(CurrentTime);
                    return true;
                }
                
            }
        }
        return false;
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
