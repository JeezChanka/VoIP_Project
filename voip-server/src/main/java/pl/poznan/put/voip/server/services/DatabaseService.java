package pl.poznan.put.voip.server.services;

import pl.poznan.put.voip.core.utils.CryptUtils;
import pl.poznan.put.voip.server.user.User;

import java.sql.*;

public class DatabaseService {

    private static final String CONN_PATH = "jdbc:sqlite:db.sqlite";

    private Connection conn;
    private PreparedStatement getUserStatement;
    private PreparedStatement createUserStatement;
    private PreparedStatement updateUserStatement;

    public DatabaseService() {

    }

    public void connect() throws Exception {
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection(CONN_PATH);

        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS users ("
                + "login VARCHAR(255) PRIMARY KEY,"
                + "password VARCHAR(255) NOT NULL);";
        Statement createTable = conn.createStatement();
        createTable.execute(sqlCreateTable);
        createTable.close();

        String sqlGetUserById = "SELECT login, password FROM users "
                + "WHERE login LIKE ?;";
        getUserStatement = conn.prepareStatement(sqlGetUserById);

        String sqlCreateUser = "INSERT INTO users (login, password) "
                + "VALUES (?,?);";
        createUserStatement = conn.prepareStatement(sqlCreateUser);

        String sqlUpdateUser = "UPDATE users SET password = ? WHERE login LIKE ?";
        updateUserStatement = conn.prepareStatement(sqlUpdateUser);
    }

    public User getUser(String login) throws SQLException {
        getUserStatement.setString(1, login);
        ResultSet rs = getUserStatement.executeQuery();

        if (!rs.next()) {
            return null;
        }

        return new User(rs.getString("login"), rs.getString("password"));
    }

    public boolean createUser(String login, String password) throws SQLException {
        try {
            User targetUser = getUser(login);

            if (targetUser != null) {
                return false;
            }

            createUserStatement.setString(1, login);
            createUserStatement.setString(2, CryptUtils.hash(password));

            createUserStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateUser(String login, String password) throws SQLException {
        try {
            User targetUser = getUser(login);

            if (targetUser == null) {
                return false;
            }

            updateUserStatement.setString(1, CryptUtils.hash(password));
            updateUserStatement.setString(2, login);

            updateUserStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public void disconnect() throws Exception {
        conn.close();
    }

}
