package pl.poznan.put.voip.core.session;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class CallSocketWrapper {
    private final DatagramSocket socket;
    public static final int BUFFER_SIZE = 1600;


    private byte[] receiveBuffer = new byte [BUFFER_SIZE];

    public CallSocketWrapper(DatagramSocket socket) {
        this.socket = socket;
    }

    public DatagramPacket receiveUdpData() throws IOException {
        DatagramPacket packet = new DatagramPacket(receiveBuffer, BUFFER_SIZE);
        socket.receive(packet);

        return packet;
    }

    public void sendUdpData(DatagramPacket packet) {
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while sending UDP data");

        }
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void close() throws IOException {
        socket.close();
    }

}
