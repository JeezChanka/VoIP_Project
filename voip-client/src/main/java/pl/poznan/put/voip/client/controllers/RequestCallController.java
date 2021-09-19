package pl.poznan.put.voip.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
                    break;
                }
                case "ERROR":
                    break;
            }
        }
    }

    private void handleRequestedCallAnsw(String... args) {
        if (args.length == 1) {
            String result = args[0];

            Stage stage = (Stage) userName.getScene().getWindow();
            stage.close();

            switch (result) {
                case "ACCEPT": {
                    ContactController lobby = (ContactController) Client.getClient().currentController();
                    String user = lobby.getReceiverName();

                    Client.getClient().switchTo("callView");
                    CallController callSite = (CallController) Client.getClient().currentController();
                    callSite.setUsername(user);

                    Client.getClient().startMicroThread();
                    break;
                }
                case "DECLINE": {
                    ContactController contact = (ContactController) Client.getClient().currentController();
                    contact.setError("Adresat odrzucił połączenie");
                    break;
                }
            }
        }
    }

    public void setUserName(String userName) {
        this.userName.setText(userName);
    }

    @Override
    public void onClose() {
        Client.getClient().currentSession().sendCommand("REQUESTEDCALLNEGATE");
    }
}
