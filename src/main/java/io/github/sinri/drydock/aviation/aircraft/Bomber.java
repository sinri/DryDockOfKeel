package io.github.sinri.drydock.aviation.aircraft;

import io.github.sinri.drydock.aviation.carrier.AircraftCarrierDeck;
import io.github.sinri.drydock.common.SundialMixin;

import javax.annotation.Nonnull;

/**
 * @since 1.5.0 Technical Preview 和AircraftCarrierDeck配合使用的舰载轰炸机类，用于按照战术设计定时发起轰炸。
 */
public abstract class Bomber extends Biplane implements SundialMixin {
    public Bomber(@Nonnull AircraftCarrierDeck deck) {
        super(deck);
    }
}
