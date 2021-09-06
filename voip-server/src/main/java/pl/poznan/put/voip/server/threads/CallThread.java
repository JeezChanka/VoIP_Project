package pl.poznan.put.voip.server.threads;

import pl.poznan.put.voip.core.commands.CommandDto;
import pl.poznan.put.voip.core.session.CallSocketWrapper;
import pl.poznan.put.voip.core.session.Session;
import pl.poznan.put.voip.core.utils.Logs;
import pl.poznan.put.voip.server.Server;
import pl.poznan.put.voip.server.services.UserService;

import java.io.IOException;
import java.net.*;
import java.util.List;

public class CallThread implements Runnable {
    private final static int PORT = 24444;
    private CallSocketWrapper socket;

    public CallThread() {
        try {
            this.socket = new CallSocketWrapper(new DatagramSocket(PORT));
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (Server.getServer().isRunning()) {
            try {
                DatagramPacket received = socket.receiveUdpData();

                Server.getServer().handleUDP(received);
            } catch (IOException ignored) {
                Logs.log("Socket has been closed");
                break;
            }
        }
    }

    public CallSocketWrapper getSocket() {
        return socket;
    }
}
