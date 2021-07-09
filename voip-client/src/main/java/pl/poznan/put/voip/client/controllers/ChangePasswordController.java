package pl.poznan.put.voip.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.client.services.UserService;
import pl.poznan.put.voip.client.utils.Controller;
import pl.poznan.put.voip.core.utils.Validators;

public class ChangePasswordController implements Controller {

    @FXML
    private PasswordField oldPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField newPasswordField2;

    @FXML
    private Text errorText;

    @FXML
    void cancelChange(ActionEvent event) {
        ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    void changePassword(ActionEvent event) {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String newPassword2 = newPasswordField2.getText();

        if (!Validators.isPasswordValid(newPassword)) {
            errorText.setFill(Color.RED);
            errorText.setText("Hasło nie spełnia wymagań");
            return;
        }
        else if (!newPassword.equals(newPassword2)) {
            errorText.setFill(Color.RED);
            errorText.setText("Hasła są różne");
            return;
        }
        else {
            errorText.setText("");
        }

        synchronized (Client.getClient()) {
            UserService us = Client.getClient().getUserService();
            us.changePassword(oldPassword, newPassword);
        }
    }

    @Override
    public void onResponse(String command, String... args) {
        if (!command.equals("CHANGEPASS") || args.length != 1) {
            errorText.setFill(Color.RED);
            errorText.setText("Wystąpił nieoczekiwany błąd.");
            return;
        }

        String result = args[0];
        switch (result) {
            case "OK": {
                errorText.setFill(Color.GREEN);
                errorText.setText("Hasło zmieniono pomyślnie");
                oldPasswordField.setText("");
                newPasswordField.setText("");
                newPasswordField2.setText("");
                break;
            }
            case "INVALID_DATA": {
                errorText.setFill(Color.RED);
                errorText.setText("Nieprawidłowe stare hasło.");
                break;
            }
            case "ERROR": {
                errorText.setFill(Color.RED);
                errorText.setText("Wystąpił nieoczekiwany błąd.");
                break;
            }
        }

    }
}