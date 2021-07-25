package pl.poznan.put.voip.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.client.services.UserService;
import pl.poznan.put.voip.client.utils.Controller;
import pl.poznan.put.voip.core.utils.Validators;

public class RegisterController implements Controller {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField password1;

    @FXML
    private Text errorPassword;

    @FXML
    private Text errorLogin;

    @FXML
    void register(ActionEvent event) {
        String login = username.getText();
        String pass = password.getText();
        String passwordRepeat = password1.getText();

        if (!Validators.isLoginValid(login)) {
            errorPassword.setText("Login nie spełnia wymagań");
            return;
        }
        else if (!Validators.isPasswordValid(pass)) {
            errorPassword.setText("Hasło nie spełnia wymagań");
            return;
        }
        else if (!pass.equals(passwordRepeat)) {
            errorPassword.setText("Hasła są różne");
            return;
        }
        else {
            errorPassword.setText("");
        }

        synchronized (Client.getClient()) {
            UserService us = Client.getClient().getUserService();
            us.register(login, pass);
        }
    }

    @Override
    public void onResponse(String command, String... args) {
        if (command.equals("REGISTER") && args.length == 1) {
            String result = args[0];
            switch (result) {
                case "OK": {
                    Client.getClient().switchTo("loginView");
                    ((LoginController) Client.getClient().currentController())
                            .getRegisterText()
                            .setText("Zarejestrowano pomyślnie!");
                    break;
                }
                case "ALREADY_EXISTS": {
                    errorLogin.setText("Konto już istnieje.");
                    break;
                }
                case "INVALID_DATA": {
                    errorLogin.setText("Wpisane dane nie spełniają wymagań.");
                    break;
                }
                case "ERROR": {
                    errorLogin.setText("Wystąpił nieoczekiwany błąd.");
                    break;
                }
            }
        }
        else {
            errorLogin.setText("Wystąpił nieoczekiwany błąd.");
        }
    }

    @FXML
    void disconnect(ActionEvent event) {
        Client.getClient().disconnect();
    }

    @FXML
    public void cancel(ActionEvent actionEvent) {
        synchronized (Client.getClient()) {
            Client.getClient().switchTo("loginView");
        }
    }
}
