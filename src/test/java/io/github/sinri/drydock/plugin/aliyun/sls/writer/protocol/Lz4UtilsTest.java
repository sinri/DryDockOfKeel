package io.github.sinri.drydock.plugin.aliyun.sls.writer.protocol;

import io.github.sinri.keel.core.helper.runtime.GCStatResult;
import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

@ExtendWith(VertxExtension.class)
class Lz4UtilsTest extends KeelJUnit5Test {

    /**
     * The constructor would run after {@code @BeforeAll} annotated method.
     */
    public Lz4UtilsTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    void getMax() {
        var max = Lz4Utils.compressor.maxCompressedLength(10 * 1024 * 1024);
        getUnitTestLogger().info("Max compressed length: " + max);
    }

    @Test
    void compressIdenticalTest() {
        List<String> strings = Keel.randomHelper().generateUniqueRandomStrings(1, 1024 * 1024 * 10);
        var s = strings.get(0);
        byte[] srcBytes = s.getBytes(StandardCharsets.UTF_8);
        byte[] bytesLegacy = Lz4Utils.compressLegacy(srcBytes);
        byte[] bytesCurrent = Lz4Utils.compress(srcBytes);

        Assertions.assertArrayEquals(bytesLegacy, bytesCurrent);

        String sLegacy = Keel.stringHelper().bufferToHexMatrix(Buffer.buffer(bytesLegacy), 8);
        String sCurrent = Keel.stringHelper().bufferToHexMatrix(Buffer.buffer(bytesCurrent), 8);
        Assertions.assertEquals(sLegacy, sCurrent);
    }

    //@Test
    //@Timeout(60)
    void memoryTest(VertxTestContext testContext) {
        AtomicReference<GCStatResult> gcStatResultRef = new AtomicReference<>();

        gcStatResultRef.set(Keel.runtimeHelper().getGCSnapshot());
        getUnitTestLogger().info("Before", ctx -> ctx
                        .put("heap", Keel.runtimeHelper().getHeapMemoryUsage().getUsed() / (1024 * 1024 * 1.0))
                //.put("gc", gcStatResultRef.get().toJsonObject())
        );

        Keel.parallelForAllComplete(List.of(0, 1), index -> {
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    list.add(i);
                }
                return Keel.asyncCallIteratively(list, round -> {
                    int len = Keel.randomHelper().generateRandomInt(128, 1024 * 1024 * 20);
                    var s = Keel.randomHelper().generateRandomString(len);
                    byte[] srcBytes = s.getBytes(StandardCharsets.UTF_8);

                    Lz4Utils.compress(srcBytes);
                    // Lz4Utils.compressLegacy(srcBytes);

                    GCStatResult x1 = Keel.runtimeHelper().getGCSnapshot();
                    GCStatResult x0 = gcStatResultRef.get();
                    GCStatResult diff = x1.since(x0);
                    gcStatResultRef.set(diff);

                    double mb = Keel.runtimeHelper().getHeapMemoryUsage().getUsed() / (1024 * 1024 * 1.0);
                    getUnitTestLogger().info("Round " + round + " of Index " + index, ctx -> ctx
                                    .put("heap", mb)
                            //.put("gc", gcStatResultRef.get().toJsonObject())
                    );
                    if (mb > 500) {
                        getUnitTestLogger().error("Heap is too big: " + mb);
                    }
                    return Keel.asyncSleep(100L);
                });
            })
            .onComplete(testContext.succeedingThenComplete());
    }
}