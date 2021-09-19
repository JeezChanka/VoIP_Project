package pl.poznan.put.voip.server.nethandlers;

import pl.poznan.put.voip.core.session.Session;
import pl.poznan.put.voip.server.Server;
import pl.poznan.put.voip.server.services.CallService;
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
            session.sendCommand("WINDYTALKS", "ERROR");
            return;
        }

        if (args.length == 1) {
            String rsaKeyEncoded = args[0];
            byte[] rsaKey = Base64.getDecoder().decode(rsaKeyEncoded);

            PublicKey rsaPublicKey = RsaUtils.getPublicKey(rsaKey);

            SecretKey aesKey = AesUtils.generateKey();

            byte[] encryptedAesKey = RsaUtils.encrypt(aesKey.getEncoded(), rsaPublicKey);
            String encodedEncryptedAesKey = Base64.getEncoder().encodeToString(encryptedAesKey);

            session.sendCommand("WINDYTALKS", encodedEncryptedAesKey);
            session.enableEncryption(aesKey);
        }
        else {
            session.sendCommand("WINDYTALKS", "ERROR");
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
                sendUserInit(session);

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

    public void handleRequestCall(String... args) {
        UserService us = Server.getServer().getUserService();
        CallService cs = Server.getServer().getCallService();
        Session session = Server.getServer().currentSession();

        if (!session.isEncryptionEnabled()) {
            session.sendCommand("REQUESTCALL", "ERROR");
            return;
        }

        if (args.length == 2) {
            String login = args[0];
            String portS = args[1];
            int port;

            try {
                port = Integer.parseInt(portS);
            } catch (NumberFormatException e)  {
                session.sendCommand("REQUESTCALL", "ERROR");
                return;
            }

            Session tSession = cs.startCall(login, port);

            if (tSession != null) {
                session.sendCommand("REQUESTCALL", "OK");
                tSession.sendCommand("INCOMINGCALL", session.getLogin());

                for (Session session1 : us) {
                    if(session1 == session || session1 == tSession) continue;

                    session1.sendCommand("USERS", "BUSY", session.getLogin());
                    session1.sendCommand("USERS", "BUSY", tSession.getLogin());
                }
            } else {
                session.sendCommand("REQUESTCALL", "BUSY");
            }
        } else {
            session.sendCommand("REQUESTCALL", "ERROR");
            return;
        }
    }

    public void handleRequestedCallNegate(String... args) {
        UserService us = Server.getServer().getUserService();
        CallService cs = Server.getServer().getCallService();
        Session session = Server.getServer().currentSession();

        if (!session.isEncryptionEnabled()) {
            session.sendCommand("REQUESTEDCALLNEGATE", "ERROR");
            return;
        }

        Session tSession = cs.declineCall();

        if(tSession != null) {
            tSession.sendCommand("INCOMINGCALLNEGATE");
        }
        session.sendCommand("REQUESTEDCALLNEGATE", "OK");

        for (Session session1 : us) {
            if(session1 == session || session1 == tSession) continue;

            session1.sendCommand("USERS", "AVAILABLE", session.getLogin());
            if (tSession != null)
                session1.sendCommand("USERS", "AVAILABLE", tSession.getLogin());
        }
    }

    public void handleDisconnectCall(String... args) {
        UserService us = Server.getServer().getUserService();
        CallService cs = Server.getServer().getCallService();
        Session session = Server.getServer().currentSession();

        if (!session.isEncryptionEnabled()) {
            session.sendCommand("DISCONNECTCALL", "ERROR");
            return;
        }

        sendUserInit(session);

        Session tSession = cs.declineCall();

        if(tSession != null) {
            tSession.sendCommand("DISCONNECTEDCALL");

            sendUserInit(tSession);
            session.sendCommand("USERS", "AVAILABLE", session.getLogin());
            session.sendCommand("USERS", "AVAILABLE", tSession.getLogin());

            for (Session session1 : us) {
                if(session1 == session || session1 == tSession) continue;

                session1.sendCommand("USERS", "AVAILABLE", session.getLogin());
                session1.sendCommand("USERS", "AVAILABLE", tSession.getLogin());
            }
        }

    }

    public void handleIncomingCallAnsw(String... args) {
        UserService us = Server.getServer().getUserService();
        CallService cs = Server.getServer().getCallService();
        Session session = Server.getServer().currentSession();

        if (!session.isEncryptionEnabled()) {
            session.sendCommand("INCOMINGCALLANSW", "ERROR");
            return;
        }

        if (args.length >= 1 && args.length <= 2) {
            String answ = args[0];

            if (answ.equals("ACCEPT") && args.length == 2) {
                String portS = args[1];
                int port;

                try {
                    port = Integer.parseInt(portS);
                } catch (NumberFormatException e)  {
                    session.sendCommand("INCOMINGCALLANSW", "ERROR");
                    return;
                }

                Session tSession = cs.acceptCall(port);

                if(tSession != null) {
                    tSession.sendCommand("REQUESTEDCALLANSW", "ACCEPT");
                }


            } else if (answ.equals("DECLINE")) {
                session.sendCommand("INCOMINGCALLANSW", "OK");
                Session tSession = cs.declineCall();


                if(tSession != null) {
                    tSession.sendCommand("REQUESTEDCALLANSW", "DECLINE");
                }
                for (Session session1 : us) {
                    if(session1 == session || session1 == tSession) continue;

                    session1.sendCommand("USERS", "AVAILABLE", session.getLogin());
                    if (tSession != null)
                        session1.sendCommand("USERS", "AVAILABLE", tSession.getLogin());
                }
            } else {
                session.sendCommand("INCOMINGCALLANSW", "ERROR");
            }
        } else {
            session.sendCommand("INCOMINGCALLANSW", "ERROR");
            return;
        }
    }

    public void sendUserInit(Session s) {
        UserService us = Server.getServer().getUserService();
        CallService cs = Server.getServer().getCallService();//us.getOnlineUsers().keySet()
        List<String> initArgs = new LinkedList<>();
        us.getOnlineUsers().forEach((login, session) -> {
            initArgs.add(login);
            initArgs.add(cs.isBusy(session) ? "Zajęty" : "Dostępny");
        });
        initArgs.add(0, "INIT");

        s.sendCommand("USERS", initArgs.toArray(new String[] {}));
    }
}
