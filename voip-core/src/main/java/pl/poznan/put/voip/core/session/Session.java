package pl.poznan.put.voip.core.session;

import pl.poznan.put.voip.core.commands.CommandDto;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class Session {

    private String login = null;

    public SocketWrapper getSocket() {
        return socket;
    }

    private final SocketWrapper socket;

    public Session(Socket socket) {
        this.socket = new SocketWrapper(socket);
    }

    public String getLogin() {
        return login;
    }

    public void login(String login) {
        this.login = login;
    }

    public boolean isLoggedIn() {
        return login != null;
    }

    public void logout() {
        this.login = null;
    }

    public boolean isEncryptionEnabled() {
        return socket.isEncryptionEnabled();
    }

    public void enableEncryption(SecretKey key) {
        socket.enableEncryption(key);
    }

    public void disconnect() throws IOException {
        socket.close();
    }

    public void sendCommand(String command, String... args) {
        socket.sendCommand(command, args);
    }

    public List<CommandDto> acceptCommands() throws IOException {
        return socket.acceptCommands();
    }

}
