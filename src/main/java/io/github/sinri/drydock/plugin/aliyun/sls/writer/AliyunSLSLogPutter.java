package io.github.sinri.drydock.plugin.aliyun.sls.writer;

import io.github.sinri.drydock.plugin.aliyun.sls.writer.entity.LogGroup;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.protocol.Lz4Utils;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 1.0
 */
class AliyunSLSLogPutter {
    @Nonnull
    private final String accessKeyId;
    @Nonnull
    private final String accessKeySecret;
    @Nonnull
    private final WebClient webClient;
    @Nonnull
    private final String endpoint;

    public AliyunSLSLogPutter(@Nonnull String accessKeyId, @Nonnull String accessKeySecret, @Nonnull String endpoint) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.webClient = WebClient.create(Keel.getVertx());
        this.endpoint = endpoint;
    }

    /**
     * Build source from configuration. Source Expression should be: - EMPTY/BLANK STRING or NULL: use SLS default
     * source generation; - A TEMPLATED STRING --- Rule 1: Replace [IP] to local address;
     */
    @Nonnull
    public static String buildSource(@Nullable String configuredSourceExpression) {
        if (configuredSourceExpression == null || configuredSourceExpression.isBlank()) {
            return "";
        }
        // Rule 1: Replace [IP] to local address
        String localHostAddress = Keel.netHelper().getLocalHostAddress();
        if (localHostAddress == null) {
            Keel.getLogger().warning("Could not get local host address for SLS source!");
            return "";
        }
        return configuredSourceExpression.replaceAll("\\[IP]", localHostAddress);
    }

    public void close() {
        this.webClient.close();
    }

    /**
     * 调用PutLogs API。
     *
     * @param project  Project name
     * @param logstore Logstore name
     * @param logGroup LogGroup to be sent
     * @return Future of void if successful, or failed future with error message
     */
    public Future<Void> putLogs(@Nonnull String project, @Nonnull String logstore, @Nonnull LogGroup logGroup) {
        String uri = String.format("/logstores/%s/shards/lb", logstore);
        String url = String.format("https://%s.%s%s", project, endpoint, uri);

        String date = getGMTDate();
        String contentType = "application/x-protobuf";

        Map<String, String> headers = new HashMap<>();
        headers.put("Date", date);
        headers.put("Content-Type", contentType);
        headers.put("x-log-apiversion", "0.6.0");
        headers.put("x-log-signaturemethod", "hmac-sha1");
        headers.put("x-log-compresstype", "lz4");
        headers.put("Host", project + "." + endpoint);

        // Convert LogGroup to protobuf format
        Buffer raw = serializeLogGroup(logGroup);
        headers.put("x-log-bodyrawsize", String.valueOf(raw.length()));
        Buffer payload = Lz4Utils.compress(raw);
        headers.put("Content-Length", String.valueOf(payload.length()));

        try {
            var contentMd5 = Base64.getEncoder().encodeToString(
                    java.security.MessageDigest.getInstance("MD5").digest(payload.getBytes())
            );
            headers.put("Content-MD5", contentMd5);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }

        // Calculate signature and add authorization header
        String signature = calculateSignature(
                "POST",
                payload,
                contentType,
                date,
                headers,
                uri,
                null
        );
        headers.put("Authorization", "LOG " + accessKeyId + ":" + signature);

        HttpRequest<Buffer> request = this.webClient.postAbs(url);
        headers.forEach(request::putHeader);
        return request.sendBuffer(payload)
                      .compose(bufferHttpResponse -> {
                          if (bufferHttpResponse.statusCode() == 200) {
                              // System.out.println("write to sls: 200");
                              return Future.succeededFuture();
                          }
                          return Future.failedFuture("put log failed [" + bufferHttpResponse.statusCode() + "] "
                                  + bufferHttpResponse.bodyAsString());
                      });
    }

    /**
     * According to Aliyun SLS API documentation, we should send LogGroupList;
     * But let's try sending just the first LogGroup to see if that works.
     */
    private Buffer serializeLogGroup(@Nonnull LogGroup logGroup) {
        return Buffer.buffer(logGroup.toProtobuf().toByteArray());
    }

    /**
     * Get current date in GMT format as required by SLS API.
     *
     * @return Date string in RFC1123 format
     */
    @Nonnull
    private String getGMTDate() {
        var RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
        SimpleDateFormat sdf = new SimpleDateFormat(RFC1123_PATTERN, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }

    private String calculateSignature(
            String method,
            Buffer body,
            String contentType,
            String date,
            Map<String, String> headers,
            String uri,
            String queries
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append("\n");
        if (body != null) {
            String md5 = Keel.digestHelper().MD5(body.getBytes());
            sb.append(md5).append("\n");
        }
        sb.append(contentType).append("\n");
        sb.append(date).append("\n");

        List<String> headerLines = headers.keySet().stream()
                                          .filter(headerName -> headerName.startsWith("x-log-") || headerName.startsWith("x-acs-"))
                                          .sorted().map(x -> x + ":" + headers.get(x))
                                          .collect(Collectors.toList());
        headerLines.forEach(x -> sb.append(x).append("\n"));

        sb.append(uri);
        if (queries != null && !queries.isBlank()) {
            sb.append("?").append(queries);
        }

        var signStr = sb.toString();

        try {
            // Calculate HMAC-SHA1 signature
            var HmacSHA1 = "HmacSHA1";
            Mac mac = Mac.getInstance(HmacSHA1);
            SecretKeySpec signingKey = new SecretKeySpec(
                    accessKeySecret.getBytes(StandardCharsets.UTF_8),
                    HmacSHA1
            );
            mac.init(signingKey);
            byte[] signatureBytes = mac.doFinal(signStr.getBytes(StandardCharsets.UTF_8));

            // Encode signature in Base64
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
