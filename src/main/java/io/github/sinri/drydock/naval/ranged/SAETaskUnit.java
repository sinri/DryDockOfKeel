package io.github.sinri.drydock.naval.ranged;

import io.github.sinri.drydock.common.Boat;
import io.github.sinri.keel.core.TechnicalPreview;

/**
 * 运行于阿里云SAE任务模板环境下时，实现本接口以获取预设能力。
 *
 * @since 3.0.0
 */
@TechnicalPreview(since = "3.0.0")
public interface SAETaskUnit extends Boat {
    /**
     * 获取阿里云SAE任务模板运行实例ID，用于在日志中标记某次任务。
     *
     * @return SAE INSTANCE ID
     * @since 3.0.0
     */
    default String getSAEInstanceId() {
        return System.getenv("INSTANCE_ID");
    }
}
