package cn.fkj233.genshindispatch.handlers;

import cn.fkj233.genshindispatch.data.Router;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;

import java.io.File;
import java.util.Objects;

import static cn.fkj233.genshindispatch.GenshinDispatch.config;
import static cn.fkj233.genshindispatch.GenshinDispatch.logger;
import static cn.fkj233.genshindispatch.Utils.lr;
import static cn.fkj233.genshindispatch.Utils.readString;

public final class AnnouncementsHandler implements Router {
    @Override public void applyRoutes(Javalin javalin) {
        // hk4e-api-os.hoyoverse.com
        this.allRoutes(javalin, "/common/hk4e_global/announcement/api/getAlertPic", new HttpJsonResponse("{\"retcode\":0,\"message\":\"OK\",\"data\":{\"total\":0,\"list\":[]}}"));
        // hk4e-api-os.hoyoverse.com
        this.allRoutes(javalin,"/common/hk4e_global/announcement/api/getAlertAnn", new HttpJsonResponse("{\"retcode\":0,\"message\":\"OK\",\"data\":{\"alert\":false,\"alert_id\":0,\"remind\":true}}"));
        // hk4e-api-os.hoyoverse.com
        this.allRoutes(javalin,"/common/hk4e_global/announcement/api/getAnnList", AnnouncementsHandler::getAnnouncement);
        // hk4e-api-os-static.hoyoverse.com
        this.allRoutes(javalin,"/common/hk4e_global/announcement/api/getAnnContent", AnnouncementsHandler::getAnnouncement);
        // hk4e-sdk-os.hoyoverse.com
        this.allRoutes(javalin,"/hk4e_global/mdk/shopwindow/shopwindow/listPriceTier", new HttpJsonResponse("{\"retcode\":0,\"message\":\"OK\",\"data\":{\"suggest_currency\":\"USD\",\"tiers\":[]}}"));

        javalin.get("/hk4e/announcement/*", AnnouncementsHandler::getPageResources);
    }

    private static void getAnnouncement(Context ctx) {
        String data = "";
        if (Objects.equals(ctx.endpointHandlerPath(), "/common/hk4e_global/announcement/api/getAnnContent")) {
            data = readString("GameAnnouncement.json");
        } else if (Objects.equals(ctx.endpointHandlerPath(), "/common/hk4e_global/announcement/api/getAnnList")) {
            data = readString("GameAnnouncementList.json");
        } else {
            ctx.result("{\"retcode\":404,\"message\":\"Unknown request path\"}");
        }

        if (data.isEmpty()) {
            ctx.result("{\"retcode\":500,\"message\":\"Unable to fetch requsted content\"}");
            return;
        }

        String dispatchDomain = "http" + (config.useEncryption ? "s" : "") + "://"
                + lr(config.accessAddress, config.bindAddress) + ":"
                + lr(config.accessPort, config.bindPort);

        data = data
                .replace("{{DISPATCH_PUBLIC}}", dispatchDomain)
                .replace("{{SYSTEM_TIME}}", String.valueOf(System.currentTimeMillis()));
        ctx.result("{\"retcode\":0,\"message\":\"OK\",\"data\": " + data + "}");
    }

    private static void getPageResources(Context ctx) {
        String possibleFilename = new File("./data/" + ctx.path()).getAbsolutePath();
        var data = readString(ctx.path());
        if (!data.isEmpty()) {
            ContentType fromExtension = ContentType.getContentTypeByExtension(possibleFilename.substring(possibleFilename.lastIndexOf(".") + 1));
            ctx.contentType(fromExtension != null ? fromExtension : ContentType.APPLICATION_OCTET_STREAM);
            ctx.result(data);
        } else {
            logger.warn("File does not exist: " + ctx.path());
        }
    }
}