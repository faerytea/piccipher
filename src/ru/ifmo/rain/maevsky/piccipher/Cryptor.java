package ru.ifmo.rain.maevsky.piccipher;

/**
 * Created by faerytea on 08.07.16.
 */
public interface Cryptor {
    /**
     * Injects {@code text} to {@code array}.
     * Note that array will be spoiled.
     * @param array data that will carry text
     * @param text what will be injected
     */
    void encrypt(int[] array, final byte[] text) throws CryptorException;

    /**
     * Extracts text from {@code array}.
     * Note that array will not be returned to original state.
     * @param array data with injection
     * @return extracted injection
     */
    byte[] decrypt(final int[] array) throws CryptorException;
}
