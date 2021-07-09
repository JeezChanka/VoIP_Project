package pl.poznan.put.voip.core.session;

import pl.poznan.put.voip.core.commands.CommandDto;
import pl.poznan.put.voip.core.utils.AesUtils;
import pl.poznan.put.voip.core.utils.CryptUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SocketWrapper {

    private static final String START_TEXT = " \u0002";
    private static final String END_TEXT = "\u0003";
    private static final String DELIMITER = END_TEXT + START_TEXT;
    private static final String CMD_END = "\r\n";

    private final Socket socket;
    private static final int BUFFER_SIZE = 128;
    private final char[] buffer = new char[BUFFER_SIZE];

    private boolean encryptionEnabled = false;
    private SecretKey aesKey = null;

    public SocketWrapper(Socket socket) {
        this.socket = socket;
    }

    private List<String> acceptCommandsRaw() throws IOException {
        List<String> commands = new LinkedList<>();
        StringBuilder builder = new StringBuilder();


        InputStream inputStream = socket.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

        int n;
        while ((n = reader.read(buffer)) != -1) {
            String data = new String(buffer, 0, n);
            int cmdEnd;
            while ((cmdEnd = data.indexOf(CMD_END)) != -1) {
                builder.append(data, 0, cmdEnd);
                data = data.substring(cmdEnd + CMD_END.length());

                String result = builder.toString();
                commands.add(result);

                builder.setLength(0);
            }

            if (!data.isEmpty()) {
                builder.append(data);
            }
            else {
                break;
            }
        }

        return commands;
    }

    public List<CommandDto> acceptCommands() throws IOException {
        return acceptCommandsRaw().stream()
                .map(fullCommand -> {
                    if (encryptionEnabled) {
                        String[] parts = fullCommand.split(" ");
                        String encodedIV = parts[0];
                        String encodedCmd = parts[1];

                        byte[] rawIV = Base64.getDecoder().decode(encodedIV);
                        IvParameterSpec iv = new IvParameterSpec(rawIV);

                        byte[] encryptedCmd = Base64.getDecoder().decode(encodedCmd);
                        byte[] cmd = AesUtils.decrypt(encryptedCmd, aesKey, iv);
                        fullCommand = new String(cmd, StandardCharsets.UTF_8);
                    }

                    int argsStart = fullCommand.indexOf(START_TEXT);
                    if (argsStart != -1) {
                        String command = fullCommand.substring(0, argsStart);
                        String args = fullCommand.substring(
                                argsStart + START_TEXT.length(),
                                fullCommand.length() - END_TEXT.length()
                        );

                        return new CommandDto(command, args.split(DELIMITER, -1));
                    }
                    else {
                        return new CommandDto(fullCommand);
                    }
                })
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private void sendCommandRaw(String cmd) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

            if (encryptionEnabled) {
                IvParameterSpec iv = CryptUtils.generateIV();
                byte[] rawIV = iv.getIV();
                String encodedIV = Base64.getEncoder().encodeToString(rawIV);

                byte[] rawCmd = cmd.getBytes(StandardCharsets.UTF_8);
                byte[] encryptedCmd = AesUtils.encrypt(rawCmd, aesKey, iv);
                String encodedEncryptedCmd = Base64.getEncoder().encodeToString(encryptedCmd);

                writer.append(encodedIV).append(' ').append(encodedEncryptedCmd)
                        .append(CMD_END).flush();
            }
            else {
                writer.append(cmd).append(CMD_END).flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while writing command");
        }
    }

    public void sendCommand(String command, String... args) {
        if (args.length == 0) {
            sendCommandRaw(command);
            return;
        }

        sendCommandRaw(command
                + START_TEXT +
                String.join(DELIMITER, args)
                + END_TEXT);
    }

    public void enableEncryption(SecretKey aesKey) {
        this.aesKey = aesKey;
        this.encryptionEnabled = true;
    }

    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    public void close() throws IOException {
        socket.close();
    }

    public Socket getSocket() {
        return socket;
    }

}
