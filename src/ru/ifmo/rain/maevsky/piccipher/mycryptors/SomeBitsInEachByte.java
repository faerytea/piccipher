package ru.ifmo.rain.maevsky.piccipher.mycryptors;

import ru.ifmo.rain.maevsky.piccipher.Cryptor;
import ru.ifmo.rain.maevsky.piccipher.CryptorException;

import java.util.Arrays;

/**
 * Created by faerytea on 09.07.16.
 */
public class SomeBitsInEachByte implements Cryptor {
    private final byte[] mask;

    public SomeBitsInEachByte() {
        this((byte) 1);
    }

    public SomeBitsInEachByte(int A, int R, int G, int B) {
        this.mask = new byte[4];
        this.mask[0] = (byte) A;
        this.mask[1] = (byte) R;
        this.mask[2] = (byte) G;
        this.mask[3] = (byte) B;
    }

    public SomeBitsInEachByte(byte... mask) {
        this.mask = Arrays.copyOf(mask, mask.length);
    }

    @Override
    public void encrypt(int[] array, byte[] text) throws CryptorException {
        if (calcMaxLength(array.length) < ((text.length + 4) * 8)) throw new CryptorException("Array is too small");
        byte[] prepared = new byte[text.length + 4];
        prepared[0] = (byte) ((text.length & 0xff000000) >> 24);
        prepared[1] = (byte) ((text.length & 0x00ff0000) >> 16);
        prepared[2] = (byte) ((text.length & 0x0000ff00) >> 8);
        prepared[3] = (byte)  (text.length & 0x000000ff);
        System.arraycopy(text, 0, prepared, 4, text.length);
        text = prepared;
        for (int i = 0, mi = 0; i < array.length; ++i) {
            array[i] &= ~(mask[mi] << 24);
            mi = (mi + 1) % mask.length;
            array[i] &= ~(mask[mi] << 16);
            mi = (mi + 1) % mask.length;
            array[i] &= ~(mask[mi] << 8);
            mi = (mi + 1) % mask.length;
            array[i] &= ~(mask[mi]);
            mi = (mi + 1) % mask.length;
        }
        for (int i = 0, ms = 0, ai = 0, as = 24, mi = 0; i < text.length; ++i) {
            for (int ts = 7; ts >= 0;) {
                if (((1 << (7 - ms)) & mask[mi]) != 0) {
                    array[ai] |= ((1 << (7 - ms)) & ((text[i] >> ts) << (7 - ms))) << as;
                    --ts;
                }
                if (++ms == 8) {
                    ms = 0;
                    ++mi;
                    if (mi >= mask.length) mi = 0;
                    as -= 8;
                    if (as < 0) {
                        as = 24;
                        ++ai;
                    }
                }
            }
        }
    }

    private byte bitCount(byte i) {
        return (byte) Integer.bitCount(i < 0 ? ((int) i) + 256 : (int) i);
    }

    private int calcMaxLength(int arrayLength) {
        byte[] bits = new byte[mask.length];
        int sum = 0;
        for (int i = 0; i < mask.length; i++) {
            sum += (bits[i] = bitCount(mask[i]));
        }
        int resum = (((arrayLength * 4) / mask.length) * sum);
        for (int i = ((arrayLength * 4) % mask.length), j = 0; i >= 0; --i, ++j) {
            resum += bits[j];
        }
        return resum;
    }

    @Override
    public byte[] decrypt(int[] array) throws CryptorException {
        boolean notDone = false;
        byte[] result = null;
        int ms = 0, ai = 0, as = 24, mi = 0;
        do {
            result = notDone
                    ? new byte[
                            ((result[0] & 0x000000ff) << 24) +
                            ((result[1] & 0x000000ff) << 16) +
                            ((result[2] & 0x000000ff) << 8) +
                             (result[3] & 0x000000ff)]
                    : new byte[4];
            notDone = !notDone;
            out:
            for (int i = 0; i < result.length; ++i) {
                for (int ts = 7; ts >= 0; ) {
                    if (((1 << (7 - ms)) & mask[mi]) != 0) {
                        result[i] |= (((1 << (7 - ms)) & (array[ai] >> as)) >> (7 - ms)) << ts;
                        --ts;
                    }
                    if (++ms == 8) {
                        ms = 0;
                        ++mi;
                        if (mi >= mask.length) mi = 0;
                        as -= 8;
                        if (as < 0) {
                            as = 24;
                            if (++ai == array.length) break out;
                        }
                    }
                }
            }
        } while (notDone);
        return result;
    }
}
