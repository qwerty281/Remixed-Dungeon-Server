package javaclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ServerConnection extends Thread
{
    private SSLSocket socket;

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    public boolean is_disabled = false;
    private TimerTask hbTimerTask = new hbTimerTask(this);
    private Timer hbTimer = new Timer();
    
    private TimerTask disconnectTimerTask = new DisconnectTimerTask(this); //таймер отвала сервера
    private Timer disconnectTimer = new Timer();
    public long lastHBTime;
    
    public ServerConnection() throws IOException
    {
        int port = 3002;
        InetAddress addr = InetAddress.getByName("qwerty281projects.tk");
        
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
            this.disconnectTimer.schedule(this.disconnectTimerTask, 1000, 527); //установка таймера дисконнекта. Первая проверка через 1 сек, следующие через 0.527 сек
            this.hbTimer.schedule(hbTimerTask, 500, 500);
            while (!this.is_disabled)
            {
                String serverMessage = (String) objectInputStream.readObject();
                serverMessage = serverMessage.trim(); //обрезаем пробелы по краям
                String[] serverMessageCmd = serverMessage.split(" "); //разделение сообщения на команды
                
                if(serverMessageCmd[0].equals("connection") && serverMessageCmd.length == 2) //действия при ключевом слове connection и правильной длине (2)
                {
                    if(serverMessageCmd[1].equals("disabled")) //действие при втором слове disable и ключевом connection
                    {
                        this.stopConnection();
                    }
                    else if(serverMessageCmd[1].equals("OK"))
                    {
                        this.lastHBTime = System.currentTimeMillis() / 1000L;
                    }
                }
                else
                {
                    System.out.println("Сервер: " + serverMessage);
                }
            }
        } catch (IOException ex) {} catch (ClassNotFoundException ex) {}
    }

    public void send(String message)
    {
        try {
            objectOutputStream.writeObject(message);
        } catch (IOException ex) {}
    }
    
    public void stopConnection()
    {
        this.is_disabled = true;
        
        this.disconnectTimer.cancel();
        this.hbTimer.cancel();
        this.disconnectTimer.purge();
        this.hbTimer.purge();
        this.disconnectTimer = null;
        this.hbTimer = null;
        
        System.out.println("Соединение завершено. Нажмите Enter, чтобы выйти.");
    }
}
