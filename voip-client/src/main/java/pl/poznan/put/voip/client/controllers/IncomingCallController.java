package pl.poznan.put.voip.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pl.poznan.put.voip.client.utils.Controller;

public class IncomingCallController implements Controller {
        @FXML
        private Text userName;

        @FXML
        void accept(ActionEvent event) {

        }

        @FXML
        void ignore(ActionEvent event) {
                //TODO wysłanie powiadomienia o nieodebraniu połączenia
                ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
        }
}
