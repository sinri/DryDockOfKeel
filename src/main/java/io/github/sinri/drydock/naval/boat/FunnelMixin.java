package io.github.sinri.drydock.naval.boat;

import io.github.sinri.drydock.naval.caravel.AliyunSLSAdapterImpl;
import io.github.sinri.keel.servant.funnel.KeelFunnel;
import io.vertx.core.Future;

import java.util.function.Supplier;

public interface FunnelMixin extends Boat {
    default KeelFunnel buildFunnel() {
        var funnel = new KeelFunnel();
        funnel.setLogger(generateLogger(AliyunSLSAdapterImpl.TopicFunnel, null));
        return funnel;
    }

    void funnel(Supplier<Future<Void>> supplier);
}
