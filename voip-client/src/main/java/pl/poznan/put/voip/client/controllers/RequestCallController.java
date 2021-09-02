package pl.poznan.put.voip.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.client.utils.Controller;

public class RequestCallController implements Controller {
    @FXML
    private Text userName;

    @FXML
    void cancel(ActionEvent event) {
        Client.getClient().currentSession().sendCommand("REQUESTEDCALLNEGATE");
    }

    @Override
    public void onResponse(String command, String... args) {
        switch (command) {
            case "REQUESTEDCALLNEGATE": {
                handleRequestedCallNegate(args);
                break;
            }
            case "REQUESTEDCALLANSW": {
                handleRequestedCallAnsw(args);
                break;
            }

        }
    }

    private void handleRequestedCallNegate(String... args) {
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

    private void handleRequestedCallAnsw(String... args) {
        if (args.length == 1) {
            String result = args[0];
            switch (result) {
                case "ACCEPT": {

                    Stage stage = (Stage) userName.getScene().getWindow();
                    stage.close();
                }
                case "DECLINE": {
                    Stage stage = (Stage) userName.getScene().getWindow();
                    stage.close();
                }
            }
        }
    }

    public void setUserName(String userName) {
        this.userName.setText(userName);
    }
}
