package pl.poznan.put.voip.server.threads;

import pl.poznan.put.voip.core.commands.CommandDto;
import pl.poznan.put.voip.core.utils.Logs;
import pl.poznan.put.voip.server.Server;
import pl.poznan.put.voip.core.session.Session;
import pl.poznan.put.voip.server.services.UserService;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ClientThread implements Runnable {

    private final Session session;

    public ClientThread(Socket clientSocket) {
        this.session = new Session(clientSocket);
    }

    @Override
    public void run() {
        while (Server.getServer().isRunning()) {
            try {
                Logs.log("Waiting for commands...");
                List<CommandDto> commands = session.acceptCommands();

                if (commands.isEmpty()) {
                    throw new SocketException("Socket has been closed");
                }

                for (CommandDto command : commands) {
                    Server.getServer().executeCommand(this, command.getCommand(), command.getArgs());
                }
            } catch (IOException ignored) {
                Logs.log("Socket has been closed");

                Server.getServer().runWithSession(session, () -> {
                    UserService us = Server.getServer().getUserService();

                    String currentLogin = Server.getServer().currentSession().getLogin();

                    for (Session otherSessions : us) {
                        if (otherSessions.getLogin().equals(currentLogin)) continue;

                        otherSessions.sendCommand("USERS", "LEFT", currentLogin);
                    }

                    Session tSession = Server.getServer().getCallService().declineCall();

                    if(tSession != null) {
                        tSession.sendCommand("DISCONNECTEDCALL");
                        Server.getServer().getClientNetHandler().sendUserInit(tSession);
                    }

                    Server.getServer().getUserService().logout();
                });

                break;
            }
        }
    }

    public Session getSession() {
        return session;
    }

}
