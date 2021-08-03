package javaclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ServerConnection extends Thread
{
    private SSLSocket socket;

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    
    public ServerConnection() throws IOException
    {
        int port = 3002;
        InetAddress addr = InetAddress.getByName("localhost");
        
        SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket l_socket = (SSLSocket) socketFactory.createSocket(addr, port); //создание SSL сокета на порту 3002
        
        this.socket = l_socket;
        
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        start();
    }
    
    @Override
    public void run()
    {
        this.send("connection detected");
        try {
            while (true)
            {
                String serverMessage = (String) objectInputStream.readObject();
                System.out.println("Сервер: " + serverMessage);
            }
        } catch (IOException ex) {} catch (ClassNotFoundException ex) {}
    }

    public void send(String message) {
        try {
            objectOutputStream.writeObject(message);
        } catch (IOException ex) {}
    }
}
