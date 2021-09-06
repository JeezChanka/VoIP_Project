package pl.poznan.put.voip.client;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.poznan.put.voip.client.threads.CallThread;
import pl.poznan.put.voip.client.threads.KeepAliveThread;
import pl.poznan.put.voip.client.nethandlers.ServerNetHandler;
import pl.poznan.put.voip.client.services.UserService;
import pl.poznan.put.voip.client.threads.MicroThread;
import pl.poznan.put.voip.client.utils.Controller;
import pl.poznan.put.voip.core.commands.CommandHandler;
import pl.poznan.put.voip.core.session.CallSocketWrapper;
import pl.poznan.put.voip.core.session.Session;
import pl.poznan.put.voip.core.utils.Logs;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Client {

    private static Client INSTANCE;

    private final Stage stage;
    private final Scene scene;

    private final Map<String, CommandHandler> commands = new HashMap<>();
    private final ServerNetHandler serverNetHandler = new ServerNetHandler();

    private Session currentSession;


    private CallSocketWrapper callSocket;
    private Controller currentController;
    private Controller currentSubController;

    private Thread keepAliveThread;
    private MicroThread microThread;

    private final UserService userService = new UserService();

    private Client(Stage stage, Scene scene, Controller controller) {
        this.stage = stage;
        this.scene = scene;
        this.currentController = controller;

        commands.put("WINDYTALKS", serverNetHandler::handleConnect);
        commands.put("LOGIN", serverNetHandler::handleLogin);
        commands.put("LOGOUT", serverNetHandler::handleLogout);
        commands.put("REGISTER", serverNetHandler::handleRegister);
        commands.put("CHANGEPASS", serverNetHandler::handleChangePassword);
        commands.put("MESSAGE", serverNetHandler::handleMessage);
        commands.put("USERS", serverNetHandler::handleUsers);
        commands.put("INCOMINGCALL", serverNetHandler::handleIncomingCall);
        commands.put("REQUESTCALL", serverNetHandler::handleRequestCall);
        commands.put("INCOMINGCALLANSW", serverNetHandler::handleIncomingCallAnsw);
        commands.put("REQUESTEDCALLANSW", serverNetHandler::handleRequestedCallAnsw);
        commands.put("INCOMINGCALLNEGATE", serverNetHandler::handleIncomingCallNegate);
        commands.put("REQUESTEDCALLNEGATE", serverNetHandler::handleRequestedCallNegate);
        commands.put("DISCONNECTEDCALL", serverNetHandler::handleDisconnectedCall);
    }

    public static void init(Stage stage, Scene scene, Controller controller) {
        INSTANCE = new Client(stage, scene, controller);
    }

    public synchronized void switchTo(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
            Parent parent = fxmlLoader.load();
            this.currentController = fxmlLoader.getController();
            scene.setRoot(parent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void displayNewWindow(String title, String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
            Parent parent = fxmlLoader.load();
            this.currentSubController = fxmlLoader.getController();
            Scene subScene = new Scene(parent);

            Stage subStage = new Stage();
            subStage.setTitle(title);
            subStage.setResizable(false);
            subStage.setScene(subScene);
            subStage.initOwner(stage);
            subStage.initModality(Modality.WINDOW_MODAL);
            subStage.setOnCloseRequest((event -> Client.this.currentSubController = null));
            subStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void executeCommand(String command, String... args) {
        CommandHandler commandHandler = commands.get(command);

        if (commandHandler != null) {
            Logs.log("Executing command " + command);

            Platform.runLater(() -> {
                synchronized (this) {
                    commandHandler.handle(args);
                }
            });
        }
        else {
            Logs.log("Command " + command + " not found.");
        }
    }

    public synchronized void setCurrentSession(Session session) throws SocketException {
        this.currentSession = session;

        CallThread runnable = new CallThread(new DatagramSocket());
        this.callSocket = runnable.getCallSocket();

        new Thread(runnable).start();
    }

    public Session currentSession() {
        return currentSession;
    }

    public synchronized void handleUDP(DatagramPacket packet) {

    }

    public synchronized void startKeepAliveThread() {
        keepAliveThread = new Thread(new KeepAliveThread());
        keepAliveThread.start();
    }

    public synchronized void startMicroThread() {
        microThread = new MicroThread();
        new Thread(microThread).start();
    }

    public synchronized boolean isMuted() {
        if(microThread == null) {
            return true;
        }
        return microThread.isSending();
    }

    public synchronized void stopMicroThread() {
        if (microThread != null) {
            microThread.stop();
            microThread = null;
        }
    }

    public synchronized void setMute(boolean muted) {
        if (microThread != null) {
            microThread.setSending(!muted);
        }
    }

    public synchronized void disconnect() {
        if (currentSession != null) {
            try {
                currentSession.disconnect();
                currentSession = null;

                callSocket.close();
                callSocket = null;

                switchTo("connectView");
            } catch (IOException ignored) {}
        }
        if (keepAliveThread != null) {
            keepAliveThread.interrupt();
            keepAliveThread = null;
        }
    }

    public UserService getUserService() {
        return userService;
    }

    public Controller currentController() {
        return currentController;
    }

    public Controller currentSubController() {
        return currentSubController;
    }

    public static Client getClient() {
        return INSTANCE;
    }

    public CallSocketWrapper getCallSocket() {
        return callSocket;
    }
}
