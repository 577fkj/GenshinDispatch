package cn.fkj233.genshindispatch.handlers;

import cn.fkj233.genshindispatch.data.Router;
import io.javalin.Javalin;
import io.javalin.http.Context;

public final class LogHandler implements Router {
    @Override public void applyRoutes(Javalin javalin) {
        // overseauspider.yuanshen.com
        javalin.post("/log", LogHandler::log);
        // log-upload-os.mihoyo.com
        javalin.post("/crash/dataUpload", LogHandler::log);
    }

    private static void log(Context ctx) {
        // TODO: Figure out how to dump request body and log to file.
        ctx.result("{\"code\":0}");
    }
}
