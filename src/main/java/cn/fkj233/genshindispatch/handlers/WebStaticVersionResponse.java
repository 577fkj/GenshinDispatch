package cn.fkj233.genshindispatch.handlers;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.io.File;

import static cn.fkj233.genshindispatch.Utils.read;

public class WebStaticVersionResponse implements Handler {

    @Override
    public void handle(Context ctx) {
        String requestFor = ctx.path().substring(ctx.path().lastIndexOf("-") + 1);

        getPageResources("/webstatic/" + requestFor, ctx);
    }

    private static void getPageResources(String path, Context ctx) {
        var data = read(new File(path).toPath());
        if (data.length != 0) {
            ContentType fromExtension = ContentType.getContentTypeByExtension(path.substring(path.lastIndexOf(".") + 1));
            ctx.contentType(fromExtension != null ? fromExtension : ContentType.APPLICATION_OCTET_STREAM);
            ctx.result(data);
        } else {
            ctx.status(404);
        }
    }
}