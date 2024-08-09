package io.github.sinri.drydock.common.baiyatan;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 1.4.9
 * For `io.github.sinri.drydock.naval.base.Warship#loadRemoteConfiguration()`.
 */
public class BaiyatanConfigurationLoader {
    private final String kumoriUrl;
    private final String kumoriClientCode;
    private final String kumoriClientSecret;

    private final JsonArray scope = new JsonArray();
    private String file = "config.properties";

    public BaiyatanConfigurationLoader() {
        this.kumoriUrl = Keel.config("kumori.url");
        this.kumoriClientCode = Keel.config("kumori.clientCode");
        this.kumoriClientSecret = Keel.config("kumori.clientSecret");
    }

    public BaiyatanConfigurationLoader setFile(String file) {
        this.file = file;
        return this;
    }

    public BaiyatanConfigurationLoader setScope(String scope) {
        this.scope.clear();
        String[] split = scope.split("/");
        for (var s : split) {
            this.scope.add(s);
        }
        return this;
    }

    protected Future<JsonObject> callOpenApi(String api, JsonObject body) {
        long timestamp = System.currentTimeMillis();
        body.put("client_code", kumoriClientCode);
        body.put("timestamp", timestamp);
        body.put("checksum", KeelHelpers.digestHelper().md5(kumoriClientCode + "@" + timestamp + "@" + kumoriClientSecret));
        return WebClient.create(Keel.getVertx())
                .postAbs(kumoriUrl + api)
                .sendJsonObject(body)
                .compose(bufferHttpResponse -> {
                    if (bufferHttpResponse.statusCode() != 200) {
                        System.out.println("CODE IS NOT 200 BUT " + bufferHttpResponse.statusCode() + ", Body: " + bufferHttpResponse.bodyAsString());
                        throw new RuntimeException("CODE IS NOT 200 BUT " + bufferHttpResponse.statusCode());
                    }

                    JsonObject jsonObject = bufferHttpResponse.bodyAsJsonObject();
                    if (jsonObject == null) {
                        System.out.println("Body NOT JSON OBJECT: " + bufferHttpResponse.bodyAsString());
                        throw new RuntimeException("RESP IS NOT JSON Object but " + bufferHttpResponse.bodyAsString());
                    }

                    String code = jsonObject.getString("code");
                    if ("OK".equals(code)) {
                        return Future.succeededFuture(jsonObject);
                    } else {
                        Object data = body.getValue("data");
                        return Future.failedFuture("Kumori API RESPONDED FAILURE: " + data);
                    }
                });
    }


    public Future<Void> baiyatan() {
        var baiyatanProject = Keel.config("baiyatan.project");
        if (baiyatanProject == null) {
            System.out.println("baiyatan.project is missing, passover");
            return Future.succeededFuture();
        }
        System.out.println("baiyatan.project: " + baiyatanProject);
        System.out.println("scope: " + scope);

        return callOpenApi("/kumori/tianwen/baiyatan/reader", new JsonObject()
                .put("scope", scope)
                .put("project", new JsonArray().add(baiyatanProject))
                .put("file", file)
        )
                .onFailure(throwable -> {
                    System.out.println("baiyatan error " + throwable.getMessage());
                })
                .compose(resp -> {
                    Keel.getConfiguration().loadPropertiesFileContent(resp.getString("data"));
                    System.out.println("LOADED CONFIG FROM BAIYATAN");
                    //System.out.println("LOADED CONFIG FROM BAIYATAN:\n" + resp.getString("data"));
                    //System.out.println(Keel.getConfiguration().toJsonObject());
                    return Future.succeededFuture();
                });
    }
}
