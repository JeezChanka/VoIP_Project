package pl.poznan.put.voip.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import pl.poznan.put.voip.client.Client;
import pl.poznan.put.voip.client.threads.ServerThread;
import pl.poznan.put.voip.client.utils.Controller;
import pl.poznan.put.voip.core.session.Session;
import pl.poznan.put.voip.core.utils.AesUtils;
import pl.poznan.put.voip.core.utils.RsaUtils;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;

public class ConnectController implements Controller {

    @FXML
    private TextField ip;

    @FXML
    private TextField port;

    @FXML
    private Text error;

    private KeyPair keyPair = null;

    @FXML
    void connect(ActionEvent event) {
        String ipText = ip.getText();
        String portText = port.getText();

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            error.setText("Nieprawidłowy port");
            return;
        }

        if (port < 1 || port > 65535) {
            error.setText("Nieprawidłowy port");
            return;
        }

        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ipText, port), 4000);

            ServerThread serverThread = new ServerThread(socket);
            Session session = serverThread.getSession();

            Client client = Client.getClient();
            client.setCurrentSession(session);
            new Thread(serverThread).start();

            this.keyPair = RsaUtils.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();

            String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            session.sendCommand("WINDYTALKS", encodedPublicKey);
        } catch (SocketTimeoutException e) {
            error.setText("Serwer nieosiągalny");
        } catch (IOException e) {
            error.setText("Błąd przy próbie połączenia z serwerem.");
        }
    }

    @Override
    public void onResponse(String command, String... args) {
        if (command.equals("WINDYTALKS")) {
            if (args.length != 1 || args[0].equals("ERROR")) {
                error.setText("Nieznany błąd przy próbie połączenia z serwerem.");
                return;
            }

            String encodedEncryptedAesKey = args[0];

            byte[] encryptedAesKey = Base64.getDecoder().decode(encodedEncryptedAesKey);

            byte[] aesKeyRaw = RsaUtils.decrypt(encryptedAesKey, keyPair.getPrivate());
            SecretKey aesKey = AesUtils.getSecretKey(aesKeyRaw);

            Client client = Client.getClient();
            Session session = client.currentSession();

            session.enableEncryption(aesKey);
            client.startKeepAliveThread();

            client.switchTo("loginView");
        }
    }
}
