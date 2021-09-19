package pl.poznan.put.voip.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.client.services.UserService;
import pl.poznan.put.voip.client.utils.Controller;

public class LoginController implements Controller {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Text error;

    private String attemptLogin = null;

    @FXML
    void disconnect(ActionEvent event) {
        Client.getClient().disconnect();
    }

    @FXML
    void login(ActionEvent event) {
        String login = username.getText();
        String pass = password.getText();

        synchronized (Client.getClient()) {
            UserService us = Client.getClient().getUserService();
            attemptLogin = login;
            us.login(login, pass);
        }
    }

    @Override
    public void onResponse(String command, String... args) {
        if (command.equals("LOGIN") && args.length == 1) {
            String result = args[0];
            switch (result) {
                case "OK": {
                    Client.getClient().currentSession().login(attemptLogin);
                    Client.getClient().switchTo("contactView");
                    break;
                }
                case "ALREADY_LOGGED_IN": {
                    error.setText("Konto jest obecnie zalogowane na serwerze.");
                    break;
                }
                case "INVALID_DATA": {
                    error.setText("Błędne dane logowania.");
                    break;
                }
                case "ERROR": {
                    error.setText("Wystąpił nieoczekiwany błąd.");
                    break;
                }
            }
        }
        else {
            error.setText("Wystąpił nieoczekiwany błąd.");
        }
    }

    @FXML
    void register(ActionEvent event) {
        Client.getClient().switchTo("registerView");
    }

    public Text getRegisterText() {
        return error;
    }
}
