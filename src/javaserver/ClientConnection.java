package javaserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.net.ssl.SSLSocket;

public class ClientConnection extends Thread
{
    private SSLSocket socket;
    private Server server;
    public boolean is_disabled = false;
    
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    
    public ClientConnection(SSLSocket socket, Server server) throws IOException
    {
        this.socket = socket;
        this.server = server;
        objectInputStream = new ObjectInputStream(this.socket.getInputStream());
        objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
        System.out.println(this.socket.toString());
        start();
    }

    @Override
    public void run()
    {
        try {
        String clientMessage;
        this.send("connection started");
        clientMessage = (String) objectInputStream.readObject();
        if(clientMessage.equals("connection detected"))
        {
            System.out.println("Клиент: соединение удалось");
        }
            while (true)
            {
                if(this.is_disabled)
                {
                    break;
                }
                clientMessage = (String) objectInputStream.readObject();
                if(clientMessage.equals("connection disable"))
                {
                    this.stopConnection();
                }
                else
                {
                    System.out.println("Клиент: " + clientMessage);
                    server.sendMessageAll(clientMessage);
                }
            }
        } catch (IOException ex) {} catch (ClassNotFoundException ex) {}
    }

    public void send(String message)
    {
        try {
            System.out.println("Отправлено к: " + this.socket.toString());
            System.out.println(message);
            objectOutputStream.writeObject(message);
        } catch (IOException ex) {}
    }
    
    public void stopConnection()
    {
        this.is_disabled = true;
        System.out.println("Соединение завершено: " + this.socket.toString());
        server.removeClientConnection(this);
    }
}