package io.github.sinri.drydock.naval.boat;

import io.github.sinri.drydock.naval.caravel.AliyunSLSAdapterImpl;
import io.github.sinri.keel.servant.queue.KeelQueue;
import io.github.sinri.keel.servant.queue.KeelQueueNextTaskSeeker;
import org.jetbrains.annotations.NotNull;

/**
 * @since 1.1.0
 */
public interface QueueMixin extends Boat {
    default KeelQueue buildQueue() {
        var queue = new KeelQueue() {
            @Override
            protected @NotNull KeelQueueNextTaskSeeker getNextTaskSeeker() {
                return buildQueueNextTaskSeeker();
            }

            @Override
            protected @NotNull SignalReader getSignalReader() {
                return buildSignalReader();
            }
        };
        queue.setLogger(generateLogger(AliyunSLSAdapterImpl.TopicQueue, null));
        return queue;
    }

    KeelQueue.SignalReader buildSignalReader();

    KeelQueueNextTaskSeeker buildQueueNextTaskSeeker();
}
