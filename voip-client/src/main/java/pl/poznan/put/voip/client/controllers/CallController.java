package pl.poznan.put.voip.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.client.utils.Controller;

public class CallController implements Controller {
    @FXML
    private Text username;

    @FXML
    void dcServer(ActionEvent event) {
        Client.getClient().currentSession().sendCommand("DISCONNECTCALL");
        Client.getClient().disconnect();
    }

    @FXML
    void disconnect(ActionEvent event) {
        Client.getClient().currentSession().sendCommand("DISCONNECTCALL");

        Client.getClient().switchTo("contactView");
        Client.getClient().stopMicroThread();
    }

    @FXML
    void mute(ActionEvent event) {
        Client.getClient().setMute(!Client.getClient().isMuted());
    }

    void setUsername(String username) {
        this.username.setText(username);
    }

    @Override
    public void onResponse(String command, String... args) {
        if(command.equals("DISCONNECTEDCALL")) {
            handleDisconnectedCall(args);
        }
    }

    void handleDisconnectedCall(String... args) {
        Client.getClient().switchTo("contactView");
    }
}
