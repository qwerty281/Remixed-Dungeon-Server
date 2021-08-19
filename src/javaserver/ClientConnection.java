package javaserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.ssl.SSLSocket;


public class ClientConnection extends Thread
{
    private SSLSocket socket;
    private Server server;
    public boolean is_disabled = false; //для завершения потока
    public String username = "user"; //имя пользователя
    public long lastHBTime; //время последнего проверочного сообщения от пользователя
    private long lastSendTime = 0; //время последней отправки предмета от пользователя
    
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    
    private TimerTask disconnectTimerTask = new DisconnectTimerTask(this); //таймер отвала клиента
    private Timer disconnectTimer = new Timer();
    
    private TimerTask hbTimerTask = new hbTimerTask(this); //таймер сообщений клиенту
    private Timer hbTimer = new Timer();
    
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
            
            this.disconnectTimer.schedule(this.disconnectTimerTask, 1000, 527); //установка таймера дисконнекта. Первая проверка через 1 сек, следующие через 0.527 сек
            this.hbTimer.schedule(hbTimerTask, 500, 500); //установка таймера проверочных сообщений. Отправка "connection OK" раз в 0.5 сек
            
            while (!this.is_disabled) //если клиент не отключен, получаем сообщения
            {
                clientMessage = (String) objectInputStream.readObject(); //получаем строку от клиента
                clientMessage = clientMessage.trim(); //обрезаем пробелы по краям
                String[] clientMessageCmd = clientMessage.split(" "); //разделение сообщения на команды
                
                //-----Обработка команд-----
                if(clientMessageCmd[0].equals("connection") && clientMessageCmd.length == 2) //действия при ключевом слове connection и правильной длине (2)
                {
                    if(clientMessageCmd[1].equals("disabled")) //действие при втором слове disable и ключевом connection
                    {
                        this.stopConnection();
                    }
                    else if(clientMessageCmd[1].equals("OK"))
                    {
                        this.lastHBTime = System.currentTimeMillis() / 1000L;
                    }
                }
                else if(clientMessageCmd[0].equals("username") && clientMessageCmd.length == 3) //действия при ключевом слове username и правильной длине (3)
                {
                    if(clientMessageCmd[1].equals("change")) //действие при втором слове change и ключевом username
                    {
                        if(clientMessageCmd[2].equals("user"))
                        {
                            this.send("username change error");
                        }
                        else if(this.server.addUsername(clientMessageCmd[2]))
                        {
                            this.server.removeUsername(this.username);
                            this.username = clientMessageCmd[2];
                            this.send("username change OK");
                        }
                        else
                        {
                            this.send("username change exists");
                        }
                    }
                }
                else if(clientMessageCmd[0].equals("send") && clientMessageCmd.length > 3)
                {
                    if(clientMessageCmd[1].equals("to"))
                    {
                        long CurrentTime = System.currentTimeMillis();
                        if(this.lastSendTime <= CurrentTime - 500)
                        {
                            this.lastSendTime = CurrentTime;
                            if(!server.sendMessageTo(clientMessageCmd[2], clientMessage.substring(clientMessageCmd[0].length() + clientMessageCmd[1].length() + clientMessageCmd[2].length() + 3), this.username))
                            {
                                this.send("send error"); //если отправлять некуда
                            }
                            else
                            {
                                this.send("send OK");
                            }
                        }
                        else
                        {
                            this.send("send error"); //если 500 мс с прошлой отправки не прошло
                        }
                    }
                }
                else if(clientMessageCmd[0].equals("chat"))
                {
                    if(clientMessageCmd[1].equals("to"))
                    {
                        this.send("chat error");
                        //server.sendMessageTo(clientMessageCmd[2], "чат");
                    }
                }
                else if(clientMessageCmd[0].equals("auth")) //авторизация пока не поддерживается
                {
                    this.send("auth failed");
                }
                else
                {
                    this.send("command error");
                }
            }
        } catch (IOException ex) {} catch (ClassNotFoundException ex) {}
    }

    public void send(String message)
    {
        synchronized(objectOutputStream)
        {
            try {
                System.out.println("Отправлено к: " + this.socket.toString());
                System.out.println(message);
                objectOutputStream.writeObject(message);
            } catch (IOException ex) {}
        }
    }
    
    public void stopConnection()
    {
        if(!this.is_disabled)
        {
            this.is_disabled = true;
            this.disconnectTimer.cancel();
            this.hbTimer.cancel();
            this.disconnectTimer.purge();
            this.hbTimer.purge();
            this.disconnectTimer = null;
            this.hbTimer = null;
            
            System.out.println("Соединение завершено: " + this.socket.toString());
            this.send("connection disabled");
            server.removeClientConnection(this);
            server.removeUsername(this.username);
        }
    }
}