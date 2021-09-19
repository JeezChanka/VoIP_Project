package pl.poznan.put.voip.core.utils;

import java.util.regex.Pattern;

public class Validators {
    private static final Pattern LOGIN_REGEX = Pattern.compile("^[\\w\\-]{3,24}$");
    private static final Pattern PASSWORD_REGEX = Pattern.compile("^[\\w\\-!@#$%^&*=+]{6,24}$");

    private static final Pattern BIG_LETTER_REGEX = Pattern.compile("[A-Z]+");
    private static final Pattern SMALL_LETTER_REGEX = Pattern.compile("[a-z]+");
    private static final Pattern NUMBER_REGEX = Pattern.compile("[0-9]+");
    private static final Pattern SPECIAL_REGEX = Pattern.compile("[\\-_!@#$%^&*=+]+");

    public static boolean isLoginValid(String login) {
        return LOGIN_REGEX.matcher(login).matches();
    }

    public static boolean isPasswordValid(String password) {
        return PASSWORD_REGEX.matcher(password).matches()
                && BIG_LETTER_REGEX.matcher(password).find()
                && SMALL_LETTER_REGEX.matcher(password).find()
                && NUMBER_REGEX.matcher(password).find()
                && SPECIAL_REGEX.matcher(password).find();
    }
}
