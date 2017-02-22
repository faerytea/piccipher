package ru.ifmo.rain.maevsky.piccipher;

import ru.ifmo.rain.maevsky.piccipher.mycryptors.SomeBitsInEachByte;

import javax.imageio.ImageIO;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by faerytea on 09.07.16.
 */
public class Demo {
    public static void main (String[] args) throws CryptorException {
        if ((args.length < 3)
                || (!(args[0].equals("encode") || args[0].equals("decode")))
                || (args[0].equals("encode")
                    && (!((args.length == 5)
                      || ((args.length == 6)
                            && ((args[4].equalsIgnoreCase("-url"))
                            || (args[4].equalsIgnoreCase("-file")))))))
                || (args[0].equals("decode") && ((args.length != 3) && (args.length != 4)))) {
            System.out.println("It is a demo of piccipher.");
            System.out.println("Usage:");
            System.out.println("\tjava -jar piccipher-wdemo.jar decode <image> <mask>");
            System.out.println("\tjava -jar piccipher-wdemo.jar decode <image> <mask> <output>");
            System.out.println("\tjava -jar piccipher-wdemo.jar encode <image> <mask> <output> <text>");
            System.out.println("\tjava -jar piccipher-wdemo.jar encode <image> <mask> <output> -url <url>");
            System.out.println("\tjava -jar piccipher-wdemo.jar encode <image> <mask> <output> -file <path/to/file>");
            System.out.println("\nRecommended image format - PNG.");
            System.out.println("<mask> is octal sequence which represents bits for spoiling. When mask ends while encoding, it will be repeated.");
            System.out.println("\nExample:");
            System.out.println("\tjava -jar piccipher-wdemo.jar encode image.png 3111 \"Secret text!\"");
            System.out.println("Encodes Secret text! to image using last two bits in alpha channel and each last bit in RGB channels.");
            System.out.println("\tjava -jar piccipher-wdemo.jar decode image.png 1");
            System.out.println("Decodes text from image using last bits in all ARGB channels.");
            System.out.println("\tjava -jar piccipher-wdemo.jar decode image.png 011 top-secret.zip");
            System.out.println("Decodes file from image using last bits in RGB channels.");
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
            byte[] secret;
            if (args.length == 6) {
                if (args[4].equalsIgnoreCase("-file")) {
                    Path file = Paths.get(args[5]);
                    if (!file.toFile().isFile()) {
                        System.err.println(args[5] + " is not a file!");
                        return;
                    }
                    try {
                        secret = Files.readAllBytes(file);
                    } catch (IOException e) {
                        System.err.println("Cannot read: " + e.getMessage());
                        return;
                    }
                } else {
                    try {
                        URL url = new URL(args[5]);
                        URLConnection connection = url.openConnection();
                        connection.connect();
                        int len = connection.getContentLength();
                        if (len < 0) {
                            System.err.println("Bad size of secret data");
                            return;
                        }
                        secret = new byte[len];
                        int offset = 0;
                        try(BufferedInputStream stream
                                    = new BufferedInputStream(connection.getInputStream())) {
                            int s;
                            while ((s = stream.read()) != -1) {
                                secret[offset++] = (byte) s;
                            }
                        }
                    } catch (MalformedURLException e) {
                        System.err.println("Not an URL: " + e.getMessage());
                        return;
                    } catch (IOException e) {
                        System.err.println("IO problems: " + e.getMessage());
                        return;
                    }
                }
            } else {
                secret = args[4].getBytes(StandardCharsets.UTF_8);
            }
            inj.inject(c, secret);
            if (!args[3].substring(args[3].length() - 4).equalsIgnoreCase(".png")) args[3] += ".png";
            try {
                if (!ImageIO.write(inj.getImage(), "png", new File(args[3])))
                    System.err.println("Cannot write picture. WTF :(");
            } catch (IOException e) {
                System.err.println("Something went wrong while writing picture.");
            }
        } else {
            byte[] decoded = inj.extract(c);
            if (args.length == 4) {
                try {
                    Files.write(Paths.get(args[3]), decoded);
                } catch (IOException e) {
                    System.err.println("Cannot write: " + e.getMessage());
                    return;
                }
            } else {
                System.out.println(new String(decoded, StandardCharsets.UTF_8));
            }
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
