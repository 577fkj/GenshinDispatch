package cn.fkj233.genshindispatch.handlers;

import cn.fkj233.genshindispatch.data.ComboTokenReqJson;
import cn.fkj233.genshindispatch.data.LoginAccountRequestJson;
import cn.fkj233.genshindispatch.data.LoginTokenRequestJson;
import cn.fkj233.genshindispatch.data.Router;
import io.javalin.Javalin;
import io.javalin.http.Context;

import static cn.fkj233.genshindispatch.GenshinDispatch.gson;

public final class DispatchHandler implements Router {
    @Override public void applyRoutes(Javalin javalin) {
        // Username & Password login (from client).
        javalin.post("/hk4e_global/mdk/shield/api/login", DispatchHandler::clientLogin);
        // Cached token login (from registry).
        javalin.post("/hk4e_global/mdk/shield/api/verify", DispatchHandler::tokenLogin);
        // Combo token login (from session key).
        javalin.post("/hk4e_global/combo/granter/login/v2/login", DispatchHandler::sessionKeyLogin);
    }

    /**
     * @route /hk4e_global/mdk/shield/api/login
     */
    private static void clientLogin(Context ctx) {
        String rawBodyData = ctx.body();
        var bodyData = gson.fromJson(rawBodyData, LoginAccountRequestJson.class);

        if (bodyData == null)
            return;

        // TODO: Login
        var responseData = "";
        ctx.json(responseData);
    }

    /**
     * @route /hk4e_global/mdk/shield/api/verify
     */
    private static void tokenLogin(Context ctx) {
        String rawBodyData = ctx.body();
        var bodyData = gson.fromJson(rawBodyData, LoginTokenRequestJson.class);

        if (bodyData == null)
            return;

        // TODO: tokenLogin
        var responseData = "";
        ctx.json(responseData);
    }

    /**
     * @route /hk4e_global/combo/granter/login/v2/login
     */
    private static void sessionKeyLogin(Context ctx) {
        String rawBodyData = ctx.body();
        var bodyData = gson.fromJson(rawBodyData, ComboTokenReqJson.class);

        if (bodyData == null || bodyData.data == null)
            return;

        var tokenData = gson.fromJson(bodyData.data, ComboTokenReqJson.LoginTokenData.class);

        // TODO: sessionKeyLogin
        var responseData = "";
        ctx.json(responseData);
    }
}