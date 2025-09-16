package io.github.sinri.drydock.plugin.aliyun.sls.writer.protocol;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
    void compressIdenticalTest() {
        List<String> strings = Keel.randomHelper().generateUniqueRandomStrings(1, 1024);
        var s = strings.get(0);
        byte[] srcBytes = s.getBytes(StandardCharsets.UTF_8);
        byte[] bytesLegacy = Lz4Utils.compressLegacy(srcBytes);
        byte[] bytesCurrent = Lz4Utils.compress(srcBytes);

        Assertions.assertArrayEquals(bytesLegacy, bytesCurrent);

        String sLegacy = Keel.stringHelper().bufferToHexMatrix(Buffer.buffer(bytesLegacy), 8);
        String sCurrent = Keel.stringHelper().bufferToHexMatrix(Buffer.buffer(bytesCurrent), 8);
        Assertions.assertEquals(sLegacy, sCurrent);

        getUnitTestLogger().info("legacy: \n" + sLegacy);
        getUnitTestLogger().info("current: \n" + sCurrent);
    }
}