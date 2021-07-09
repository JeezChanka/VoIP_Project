package pl.poznan.put.voip.client.threads;

import pl.poznan.put.voip.client.Client;

public class KeepAliveThread implements Runnable {

    @Override
    public void run() {
        try {
            for (;;) {
                Thread.sleep(5000);
                synchronized (Client.getClient()) {
                    Client.getClient().currentSession().sendCommand("KEEPALIVE");
                }
            }
        } catch (InterruptedException ignored) {}
    }

}
