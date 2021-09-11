package pl.poznan.put.voip.client.utils;

import javafx.beans.property.SimpleStringProperty;

public class User {
    private final SimpleStringProperty login = new SimpleStringProperty("");
    private final SimpleStringProperty status = new SimpleStringProperty("");

    public User(String login, String status) {
        this.login.set(login);
        this.status.set(status);
    }

    public String getStatus() {
        return status.get();
    }

    public String getLogin() {
        return login.get();
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public SimpleStringProperty loginProperty() {
        return login;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }
}
