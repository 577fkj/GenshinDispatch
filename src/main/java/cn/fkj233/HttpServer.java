package cn.fkj233;

import io.javalin.Javalin;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;

import static cn.fkj233.GenshinDispatch.config;
import static cn.fkj233.GenshinDispatch.logger;

public class HttpServer {

    private final Javalin javalin;

    public HttpServer() {
        javalin = Javalin.create(config -> {
            config.server(HttpServer::createServer);
            config.enforceSsl = cn.fkj233.GenshinDispatch.config.useEncryption;
        });
    }

    private static Server createServer() {
        Server server = new Server();
        ServerConnector serverConnector
                = new ServerConnector(server);

        if (config.useEncryption) {
            var sslContextFactory = new SslContextFactory.Server();
            var keystoreFile = new File(config.keystore);

            if (!keystoreFile.exists()) {
                config.useEncryption = false;
                logger.warn("keystore file not found!!");
            } else try {
                sslContextFactory.setKeyStorePath(keystoreFile.getPath());
                sslContextFactory.setKeyStorePassword(config.keystorePassword);
            } catch (Exception ignored) {
                logger.warn("keystore password error!");

                try {
                    sslContextFactory.setKeyStorePath(keystoreFile.getPath());
                    sslContextFactory.setKeyStorePassword("123456");

                    logger.warn("keystore use default password.");
                } catch (Exception exception) {
                    logger.warn("keystore load failed.");
                }
            } finally {
                serverConnector = new ServerConnector(server, sslContextFactory);
            }
        }

        serverConnector.setPort(config.bindPort);
        server.setConnectors(new ServerConnector[]{serverConnector});

        return server;
    }

    public HttpServer addRouter(Class<? extends Router> router, Object... args) {
        Class<?>[] types = new Class<?>[args.length];
        for (var argument : args)
            types[args.length - 1] = argument.getClass();

        try {
            var constructor = router.getDeclaredConstructor(types);
            var routerInstance = constructor.newInstance(args);
            routerInstance.applyRoutes(this.javalin);
        } catch (Exception exception) {
            logger.warn("router add failed!");
        } return this;
    }

    public void start() throws UnsupportedEncodingException {
        if (config.bindAddress.equals("")) {
            this.javalin.start(config.bindPort);
        }else {
            this.javalin.start(config.bindAddress, config.bindPort);
        }

        logger.info("dispatch bind {}:{}", config.accessAddress, javalin.port());
    }
}
