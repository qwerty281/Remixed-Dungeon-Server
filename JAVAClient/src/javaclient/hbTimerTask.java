package javaclient;

import java.util.TimerTask;

public class hbTimerTask extends TimerTask {

    private ServerConnection serverConnection;
    public hbTimerTask(ServerConnection serverConnection)
    {
        this.serverConnection = serverConnection;
    }
    
    @Override
    public void run() {
        this.serverConnection.send("connection OK");
    }
}