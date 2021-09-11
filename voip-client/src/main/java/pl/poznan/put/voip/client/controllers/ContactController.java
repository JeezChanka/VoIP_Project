package pl.poznan.put.voip.client.controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.client.services.UserService;
import pl.poznan.put.voip.client.utils.Controller;
import pl.poznan.put.voip.client.utils.User;
import pl.poznan.put.voip.core.session.Session;
import pl.poznan.put.voip.core.utils.Logs;

import java.lang.reflect.Array;

public class ContactController implements Controller {
    @FXML
    public TableColumn logins;

    @FXML
    private Text callingUser;

    @FXML
    private Text userName;

    public void setError(String error) {
        this.error.setText(error);
    }

    @FXML
    private Text error;

    @FXML
    private TableView<User> loggedUsers;

    public String getReceiverName() {
        return receiverName.getText();
    }

    @FXML
    private TextArea receiverName;

    @FXML
    public void initialize() {
        synchronized (Client.getClient()) {
            Session session = Client.getClient().currentSession();
            userName.setText(session.getLogin());

            logins.setSortType(TableColumn.SortType.ASCENDING);
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

    @FXML
    void callControl(ActionEvent event) {
        String port = String.valueOf(Client.getClient().getCallSocket().getSocket().getLocalPort());

        String receiver = receiverName.getText();
        for (User user : loggedUsers.getItems()) {
            if(user.getLogin().equals(receiver) && !userName.getText().equals(receiver)) {
                Client.getClient().currentSession().sendCommand("REQUESTCALL", receiver, port);
                return;
            }
        }
        error.setText("Zła nazwa użytkownika!");
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
            case "INCOMINGCALL": {
                handleIncomingCall(args);
                break;
            }
            case "REQUESTCALL": {
                handleRequestCall(args);
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
                    String status = args[i + 1];
                    userList.add(new User(login, status));
                    i++;
                }

                break;
            }
            case "JOINED": {
                if (args.length != 2) return;

                String login = args[1];
                loggedUsers.getItems().add(new User(login, "Dostępny"));

                break;
            }
            case "LEFT": {
                if (args.length != 2) return;

                String login = args[1];
                loggedUsers.getItems().removeIf((user) -> user.getLogin().equals(login));

                break;
            }
            case "BUSY": {
                if (args.length != 2) return;

                String login = args[1];
                loggedUsers.getItems().filtered((user) -> user.getLogin().equals(login)).get(0).setStatus("Zajęty");

                break;
            }
            case "AVAILABLE": {
                if (args.length != 2) return;

                String login = args[1];
                Logs.log(login);
                loggedUsers.getItems().filtered((user) -> user.getLogin().equals(login)).get(0).setStatus("Dostępny");

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

    private void handleIncomingCall(String... args) {
        String sender = args[0];

        Client.getClient().displayNewWindow("Połączenie przychodzące", "incomingCallView");
        IncomingCallController incoming = (IncomingCallController) Client.getClient().currentSubController();
        incoming.setUserName(sender);
    }

    private void handleRequestCall(String... args) {
        if (args.length == 1) {
            String result = args[0];
            switch (result) {
                case "OK": {
                    Client.getClient().displayNewWindow("Dzwonienie", "requestCallView");
                    RequestCallController request = (RequestCallController) Client.getClient().currentSubController();
                    request.setUserName(receiverName.getText());
                    break;
                }
                case "BUSY": {
                    error.setText("Użytkownik zajęty!");
                    break;
                }
                case "ERROR": {
                    error.setText("Nieoczekiwany błąd!");
                    break;
                }
            }
        }
    }
}
