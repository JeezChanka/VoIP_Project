package pl.poznan.put.voip.client.threads;

import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.core.session.CallSocketWrapper;
import pl.poznan.put.voip.core.session.Session;
import pl.poznan.put.voip.core.utils.Logs;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;


public class MicroThread implements Runnable {
    private byte[] sendBuffer = new byte [CallSocketWrapper.BUFFER_SIZE];

    private volatile boolean listening = true;

    private volatile boolean sending = true;

    @Override
    public void run() {
        Client client = Client.getClient();

        try (TargetDataLine micro = AudioSystem.getTargetDataLine(CallThread.AUDIO_FORMAT)) {
            micro.open(CallThread.AUDIO_FORMAT);
            micro.start();

            while (listening) {
                try {
                    micro.read(sendBuffer,0, CallSocketWrapper.BUFFER_SIZE);

                    if(!sending) continue;

                    Session session = Client.getClient().currentSession();

                    if(session == null) break;

                    DatagramPacket data = new DatagramPacket
                            (sendBuffer, 0, CallSocketWrapper.BUFFER_SIZE,
                                    Client.getClient().currentSession().getSocket().getSocket().getInetAddress(), 24444);

                    Client.getClient().getCallSocket().getSocket().send(data);
                } catch (IOException ignored) {
                    Logs.log("Socket has been closed");
                    client.disconnect();
                    break;
                }
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        listening = false;
    }

    public void setSending(boolean isSending) {
        sending = isSending;
    }

    public boolean isSending(){
        return sending;
    }
}