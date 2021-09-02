package pl.poznan.put.voip.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.client.utils.Controller;

public class IncomingCallController implements Controller {
        @FXML
        private Text userName;

        @FXML
        void accept(ActionEvent event) {
                String port = "24444";//TODO pobrać port z socketa

                Client.getClient().currentSession().sendCommand("INCOMINGCALLANSW", "ACCEPT", port);
                ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
                Client.getClient().switchTo("callView");
        }

        @FXML
        void ignore(ActionEvent event) {
                Client.getClient().currentSession().sendCommand("INCOMINGCALLANSW", "DECLINE");
                ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
        }

        public void setUserName(String userName) {
                this.userName.setText(userName);
        }

        @Override
        public void onResponse(String command, String... args) {
                switch (command) {
                        case "INCOMINGCALLNEGATE": {
                                handleIncomingCallNegate(args);
                                break;
                        }
                        case "INCOMINGCALLANSW": {
                                handleIncomingCallAnsw(args);
                                break;
                        }
                }
        }

        private void handleIncomingCallNegate(String... args) {
                Stage stage = (Stage) userName.getScene().getWindow();
                stage.close();
        }

        private void handleIncomingCallAnsw(String... args) {
                if (args.length == 1) {
                        String result = args[0];
                        switch (result) {
                                case "OK": {
                                        Stage stage = (Stage) userName.getScene().getWindow();
                                        stage.close();
                                }
                                case "ERROR":
                        }
                }
        }
}
