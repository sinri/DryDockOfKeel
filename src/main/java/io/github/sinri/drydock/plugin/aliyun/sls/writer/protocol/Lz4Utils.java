package io.github.sinri.drydock.plugin.aliyun.sls.writer.protocol;

import io.vertx.core.buffer.Buffer;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * LZ4压缩工具
 * <p>
 * 使用ThreadLocal缓冲区池优化内存分配，减少GC压力。
 * 每个线程维护自己的缓冲区，避免线程竞争的同时提供内存复用。
 *
 * @since 2.1.0
 */
public final class Lz4Utils {
    static final LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();

    /**
     * ThreadLocal缓冲区池，每个线程维护自己的缓冲区
     */
    private static final ThreadLocal<BufferPool> bufferPool = ThreadLocal.withInitial(BufferPool::new);

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
     * @since 2.1.0
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
     * <p>
     * 使用ThreadLocal缓冲区池优化内存分配，减少GC压力。
     * 对于大数据先尝试保守估算，失败时回退到最大长度策略。
     *
     * @param srcBytes 待压缩的字节数组
     * @return 压缩后的字节数组
     * @since 3.0.1
     */
    @Nonnull
    public static byte[] compress(@Nullable byte[] srcBytes) {
        if (srcBytes == null || srcBytes.length == 0) {
            return new byte[0];
        }

        BufferPool pool = bufferPool.get();
        try {
            // 对于大数据，先尝试保守估算以减少内存浪费
            if (srcBytes.length > 8192) { // 8KB以上才考虑优化
                // 使用保守的估算：原长度 + 64字节头部开销
                // 这个估算适用于大多数文本/JSON数据，通常压缩比在0.3-0.7之间
                int conservativeEstimate = srcBytes.length + 64;
                int maxCompressedLength = compressor.maxCompressedLength(srcBytes.length);

                // 只有当保守估算明显小于最大长度时才使用
                if (conservativeEstimate < maxCompressedLength * 0.7) {
                    byte[] compressedBytes = pool.getBuffer(conservativeEstimate);
                    try {
                        int compressedLength = compressor.compress(srcBytes, 0, srcBytes.length, compressedBytes, 0);
                        return java.util.Arrays.copyOf(compressedBytes, compressedLength);
                    } catch (Exception e) {
                        // 保守估算失败，回退到最大长度策略
                        // 继续执行下面的逻辑
                    }
                }
            }

            // 备用方案：使用最大压缩长度（原有逻辑）
            int maxCompressedLength = compressor.maxCompressedLength(srcBytes.length);
            byte[] compressedBytes = pool.getBuffer(maxCompressedLength);

            int compressedLength = compressor.compress(srcBytes, 0, srcBytes.length, compressedBytes, 0);

            // 总是返回精确大小的数组，避免返回过大的缓冲区
            return java.util.Arrays.copyOf(compressedBytes, compressedLength);
        } finally {
            // 归还缓冲区并进行内存管理
            pool.returnBuffer();
        }
    }

    /**
     * 获取当前线程的缓冲区池状态（用于监控和调试）
     *
     * @return 缓冲区大小，如果未初始化则返回0
     */
    static int getCurrentThreadBufferSize() {
        BufferPool pool = bufferPool.get();
        return pool.getCurrentSize();
    }

    /**
     * 清理当前线程的缓冲区池（用于测试或显式内存管理）
     */
    static void clearCurrentThreadBuffer() {
        BufferPool pool = bufferPool.get();
        pool.buffer = null;
        pool.currentSize = 0;
    }

    /**
     * 强制清理所有线程的缓冲区池（谨慎使用）
     * <p>
     * 注意：这个方法会移除ThreadLocal，但无法清理已经分配给其他线程的缓冲区。
     * 主要用于测试场景或应用程序关闭时的清理。
     */
    static void clearAllBuffers() {
        bufferPool.remove();
    }

    /**
     * 缓冲区池实现
     */
    private static class BufferPool {
        private byte[] buffer;
        private int currentSize;
        private long lastUsedTime;

        /**
         * 获取指定大小的缓冲区
         *
         * @param requiredSize 需要的缓冲区大小
         * @return 缓冲区数组
         */
        byte[] getBuffer(int requiredSize) {
            lastUsedTime = System.currentTimeMillis();

            if (buffer == null || buffer.length < requiredSize) {
                // 分配稍大一些的缓冲区，避免频繁重新分配
                int newSize;
                // use 10MB as default
                newSize = Math.max(requiredSize, 10 * 1024 * 1024);

                buffer = new byte[newSize];
                currentSize = newSize;
            }
            return buffer;
        }

        /**
         * 返还缓冲区并进行内存管理
         */
        void returnBuffer() {
            // 如果缓冲区过大或长时间未使用，释放它以避免内存泄漏
            long currentTime = System.currentTimeMillis();
            boolean tooLarge = currentSize > 16 * 1024 * 1024; // 默认10MB，如果分配了16MB的就赶紧灭了
            boolean tooOld = (currentTime - lastUsedTime) > 300_000; // 5分钟未使用

            if (tooLarge || tooOld) {
                buffer = null;
                currentSize = 0;
            }
        }

        /**
         * 获取当前缓冲区大小（用于监控）
         */
        int getCurrentSize() {
            return currentSize;
        }
    }
}
