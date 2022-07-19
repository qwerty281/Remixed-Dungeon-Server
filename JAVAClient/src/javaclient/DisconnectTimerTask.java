package javaclient;

import java.util.TimerTask;

public class DisconnectTimerTask extends TimerTask {

    private ServerConnection serverConnection;
    public DisconnectTimerTask(ServerConnection serverConnection)
    {
        this.serverConnection = serverConnection;
    }
    
    @Override
    public void run() {
        long CurrentTime = System.currentTimeMillis() / 1000L;
        if(this.serverConnection.lastHBTime < CurrentTime - 5)
        {
            this.serverConnection.stopConnection();
        }
    }
}