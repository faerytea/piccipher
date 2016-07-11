package ru.ifmo.rain.maevsky.piccipher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by faerytea on 08.07.16.
 */
public class Injector {
    private final int[] pic;
    private final int width;

    public Injector(BufferedImage pic) {
        this.pic = pic.getRGB(0, 0, pic.getWidth(), pic.getHeight(), null, 0, pic.getWidth());
        this.width = pic.getWidth();
    }

    public Injector(InputStream stream) throws IOException {
        this(ImageIO.read(stream));
    }

    public BufferedImage getImage() {
        BufferedImage t = new BufferedImage(width, pic.length / width, BufferedImage.TYPE_INT_ARGB);
        t.setRGB(0, 0, width, pic.length / width, pic, 0, width);
        return t;
    }

    public void inject(Cryptor needle, byte[] injection) throws CryptorException {
        needle.encrypt(pic, injection);
    }

    public byte[] extract(Cryptor needle) throws CryptorException {
        return needle.decrypt(pic);
    }
}
