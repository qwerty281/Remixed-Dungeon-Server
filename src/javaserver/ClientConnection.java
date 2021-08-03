package javaserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.net.ssl.SSLSocket;

public class ClientConnection extends Thread
{
    private SSLSocket socket;

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    
    public ClientConnection(SSLSocket socket) throws IOException
    {
        this.socket = socket;
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
                clientMessage = (String) objectInputStream.readObject();
                System.out.println("Клиент: " + clientMessage);
            }
        } catch (IOException ex) {} catch (ClassNotFoundException ex) {}
    }

    public void send(String message) {
        try {
            System.out.println("Отправлено к: " + this.socket.toString());
            System.out.println(message);
            objectOutputStream.writeObject(message);
        } catch (IOException ex) {}
    }
}