package io.github.sinri.drydock.plugin.aliyun.sls.writer.protocol;

import io.vertx.core.buffer.Buffer;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * LZ4压缩工具
 *
 * @since 2.1.0
 */
public final class Lz4Utils {
    private static final LZ4Factory factory = LZ4Factory.fastestInstance();
    private static final LZ4Compressor compressor = factory.fastCompressor();

    /**
     * 使用LZ4算法压缩Buffer
     *
     * @param buffer 待压缩的Buffer
     * @return 压缩后的Buffer
     */
    @Nonnull
    public static Buffer compress(@Nullable Buffer buffer) {
        if (buffer == null || buffer.length() == 0) {
            return Buffer.buffer();
        }

        byte[] srcBytes = buffer.getBytes();
        byte[] compressedBytes = compress(srcBytes);
        return Buffer.buffer(compressedBytes);
    }

    /**
     * 使用LZ4算法压缩字节数组
     *
     * @param srcBytes 待压缩的字节数组
     * @return 压缩后的字节数组
     */
    @Nonnull
    public static byte[] compress(@Nullable byte[] srcBytes) {
        if (srcBytes == null || srcBytes.length == 0) {
            return new byte[0];
        }

        int maxCompressedLength = compressor.maxCompressedLength(srcBytes.length);
        byte[] compressedBytes = new byte[maxCompressedLength];
        int compressedLength = compressor.compress(srcBytes, 0, srcBytes.length, compressedBytes, 0);
        byte[] resultBytes = new byte[compressedLength];
        System.arraycopy(compressedBytes, 0, resultBytes, 0, compressedLength);
        return resultBytes;
    }
}
