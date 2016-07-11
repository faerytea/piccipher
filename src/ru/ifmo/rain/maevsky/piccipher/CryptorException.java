package ru.ifmo.rain.maevsky.piccipher;

/**
 * Created by faerytea on 09.07.16.
 */
public class CryptorException extends Exception {
    public CryptorException(String message) {
        super(message);
    }

    public CryptorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptorException(Throwable cause) {
        super(cause);
    }
}
