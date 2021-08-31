package pl.poznan.put.voip.client.controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.client.services.UserService;
import pl.poznan.put.voip.client.utils.Controller;
import pl.poznan.put.voip.client.utils.User;
import pl.poznan.put.voip.core.session.Session;

public class ContactController implements Controller {
    @FXML
    private Text callingUser;

    @FXML
    private Text userName;

    @FXML
    private Text error;

    @FXML
    private TableView<User> loggedUsers;

    @FXML
    private TextArea receiverName;

    @FXML
    public void initialize() {
        synchronized (Client.getClient()) {
            Session session = Client.getClient().currentSession();
            userName.setText(session.getLogin());
        }
    }

    @FXML
    void changePassword(ActionEvent event) {
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
            case "USERS": {
                handleUsers(args);
                break;
            }
            case "CALL": {
                handleCall(args);
                break;
            }
        }
    }

    private void handleUsers(String... args) {
        if (args.length < 1) return;

        String updateType = args[0];
        switch (updateType) {
            case "INIT": {
                ObservableList<User> userList = loggedUsers.getItems();
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
                loggedUsers.getItems().add(new User(login));

                break;
            }
            case "LEFT": {
                if (args.length != 2) return;

                String login = args[1];
                loggedUsers.getItems().removeIf((user) -> user.getLogin().equals(login));

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

    private void handleCall(String... args) {
        Client.getClient().displayNewWindow("Połączenie przychodzące", "incomingCallView");
    }

    @FXML
    void callControl(ActionEvent event) {
        Client.getClient().displayNewWindow("Łączenie z adresatem", "passwordView");
    }
}
