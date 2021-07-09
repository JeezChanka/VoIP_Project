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
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField passwordRepeatField;

    @FXML
    private Text errorPassText;

    @FXML
    private Text errorText;

    @FXML
    void register(ActionEvent event) {
        String login = loginField.getText();
        String password = passwordField.getText();
        String passwordRepeat = passwordRepeatField.getText();

        if (!Validators.isLoginValid(login)) {
            errorPassText.setText("Login nie spełnia wymagań");
            return;
        }
        else if (!Validators.isPasswordValid(password)) {
            errorPassText.setText("Hasło nie spełnia wymagań");
            return;
        }
        else if (!password.equals(passwordRepeat)) {
            errorPassText.setText("Hasła są różne");
            return;
        }
        else {
            errorPassText.setText("");
        }

        synchronized (Client.getClient()) {
            UserService us = Client.getClient().getUserService();
            us.register(login, password);
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
                    errorText.setText("Konto już istnieje.");
                    break;
                }
                case "INVALID_DATA": {
                    errorText.setText("Wpisane dane nie spełniają wymagań.");
                    break;
                }
                case "ERROR": {
                    errorText.setText("Wystąpił nieoczekiwany błąd.");
                    break;
                }
            }
        }
        else {
            errorText.setText("Wystąpił nieoczekiwany błąd.");
        }
    }

    @FXML
    void disconnect(ActionEvent event) {
        Client.getClient().disconnect();
    }

    @FXML
    public void goBack(ActionEvent actionEvent) {
        synchronized (Client.getClient()) {
            Client.getClient().switchTo("loginView");
        }
    }
}
