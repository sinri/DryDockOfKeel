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
    private static final LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();

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
     * 使用LZ4算法压缩字节数组，之前的实现
     *
     * @param srcBytes 待压缩的字节数组
     * @return 压缩后的字节数组
     * @deprecated as of 3.0.1, renamed from original {@link #compress(byte[])} and will be removed in a future release
     */
    @Deprecated
    @Nonnull
    static byte[] compressLegacy(@Nullable byte[] srcBytes) {
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
        // 使用更保守的初始大小估算，通常LZ4压缩比在0.3-0.8之间
        // 这里使用0.8作为安全系数，避免频繁扩容
        int estimatedSize = Math.max(32, (int) (srcBytes.length * 0.8));
        int maxCompressedLength = compressor.maxCompressedLength(srcBytes.length);

        // 使用较小的估算大小，但不超过最大压缩长度
        int initialSize = Math.min(estimatedSize, maxCompressedLength);
        byte[] compressedBytes = new byte[initialSize];

        try {
            int compressedLength = compressor.compress(srcBytes, 0, srcBytes.length, compressedBytes, 0);

            // 如果压缩后的数据正好适合，直接返回调整大小的数组
            if (compressedLength == compressedBytes.length) {
                return compressedBytes;
            }

            // 创建精确大小的结果数组
            byte[] resultBytes = new byte[compressedLength];
            System.arraycopy(compressedBytes, 0, resultBytes, 0, compressedLength);
            return resultBytes;

        } catch (Exception e) {
            // 如果初始大小不够，回退到使用最大压缩长度
            compressedBytes = new byte[maxCompressedLength];
            int compressedLength = compressor.compress(srcBytes, 0, srcBytes.length, compressedBytes, 0);

            byte[] resultBytes = new byte[compressedLength];
            System.arraycopy(compressedBytes, 0, resultBytes, 0, compressedLength);
            return resultBytes;
        }
    }
}
