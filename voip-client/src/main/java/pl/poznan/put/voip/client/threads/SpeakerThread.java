package pl.poznan.put.voip.client.threads;

import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.core.session.CallSocketWrapper;
import pl.poznan.put.voip.core.utils.Logs;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class SpeakerThread implements Runnable {
    public static final AudioFormat AUDIO_FORMAT =
            new AudioFormat (8000.0F, 16, 2, true, false);

    private final CallSocketWrapper callSocket;

    private SourceDataLine speaker = null;

    public SpeakerThread(DatagramSocket socket) {
        this.callSocket = new CallSocketWrapper(socket);
    }

    @Override
    public void run() {
        Client client = Client.getClient();

        try (SourceDataLine speaker = AudioSystem.getSourceDataLine(AUDIO_FORMAT)) {
            this.speaker = speaker;
            speaker.open(AUDIO_FORMAT);
            speaker.start();
            clean();

            for (;;) {
                try {
                    DatagramPacket packet = callSocket.receiveUdpData();

                    byte[] data = packet.getData();
                    int dLen = packet.getLength();

                    speaker.write(data, 0, dLen);
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

    public synchronized void clean() {
        speaker.flush();
    }

    public CallSocketWrapper getCallSocket() {
        return callSocket;
    }
}