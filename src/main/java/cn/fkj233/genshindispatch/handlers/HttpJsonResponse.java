package cn.fkj233.genshindispatch.handlers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public final class HttpJsonResponse implements Handler {
    private final String response;

    public HttpJsonResponse(String response) {
        this.response = response;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        ctx.result(response);
    }
}