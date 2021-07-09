package pl.poznan.put.voip.server.services;

import pl.poznan.put.voip.core.session.Session;
import pl.poznan.put.voip.core.utils.CryptUtils;
import pl.poznan.put.voip.server.Server;
import pl.poznan.put.voip.server.user.User;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserService implements Iterable<Session> {

    private final Map<String, Session> onlineUsers = new HashMap<>();

    public String login(String login, String password) {
        DatabaseService ds = Server.getServer().getDatabaseService();

        if (isLoggedIn(login)) {
            return "ALREADY_LOGGED_IN";
        }

        User targetUser;
        try {
            targetUser = ds.getUser(login);
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR";
        }

        String hashedPassword = CryptUtils.hash(password);

        if (targetUser != null
                && targetUser.getPassword().equals(hashedPassword)) {
            Session session = Server.getServer().currentSession();

            session.login(login);
            onlineUsers.put(login, session);

            return "OK";
        }
        else {
            return "INVALID_DATA";
        }
    }

    public boolean isLoggedIn(String login) {
        return onlineUsers.containsKey(login);
    }

    public String register(String login, String password) {
        DatabaseService ds = Server.getServer().getDatabaseService();

        try {
            boolean result = ds.createUser(login, password);
            return result ? "OK" : "ALREADY_EXISTS";
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    public String changePassword(String oldPassword, String newPassword) {
        Session session = Server.getServer().currentSession();

        if (!session.isLoggedIn()) {
            return "ERROR";
        }

        DatabaseService ds = Server.getServer().getDatabaseService();

        User targetUser;
        try {
            targetUser = ds.getUser(session.getLogin());
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR";
        }

        String hashedOldPassword = CryptUtils.hash(oldPassword);
        if (!targetUser.getPassword().equals(hashedOldPassword)) {
            return "INVALID_DATA";
        }

        try {
            boolean succeed = ds.updateUser(targetUser.getLogin(), newPassword);
            return succeed ? "OK" : "ERROR";
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    public String logout() {
        Session session = Server.getServer().currentSession();

        if (session == null || !isLoggedIn(session.getLogin())) {
            return "NOT_LOGGED_IN";
        }

        onlineUsers.remove(session.getLogin());
        session.logout();

        return "OK";
    }

    public Map<String, Session> getOnlineUsers() {
        return onlineUsers;
    }

    @Override
    public Iterator<Session> iterator() {
        return onlineUsers.values().iterator();
    }
}
