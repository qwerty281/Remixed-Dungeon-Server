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
    
    private final int port = 3002;
    private SSLServerSocket serverSocket;

    public Server() throws IOException {
        //-------ЗАПУСК СЕРВЕРА--------------
        System.out.println("Запуск сервера...");
                
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
            System.out.println("JRE поддерживает TLSv1. Возможны проблемы с совместимостью сервера со старыми версиями Android.");
        }
        else
        {
            System.out.println("JRE не поддерживает TLSv1. Используются протоколы по умолчанию.");
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
                connects.add(clientConnection);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendMessageAll(String msg) {
        for(ClientConnection client : connects) {
            client.send(msg);
        }
    }
}
