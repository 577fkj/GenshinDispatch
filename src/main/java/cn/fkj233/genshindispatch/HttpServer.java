package cn.fkj233.genshindispatch;

import cn.fkj233.genshindispatch.data.Router;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;

import static cn.fkj233.genshindispatch.GenshinDispatch.config;
import static cn.fkj233.genshindispatch.GenshinDispatch.logger;

public class HttpServer {

    private final Javalin javalin;

    public HttpServer() {
        javalin = Javalin.create(config -> {
            config.server(HttpServer::createServer);
            config.enforceSsl = GenshinDispatch.config.useEncryption;
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

    /**
     * Handles the '/' (index) endpoint on the Express application.
     */
    public static class DefaultRequestRouter implements Router {
        @Override public void applyRoutes(Javalin javalin) {
            javalin.get("/", ctx -> {
                ctx.contentType(ContentType.APPLICATION_JSON);
                ctx.result("{\"code\":\"0\"}");
            });
        }
    }

    /**
     * Handles unhandled endpoints on the Express application.
     */
    public static class UnhandledRequestRouter implements Router {
        @Override public void applyRoutes(Javalin javalin) {
            javalin.error(404, ctx -> {
                ctx.contentType(ContentType.TEXT_HTML);
                ctx.result("""
                        <!DOCTYPE html>
                        <html>
                            <head>
                                <meta charset="utf8">
                            </head>

                            <body>
                                <img src="https://http.cat/404" />
                            </body>
                        </html>
                        """);
            });
        }
    }
}
