package javaserver;

import java.util.TimerTask;

public class DisconnectTimerTask extends TimerTask {

    private ClientConnection clientConnection;
    public DisconnectTimerTask(ClientConnection clientConnection)
    {
        this.clientConnection = clientConnection;
    }
    
    @Override
    public void run() {
        long CurrentTime = System.currentTimeMillis() / 1000L;
        if(this.clientConnection.lastHBTime < CurrentTime - 5)
        {
            this.clientConnection.stopConnection();
        }
    }
}