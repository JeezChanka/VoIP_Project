package pl.poznan.put.voip.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import pl.poznan.put.voip.client.utils.Controller;

import java.awt.*;

public class ContactController implements Controller {
    @FXML
    private Text callingUser;

    @FXML
    private Button connectionButton;

    @FXML
    private TextField userName;

    @FXML
    private Text error;

    @FXML
    private TableView<?> loggedUsers;

    @FXML
    void accept(ActionEvent event) {

    }

    @FXML
    void callControl(ActionEvent event) {

    }

    @FXML
    void ignore(ActionEvent event) {

    }
}
