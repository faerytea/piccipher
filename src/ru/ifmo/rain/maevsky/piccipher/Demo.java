package ru.ifmo.rain.maevsky.piccipher;

import ru.ifmo.rain.maevsky.piccipher.mycryptors.SomeBitsInEachByte;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by faerytea on 09.07.16.
 */
public class Demo {
    public static void main (String[] args) throws CryptorException {
        if ((args.length < 3)
                || (!(args[0].equals("encode") || args[0].equals("decode")))
                || (args[0].equals("encode") && (args.length != 5))
                || (args[0].equals("decode") && (args.length != 3))) {
            System.out.println("It is a demo of piccipher.");
            System.out.println("Usage:");
            System.out.println("\tjava -jar piccipher-wdemo.jar encode <image> <mask> <output> <text>");
            System.out.println("\tjava -jar piccipher-wdemo.jar decode <image> <mask>");
            System.out.println("\nRecommended image format - PNG.");
            System.out.println("<mask> is octal sequence which represents bits for spoiling. When mask ends while encoding, it will be repeated.");
            System.out.println("\nExample:");
            System.out.println("\tjava -jar piccipher-wdemo.jar encode image.png 3111 \"Secret text!\"");
            System.out.println("Encodes Secret text! to image using last two bits in alpha channel and each last bit in RGB channels.");
            System.out.println("\tjava -jar piccipher-wdemo.jar decode image.png 1");
            System.out.println("Decodes text from image using last bits in all ARGB channels.");
            return;
        }
        Injector inj;
        byte[] mask = getMask(args[2]);
        if (mask == null) {
            System.out.println("Your mask wrong (contains non-octal digits). See usage.");
            return;
        }
        Cryptor c = new SomeBitsInEachByte(mask);
        try {
            inj = new Injector(new FileInputStream(args[1]));
        } catch (FileNotFoundException e) {
            System.err.println("File not found.");
            return;
        } catch (IOException e) {
            System.err.println("Something went wrong while reading picture.");
            System.err.println(e.getMessage());
            return;
        }
        if (args[0].equals("encode")) {
            inj.inject(c, args[4].getBytes(StandardCharsets.UTF_8));
            if (!args[3].substring(args[3].length() - 4).equalsIgnoreCase(".png")) args[3] += ".png";
            try {
                if (!ImageIO.write(inj.getImage(), "png", new File(args[3])))
                    System.err.println("Cannot write picture. WTF :(");
            } catch (IOException e) {
                System.err.println("Something went wrong while writing picture.");
            }
        } else {
            byte[] decoded = inj.extract(c);
            System.out.println(new String(decoded, StandardCharsets.UTF_8));
        }
    }

    private static byte[] getMask(String mask) {
        byte[] res = new byte[mask.length()];
        for (int i = 0; i < res.length; ++i) {
            res[i] = (byte) (mask.charAt(i) - '0');
            if ((res[i] < 0) || (res[i] > 7)) return null;
        }
        return res;
    }
}
