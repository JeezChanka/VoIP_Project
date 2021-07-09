package pl.poznan.put.voip.server.nethandlers;

import pl.poznan.put.voip.core.session.Session;
import pl.poznan.put.voip.core.utils.*;
import pl.poznan.put.voip.server.Server;
import pl.poznan.put.voip.server.services.UserService;
import pl.poznan.put.voip.core.utils.AesUtils;
import pl.poznan.put.voip.core.utils.RsaUtils;
import pl.poznan.put.voip.core.utils.Validators;

import javax.crypto.SecretKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

public class ClientNetHandler {

    public void handleConnect(String... args) {
        Session session = Server.getServer().currentSession();

        if (session.isEncryptionEnabled()) {
            session.sendCommand("FIRECHAT", "ERROR");
            return;
        }

        if (args.length == 1) {
            String rsaKeyEncoded = args[0];
            byte[] rsaKey = Base64.getDecoder().decode(rsaKeyEncoded);

            PublicKey rsaPublicKey = RsaUtils.getPublicKey(rsaKey);

            SecretKey aesKey = AesUtils.generateKey();

            byte[] encryptedAesKey = RsaUtils.encrypt(aesKey.getEncoded(), rsaPublicKey);
            String encodedEncryptedAesKey = Base64.getEncoder().encodeToString(encryptedAesKey);

            session.sendCommand("FIRECHAT", encodedEncryptedAesKey);
            session.enableEncryption(aesKey);
        }
        else {
            session.sendCommand("FIRECHAT", "ERROR");
        }
    }

    public void handleKeepAlive(String... args) {}

    public void handleLogin(String... args) {
        UserService us = Server.getServer().getUserService();
        Session session = Server.getServer().currentSession();

        if (!session.isEncryptionEnabled()) {
            session.sendCommand("LOGIN", "ERROR");
            return;
        }

        if (args.length == 2) {
            String login = args[0];
            String password = args[1];

            String result = us.login(login, password);
            session.sendCommand("LOGIN", result);

            if (result.equals("OK")) {
                List<String> initArgs = new LinkedList<>(us.getOnlineUsers().keySet());
                initArgs.add(0, "INIT");

                session.sendCommand("USERS", initArgs.toArray(new String[] {}));

                for (Session otherSessions : us) {
                    if (otherSessions.getLogin().equals(login)) continue;

                    otherSessions.sendCommand("USERS", "JOINED", login);
                }
            }
        }
        else {
            session.sendCommand("LOGIN", "ERROR");
        }
    }

    public void handleLogout(String... args) {
        UserService us = Server.getServer().getUserService();
        Session session = Server.getServer().currentSession();

        if (!session.isEncryptionEnabled()) {
            session.sendCommand("LOGOUT", "ERROR");
            return;
        }

        if (args.length == 0) {
            if (!session.isLoggedIn()) {
                session.sendCommand("NOT_LOGGED_IN");
                return;
            }

            String currentLogin = session.getLogin();

            String result = us.logout();
            session.sendCommand("LOGOUT", result);

            if (result.equals("OK")) {
                for (Session otherSessions : us) {
                    if (otherSessions.getLogin().equals(currentLogin)) continue;

                    otherSessions.sendCommand("USERS", "LEFT", currentLogin);
                }
            }
        }
        else {
            session.sendCommand("LOGOUT", "ERROR");
        }
    }

    public void handleMessage(String... args) {
        UserService us = Server.getServer().getUserService();
        Session session = Server.getServer().currentSession();

        if (!session.isEncryptionEnabled()) {
            session.sendCommand("MESSAGE", "ERROR");
            return;
        }

        if (session.isLoggedIn() && args.length == 1) {
            String message = args[0];

            if (message.isEmpty()) {
                return;
            }

            for (Session targetSession : us) {
                targetSession.sendCommand("MESSAGE", session.getLogin(), message);
            }
        }
    }

    public void handleRegister(String... args) {
        UserService us = Server.getServer().getUserService();
        Session session = Server.getServer().currentSession();

        if (!session.isEncryptionEnabled()) {
            session.sendCommand("REGISTER", "ERROR");
            return;
        }

        if (args.length == 2) {
            String login = args[0];
            String password = args[1];

            if (!Validators.isLoginValid(login)) {
                session.sendCommand("REGISTER", "ERROR");
                return;
            }
            else if (!Validators.isPasswordValid(password)) {
                session.sendCommand("REGISTER", "ERROR");
                return;
            }

            String result = us.register(login, password);

            session.sendCommand("REGISTER", result);
        }
        else {
            session.sendCommand("REGISTER", "ERROR");
        }
    }

    public void handleChangePassword(String... args) {
        UserService us = Server.getServer().getUserService();
        Session session = Server.getServer().currentSession();

        if (!session.isEncryptionEnabled()) {
            session.sendCommand("CHANGEPASS", "ERROR");
            return;
        }

        if (args.length == 2) {
            String oldPassword = args[0];
            String newPassword = args[1];

            if (!Validators.isPasswordValid(newPassword)) {
                session.sendCommand("CHANGEPASS", "ERROR");
                return;
            }

            String result = us.changePassword(oldPassword, newPassword);

            session.sendCommand("CHANGEPASS", result);
        }
        else {
            session.sendCommand("CHANGEPASS", "ERROR");
        }
    }

}