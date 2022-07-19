package javaserver;

import java.util.TimerTask;

public class hbTimerTask extends TimerTask {

    private ClientConnection clientConnection;
    public hbTimerTask(ClientConnection clientConnection)
    {
        this.clientConnection = clientConnection;
    }
    
    @Override
    public void run() {
        this.clientConnection.send("connection OK");
    }
}