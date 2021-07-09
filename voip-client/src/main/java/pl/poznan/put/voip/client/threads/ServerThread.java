package pl.poznan.put.voip.client.threads;

import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.core.session.Session;
import pl.poznan.put.voip.core.commands.CommandDto;
import pl.poznan.put.voip.core.utils.Logs;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ServerThread implements Runnable {

    private final Session session;

    public ServerThread(Socket clientSocket) {
        this.session = new Session(clientSocket);
    }

    @Override
    public void run() {
        Client client = Client.getClient();

        for (;;) {
            try {
                Logs.log("Waiting for commands...");
                List<CommandDto> commands = session.acceptCommands();

                if (commands.isEmpty()) {
                    throw new SocketException("Socket has been closed");
                }

                for (CommandDto command : commands) {
                    client.executeCommand(command.getCommand(), command.getArgs());
                }
            } catch (IOException ignored) {
                Logs.log("Socket has been closed");
                client.disconnect();
                break;
            }
        }
    }

    public Session getSession() {
        return session;
    }

}
