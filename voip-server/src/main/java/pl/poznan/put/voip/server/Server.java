package pl.poznan.put.voip.server;

import pl.poznan.put.voip.core.commands.CommandHandler;
import pl.poznan.put.voip.core.utils.Logs;
import pl.poznan.put.voip.core.session.Session;
import pl.poznan.put.voip.server.nethandlers.ClientNetHandler;
import pl.poznan.put.voip.server.threads.ClientThread;
import pl.poznan.put.voip.server.services.DatabaseService;
import pl.poznan.put.voip.server.services.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static final Server INSTANCE = new Server();

    public static int PORT = 20250;

    private volatile boolean running = false;
    private ServerSocket serverSocket;

    private Session currentSession;

    private final Map<String, CommandHandler> commands = new HashMap<>();
    private final ClientNetHandler clientNetHandler = new ClientNetHandler();

    private final DatabaseService databaseService = new DatabaseService();
    private final UserService userService = new UserService();

    private Server() {
        commands.put("WINDYTALKS", clientNetHandler::handleConnect);
        commands.put("LOGIN", clientNetHandler::handleLogin);
        commands.put("LOGOUT", clientNetHandler::handleLogout);
        commands.put("REGISTER", clientNetHandler::handleRegister);
        commands.put("CHANGEPASS", clientNetHandler::handleChangePassword);
        commands.put("MESSAGE", clientNetHandler::handleMessage);
        commands.put("KEEPALIVE", clientNetHandler::handleKeepAlive);
        commands.put("REQUESTCALL", clientNetHandler::handleRequestCall);
        commands.put("REQUESTEDCALL", clientNetHandler::handleRequestedCall);
        commands.put("INCOMINGCALLANSW", clientNetHandler::handleIncomingCallAnsw);
    }

    public void start(String[] args) throws Exception {
        if (running) {
            throw new RuntimeException("Server has already started!");
        }

        running = true;

        databaseService.connect();

        try {
            serverSocket = new ServerSocket(PORT);

            Logs.log("Server started");
            while (running) {
                final Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(10000);

                Logs.log("New connection received");
                new Thread(new ClientThread(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        databaseService.disconnect();
    }

    public synchronized void executeCommand(ClientThread thread,
                                            String command, String... args) {
        CommandHandler commandHandler = commands.get(command);
        if (commandHandler != null) {
            Logs.log("Executing command " + command);

            runWithSession(thread.getSession(), () -> {
                commandHandler.handle(args);
            });
        }
        else {
            Logs.log("Command " + command + " not found.");
        }
    }

    public synchronized void stop() {
        if (!running) {
            throw new RuntimeException("Server is not running!");
        }

        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void runWithSession(Session session, Runnable runnable) {
        this.currentSession = session;
        runnable.run();
        this.currentSession = null;
    }

    public boolean isRunning() {
        return running;
    }

    public static Server getServer() {
        return INSTANCE;
    }

    public Session currentSession() {
        return currentSession;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public UserService getUserService() {
        return userService;
    }

}
