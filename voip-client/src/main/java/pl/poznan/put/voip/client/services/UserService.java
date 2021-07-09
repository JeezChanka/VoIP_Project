package pl.poznan.put.voip.client.services;

import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.core.session.Session;

public class UserService {

    public void login(String login, String password) {
        Session session = Client.getClient().currentSession();
        session.sendCommand("LOGIN", login, password);
    }

    public void register(String login, String password) {
        Session session = Client.getClient().currentSession();
        session.sendCommand("REGISTER", login, password);
    }

    public void changePassword(String oldPassword, String newPassword) {
        Session session = Client.getClient().currentSession();
        session.sendCommand("CHANGEPASS", oldPassword, newPassword);
    }

    public void logout() {
        Session session = Client.getClient().currentSession();
        session.sendCommand("LOGOUT");
    }

}
