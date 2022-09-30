package cn.fkj233;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOError;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;

public class GenshinDispatch {
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static Config config;
    static Logger logger = LoggerFactory.getLogger(GenshinDispatch.class);
    private static LineReader consoleLineReader = null;
    static HttpServer httpServer;

    public static void main(String[] args) throws Exception {
        Crypto.loadKeys();
        loadConfig();

        httpServer = new HttpServer();
        httpServer.addRouter(RegionHandler.class);
        httpServer.start();

        startConsole();
    }

    public static void loadConfig() throws Exception {
        if (!new File("./config.json").exists()) {
            config = new Config();
            saveConfig();
        } else {
            try (var fileReader = Files.newBufferedReader(new File("./config.json").toPath(), StandardCharsets.UTF_8)) {
                config = gson.fromJson(fileReader, Config.class);
            }
        }
    }

    public static void saveConfig() {
        try (FileWriter file = new FileWriter("./config.json")) {
            file.write(gson.toJson(config));
        } catch (Throwable e) {
            logger.error("save config failed.", e);
        }
    }

    public static void startConsole() {
        logger.info("start done!");

        if (consoleLineReader == null) {
            Terminal terminal = null;
            try {
                terminal = TerminalBuilder.builder().jna(true).build();
            } catch (Exception e) {
                try {
                    // Fallback to a dumb jline terminal.
                    terminal = TerminalBuilder.builder().dumb(true).build();
                } catch (Exception ignored) {
                    // When dumb is true, build() never throws.
                }
            }
            consoleLineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
        }

        String input = null;
        boolean isLastInterrupted = false;
        while (true) {
            try {
                input = consoleLineReader.readLine("Dispatch> ");
            } catch (UserInterruptException e) {
                if (!isLastInterrupted) {
                    isLastInterrupted = true;
                    logger.info("Press Ctrl-C again to shutdown.");
                    continue;
                } else {
                    Runtime.getRuntime().exit(0);
                }
            } catch (EndOfFileException e) {
                logger.info("EOF detected.");
                continue;
            } catch (IOError e) {
                logger.error("An IO error occurred.", e);
                continue;
            }

            isLastInterrupted = false;
            try {
                String[] args = input.split(" ");
                Config.Region[] regions;
                switch (args[0]) {
                    case "help":
                        logger.info("list");
                        logger.info("add <name> <title> <ip> <port>");
                        logger.info("del <name>");
                        logger.info("start <name> TODO: not use");
                        logger.info("stop <name> TODO: not use");
                        logger.info("kill-server");
                        logger.info("help");
                        break;
                    case "list":
                        for (int i = 0;i < config.regions.length;i++) {
                            logger.info("Name: " + config.regions[i].Name + ":");
                            logger.info("    Title: " + config.regions[i].Title);
                            logger.info("    Ip: " + config.regions[i].Ip);
                            logger.info("    Port: " + config.regions[i].Port);
                            logger.info("    Running: " + config.regions[i].Run);
                        }
                        break;
                    case "add":
                        if (args.length != 5) {
                            logger.info("add <name> <title> <ip> <port>");
                            break;
                        }
                        ArrayList<String> name = new ArrayList<>();
                        regions = new Config.Region[config.regions.length + 1];
                        for (int i = 0;i < config.regions.length;i++) {
                            name.add(config.regions[i].Name);
                            regions[i] = config.regions[i];
                        }
                        if (name.contains(args[1])) {
                            logger.info("name is use!");
                            break;
                        }
                        Config.Region region = new Config.Region(args[1], args[2], args[3], Integer.parseInt(args[4]), true);
                        regions[config.regions.length] = region;
                        config.regions = regions;
                        saveConfig();
                        logger.info("Name: " + region.Name + ":");
                        logger.info("    Title: " + region.Title);
                        logger.info("    Ip: " + region.Ip);
                        logger.info("    Port: " + region.Port);
                        logger.info("    Running: " + region.Run);
                        break;
                    case "del":
                        if (args.length != 2) {
                            logger.info("del <name>");
                            break;
                        }
                        regions = new Config.Region[config.regions.length - 1];
                        for (int i = 0;i < config.regions.length;i++) {
                            if (!Objects.equals(config.regions[i].Name, args[1])) {
                                regions[i] = config.regions[i];
                            }
                        }
                        config.regions = regions;
                        break;
                    case "start":
                        if (args.length != 2) {
                            logger.info("start <name>");
                            break;
                        }
                        for (int i = 0;i < config.regions.length;i++) {
                            if (Objects.equals(config.regions[i].Name, args[1])) {
                                config.regions[i].Run = true;
                            }
                        }
                        break;
                    case "stop":
                        if (args.length != 2) {
                            logger.info("start <name>");
                            break;
                        }
                        for (int i = 0;i < config.regions.length;i++) {
                            if (Objects.equals(config.regions[i].Name, args[1])) {
                                config.regions[i].Run = false;
                            }
                        }
                        break;
                    case "kill-server":
                        System.exit(1000);
                    default:
                        logger.info("Unknown command!");
                }
                saveConfig();
            } catch (Exception e) {
                logger.error("run command failed.", e);
            }
        }
    }
}
