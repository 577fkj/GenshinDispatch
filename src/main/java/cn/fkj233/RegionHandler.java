package cn.fkj233;


import cn.fkj233.proto.QueryCurrRegionHttpRspOuterClass.QueryCurrRegionHttpRsp;
import cn.fkj233.proto.QueryRegionListHttpRspOuterClass.QueryRegionListHttpRsp;
import cn.fkj233.proto.RegionInfoOuterClass.RegionInfo;
import cn.fkj233.proto.RegionSimpleInfoOuterClass.RegionSimpleInfo;
import cn.fkj233.proto.StopServerInfoOuterClass.StopServerInfo;
import cn.fkj233.proto.ForceUpdateInfoOuterClass.ForceUpdateInfo;
import com.google.protobuf.ByteString;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.Signature;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


import static cn.fkj233.GenshinDispatch.config;
import static cn.fkj233.GenshinDispatch.logger;

/**
 * Handles requests related to region queries.
 */
public final class RegionHandler implements Router {
    private static final Map<String, RegionData> regions = new ConcurrentHashMap<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String lr(String left, String right) {
        return left.isEmpty() ? right : left;
    }
    public static int lr(int left, int right) {
        return left == 0 ? right : left;
    }

    public RegionHandler() {}

    @Override public void applyRoutes(Javalin javalin) {
        javalin.get("/query_region_list", RegionHandler::queryRegionList);
        javalin.get("/query_cur_region/{region}", RegionHandler::queryCurrentRegion );
    }

    /**
     * @route /query_region_list
     */
    private static void queryRegionList(Context ctx) {
        String dispatchDomain = "http" + (config.useEncryption ? "s" : "") + "://"
                + lr(config.accessAddress, config.bindAddress) + ":"
                + lr(config.accessPort, config.bindPort);

        List<RegionSimpleInfo> servers = new ArrayList<>();
        List<String> usedNames = new ArrayList<>();

        var configuredRegions = new ArrayList<>(List.of(config.regions));

        configuredRegions.forEach(region -> {
            if (usedNames.contains(region.Name)) {
                logger.error("Region name already in use.");
                return;
            }

            var identifier = RegionSimpleInfo.newBuilder()
                    .setName(region.Name).setTitle(region.Title).setType("DEV_PUBLIC")
                    .setDispatchUrl(dispatchDomain + "/query_cur_region/" + region.Name)
                    .build();
            usedNames.add(region.Name); servers.add(identifier);

            var regionInfo = RegionInfo.newBuilder()
                    .setGateserverIp(region.Ip).setGateserverPort(region.Port)
                    .setSecretKey(ByteString.copyFrom(Crypto.DISPATCH_SEED))
                    .build();
            QueryCurrRegionHttpRsp updatedQuery = null;
            if (region.Run) {
                updatedQuery = QueryCurrRegionHttpRsp.newBuilder()
                        .setRegionInfo(regionInfo)
                        .build();
            } else {
                try {
                    updatedQuery = QueryCurrRegionHttpRsp.newBuilder()
                            .setRegionInfo(regionInfo)
                            .setRetcode(11)
                            .setMsg(region.stopServer.Title)
                            .setStopServer(StopServerInfo.newBuilder()
                                    .setStopBeginTime(Math.toIntExact(dateFormat.parse(region.stopServer.StartTime).getTime() / 1000))
                                    .setStopEndTime(Math.toIntExact(dateFormat.parse(region.stopServer.StopTime).getTime() / 1000))
                                    .setContentMsg(region.stopServer.Msg)
                                    .setUrl(region.stopServer.Url)
                                    .build())
                            .build();
                } catch (ParseException e) {
                    logger.info("parse time failed.");
                }
            }
            if (updatedQuery != null) {
                regions.put(region.Name, new RegionData(updatedQuery, Base64.getEncoder().encodeToString(updatedQuery.toByteString().toByteArray())));
            }
        });

        byte[] customConfig = "{\"sdkenv\":\"2\",\"checkdevice\":\"false\",\"loadPatch\":\"false\",\"showexception\":\"false\",\"regionConfig\":\"pm|fk|add\",\"downloadMode\":\"0\"}".getBytes();
        Crypto.xor(customConfig, Crypto.DISPATCH_KEY);

        QueryRegionListHttpRsp updatedRegionList = QueryRegionListHttpRsp.newBuilder()
                .addAllRegionList(servers)
                .setClientSecretKey(ByteString.copyFrom(Crypto.DISPATCH_SEED))
                .setClientCustomConfigEncrypted(ByteString.copyFrom(customConfig))
                .setEnableLoginPc(true).build();

        ctx.result(Base64.getEncoder().encodeToString(updatedRegionList.toByteString().toByteArray()));

        logger.info(String.format("[Dispatch] Client %s request: query_region_list", ctx.ip()));
    }

    /**
     * @route /query_cur_region/{region}
     */
    private static void queryCurrentRegion(Context ctx) {
        // Get region to query.
        String regionName = ctx.pathParam("region");
        String versionName = ctx.queryParam("version");
        var region = regions.get(regionName);

        // Get region data.
        String regionData = "CAESGE5vdCBGb3VuZCB2ZXJzaW9uIGNvbmZpZw==";
        if (ctx.queryParamMap().values().size() > 0) {
            if (region != null)
                regionData = region.getBase64();
        }


        int versionMajor;
        int versionMinor;
        int versionFix;
        if (versionName != null) {
            String[] versionCode = versionName.replaceAll(Pattern.compile("[a-zA-Z]").pattern(), "").split("\\.");
            versionMajor = Integer.parseInt(versionCode[0]);
            versionMinor = Integer.parseInt(versionCode[1]);
            versionFix = Integer.parseInt(versionCode[2]);
        } else {
            versionMajor = 3;
            versionMinor = 1;
            versionFix = 0;
        }

        if (versionMajor >= 3 || (versionMajor == 2 && versionMinor == 7 && versionFix >= 50) || (versionMajor == 2 && versionMinor == 8)) {
            try {

                if (ctx.queryParam("dispatchSeed") == null) {
                    // More love for UA Patch players
                    var rsp = new QueryCurRegionRspJson();

                    rsp.content = regionData;
                    rsp.sign = "TW9yZSBsb3ZlIGZvciBVQSBQYXRjaCBwbGF5ZXJz";

                    ctx.json(rsp);
                    return;
                }

                String key_id = ctx.queryParam("key_id");
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, Objects.equals(key_id, "3") ? Crypto.CUR_OS_ENCRYPT_KEY : Crypto.CUR_CN_ENCRYPT_KEY);
                var regionInfo = Base64.getDecoder().decode(regionData);

                //Encrypt regionInfo in chunks
                ByteArrayOutputStream encryptedRegionInfoStream = new ByteArrayOutputStream();

                //Thank you so much GH Copilot
                int chunkSize = 256 - 11;
                int regionInfoLength = regionInfo.length;
                int numChunks = (int) Math.ceil(regionInfoLength / (double) chunkSize);

                for (int i = 0; i < numChunks; i++) {
                    byte[] chunk = Arrays.copyOfRange(regionInfo, i * chunkSize, Math.min((i + 1) * chunkSize, regionInfoLength));
                    byte[] encryptedChunk = cipher.doFinal(chunk);
                    encryptedRegionInfoStream.write(encryptedChunk);
                }

                Signature privateSignature = Signature.getInstance("SHA256withRSA");
                privateSignature.initSign(Crypto.CUR_SIGNING_KEY);
                privateSignature.update(regionInfo);

                var rsp = new QueryCurRegionRspJson();

                rsp.content = Base64.getEncoder().encodeToString(encryptedRegionInfoStream.toByteArray());
                rsp.sign = Base64.getEncoder().encodeToString(privateSignature.sign());

                ctx.json(rsp);
            }
            catch (Exception e) {
                logger.error("An error occurred while handling query_cur_region.", e);
            }
        }
        else {
            ctx.result(regionData);
        }
        logger.info(String.format("Client %s request: query_cur_region/%s", ctx.ip(), regionName));
    }

    /**
     * Region data container.
     */
    public static class RegionData {
        private final QueryCurrRegionHttpRsp regionQuery;
        private final String base64;

        public RegionData(QueryCurrRegionHttpRsp prq, String b64) {
            this.regionQuery = prq;
            this.base64 = b64;
        }

        public QueryCurrRegionHttpRsp getRegionQuery() {
            return this.regionQuery;
        }

        public String getBase64() {
            return this.base64;
        }
    }
}