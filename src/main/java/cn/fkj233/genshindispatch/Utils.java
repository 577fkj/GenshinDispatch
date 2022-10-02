package cn.fkj233.genshindispatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static cn.fkj233.genshindispatch.GenshinDispatch.logger;

public class Utils {
    public static byte[] read(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            logger.warn("Failed to read file: " + path);
        }

        return new byte[0];
    }

    public static byte[] read(String path) {
        try {
            return Files.readAllBytes(new File(path).toPath());
        } catch (IOException e) {
            logger.warn("Failed to read file: " + path);
        }

        return new byte[0];
    }

    public static String readString(String path) {
        try {
            return new String(Files.readAllBytes(new File(path).toPath()));
        } catch (IOException e) {
            logger.warn("Failed to read file: " + path);
        }

        return "";
    }

    public static String lr(String left, String right) {
        return left.isEmpty() ? right : left;
    }
    public static int lr(int left, int right) {
        return left == 0 ? right : left;
    }
}
