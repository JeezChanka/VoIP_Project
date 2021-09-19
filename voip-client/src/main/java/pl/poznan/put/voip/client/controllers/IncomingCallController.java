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
                String port = String.valueOf(Client.getClient().getCallSocket().getSocket().getLocalPort());

                Client.getClient().currentSession().sendCommand("INCOMINGCALLANSW", "ACCEPT", port);
                ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();

                Client.getClient().switchTo("callView");

                CallController callSite = (CallController) Client.getClient().currentController();
                callSite.setUsername(userName.getText());

                Client.getClient().startMicroThread();
        }

        @FXML
        void ignore(ActionEvent event) {
                Client.getClient().currentSession().sendCommand("INCOMINGCALLANSW", "DECLINE");
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
                                        Client.getClient().startMicroThread();
                                }
                                case "ERROR":
                        }
                }
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
        @Override
        public void onClose() {
                Client.getClient().currentSession().sendCommand("INCOMINGCALLANSW", "DECLINE");
        }
}
