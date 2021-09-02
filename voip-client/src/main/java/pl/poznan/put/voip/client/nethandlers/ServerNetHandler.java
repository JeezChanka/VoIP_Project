package pl.poznan.put.voip.client.nethandlers;

import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.client.utils.Controller;

public class ServerNetHandler {

    public void handleConnect(String... args) {
        Client.getClient().currentController().onResponse("WINDYTALKS", args);
    }

    public void handleLogin(String... args) {
        Client.getClient().currentController().onResponse("LOGIN", args);
    }

    public void handleLogout(String... args) {
        Client.getClient().currentController().onResponse("LOGOUT", args);
    }

    public void handleMessage(String... args) {
        Client.getClient().currentController().onResponse("MESSAGE", args);
    }

    public void handleRegister(String... args) {
        Client.getClient().currentController().onResponse("REGISTER", args);
    }

    public void handleChangePassword(String... args) {
        Controller subController = Client.getClient().currentSubController();
        if (subController == null) return;

        subController.onResponse("CHANGEPASS", args);
    }

    public void handleUsers(String... args) {

        Client.getClient().currentController().onResponse("USERS", args);
    }

    public void handleIncomingCall(String... args) {

        Client.getClient().currentController().onResponse("INCOMINGCALL", args);
    }

    public void handleRequestedCallAnsw(String... args) {
        Client.getClient().currentController().onResponse("REQUESTEDCALLANSW", args);
    }

    public void handleIncomingCallNegate(String... args) {
        Client.getClient().currentController().onResponse("INCOMINGCALLNEGATE", args);
    }

}
