package cn.fkj233.genshindispatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static cn.fkj233.genshindispatch.GenshinDispatch.logger;
import static cn.fkj233.genshindispatch.Utils.read;

public class Crypto {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static byte[] DISPATCH_KEY;
    public static byte[] DISPATCH_SEED;

    public static byte[] ENCRYPT_KEY;
    public static long ENCRYPT_SEED = Long.parseUnsignedLong("11468049314633205968");
    public static byte[] ENCRYPT_SEED_BUFFER = new byte[0];

    public static PublicKey CUR_OS_ENCRYPT_KEY;
    public static PublicKey CUR_CN_ENCRYPT_KEY;
    public static PrivateKey CUR_SIGNING_KEY;

    public static void loadKeys() {
        DISPATCH_KEY = read(new File("./keys/dispatchKey.bin").toPath());
        DISPATCH_SEED = read(new File("./keys/dispatchSeed.bin").toPath());

        ENCRYPT_KEY = read(new File("./keys/secretKey.bin").toPath());
        ENCRYPT_SEED_BUFFER = read(new File("./keys/secretKeyBuffer.bin").toPath());

        try {
            //These should be loaded from ChannelConfig_whatever.json
            CUR_SIGNING_KEY = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(read(new File("./keys/SigningKey.der").toPath())));

            CUR_OS_ENCRYPT_KEY = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(read(new File("./keys/OSCB_Pub.der").toPath())));

            CUR_CN_ENCRYPT_KEY = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(read(new File("./keys/OSCN_Pub.der").toPath())));
        }
        catch (Exception e) {
            logger.error("An error occurred while loading keys.", e);
        }
    }

    public static void xor(byte[] packet, byte[] key) {
        try {
            for (int i = 0; i < packet.length; i++) {
                packet[i] ^= key[i % key.length];
            }
        } catch (Exception e) {
            logger.error("Crypto error.", e);
        }
    }

    public static byte[] createSessionKey(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
}
