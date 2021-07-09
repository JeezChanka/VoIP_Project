package pl.poznan.put.voip.client.utils;

import javafx.beans.property.SimpleStringProperty;

public class User {
    private final SimpleStringProperty login = new SimpleStringProperty("");

    public User(String login) {
        this.login.set(login);
    }

    public String getLogin() {
        return login.get();
    }

}
