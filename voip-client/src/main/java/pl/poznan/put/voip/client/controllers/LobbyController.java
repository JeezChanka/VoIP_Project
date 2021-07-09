package pl.poznan.put.voip.client.controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.client.services.UserService;
import pl.poznan.put.voip.client.utils.Controller;
import pl.poznan.put.voip.client.utils.User;
import pl.poznan.put.voip.core.session.Session;

public class LobbyController implements Controller {

    @FXML
    public ScrollPane scrollPane;

    @FXML
    private TableView<User> loggedUsersTable;

    @FXML
    private Text currentUsernameField;

    @FXML
    private VBox chatBox;

    @FXML
    private TextArea messageArea;

    @FXML
    public void initialize() {
        synchronized (Client.getClient()) {
            Session session = Client.getClient().currentSession();
            currentUsernameField.setText(session.getLogin());
        }
    }

    @FXML
    void changePass(ActionEvent event) {
        Client.getClient().displayNewWindow("Zmiana hasła", "passwordView");
    }

    @FXML
    void disconnect(ActionEvent event) {
        Client.getClient().disconnect();
    }

    @FXML
    void logout(ActionEvent event) {
        synchronized (Client.getClient()) {
            UserService us = Client.getClient().getUserService();
            us.logout();
        }
    }

    @Override
    public void onResponse(String command, String... args) {
        switch (command) {
            case "LOGOUT": {
                handleLogout(args);
                break;
            }
            case "MESSAGE": {
                handleMessage(args);
                break;
            }
            case "USERS": {
                handleUsers(args);
                break;
            }
        }
    }

    private void handleUsers(String... args) {
        if (args.length < 1) return;

        String updateType = args[0];
        switch (updateType) {
            case "INIT": {
                ObservableList<User> userList = loggedUsersTable.getItems();
                userList.clear();

                for (int i = 1; i < args.length; ++i) {
                    String login = args[i];
                    userList.add(new User(login));
                }
                break;
            }
            case "JOINED": {
                if (args.length != 2) return;

                String login = args[1];
                loggedUsersTable.getItems().add(new User(login));

                break;
            }
            case "LEFT": {
                if (args.length != 2) return;

                String login = args[1];
                loggedUsersTable.getItems().removeIf((user) -> user.getLogin().equals(login));

                break;
            }
        }
    }

    private void handleLogout(String... args) {
        if (args.length == 1) {
            String result = args[0];
            switch (result) {
                case "OK":
                case "NOT_LOGGED_IN": {
                    Client.getClient().currentSession().logout();
                    Client.getClient().switchTo("loginView");
                    break;
                }
                case "ERROR": {
                    Client.getClient().disconnect();
                }
            }
        }
    }

    private void handleMessage(String... args) {
        if (args.length == 2) {
            final String login = args[0];
            final String message = args[1];

            Text loginText = new Text(login);
            loginText.setFill(Color.GREEN);

            TextFlow textFlow = new TextFlow(
                    new Text("["), loginText, new Text("]: "),
                    new Text(message)
            );

            Font font = new Font(16.0D);
            for (Node node : textFlow.getChildren()) {
                ((Text) node).setFont(font);
            }

            ObservableList<Node> nodeList = chatBox.getChildren();

            nodeList.add(textFlow);

            int size = nodeList.size();
            if (size > 100) {
                nodeList.remove(0, size - 100);
            }

            double prevVValue = scrollPane.getVvalue();
            scrollPane.layout();

            if (prevVValue == 1.0D) {
                scrollPane.setVvalue(1.0D);
            }
        }
    }

    @FXML
    void sendMessage(ActionEvent event) {
        synchronized (Client.getClient()) {
            String message = messageArea.getText();

            Client.getClient().currentSession()
                    .sendCommand("MESSAGE", message);
            messageArea.setText("");
        }
    }

}
