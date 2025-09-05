package io.github.sinri.drydock.aviation;

import io.github.sinri.drydock.naval.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.core.servant.sundial.KeelSundial;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;

import javax.annotation.Nonnull;

/**
 * An abstract bomber aircraft that operates from an aircraft carrier deck.
 * <p>
 * This class extends {@link KeelSundial} to provide scheduled task capabilities
 * and implements {@link Biplane} interface to define aircraft-specific operations.
 * <p>
 * A bomber is designed for carrying and delivering payloads to targets,
 * typically used for strategic bombing missions. It operates within the context
 * of an aircraft carrier's deck system and integrates with the carrier's
 * issue recording and monitoring infrastructure.
 * <p>
 * The bomber maintains a reference to its assigned aircraft carrier deck
 * and delegates issue recording to the deck's issue record center.
 * 
 * @since 3.0.0
 * @see Biplane
 * @see AircraftCarrierDeck
 * @see KeelSundial
 */
public abstract class Bomber extends KeelSundial implements Biplane {
    /**
     * The aircraft carrier deck that this bomber is assigned to.
     * <p>
     * This deck provides the operational context for the bomber,
     * including issue recording and monitoring capabilities.
     */
    private final AircraftCarrierDeck deck;

    /**
     * Constructs a new bomber aircraft assigned to the specified carrier deck.
     * <p>
     * The bomber will be initialized with the given deck and will delegate
     * all issue recording operations to the deck's issue record center.
     *
     * @param deck the aircraft carrier deck to assign this bomber to
     * @throws NullPointerException if the deck parameter is null
     */
    public Bomber(@Nonnull AircraftCarrierDeck deck) {
        super();
        this.deck = deck;
    }

    /**
     * Returns the aircraft carrier deck that this bomber is assigned to.
     * <p>
     * This method provides access to the deck instance that was provided
     * during construction, allowing the bomber to interact with the
     * carrier's operational systems.
     *
     * @return the aircraft carrier deck instance, never null
     */
    @Nonnull
    @Override
    public final AircraftCarrierDeck getAircraftCarrierDeck() {
        return deck;
    }

    /**
     * Returns the issue record center associated with this bomber.
     * <p>
     * This method delegates to the aircraft carrier deck's issue record center,
     * ensuring that all issues and events are recorded in the context of
     * the carrier's monitoring system.
     *
     * @return the issue record center from the assigned aircraft carrier deck
     */
    @Override
    public final KeelIssueRecordCenter getIssueRecordCenter() {
        return getAircraftCarrierDeck().getIssueRecordCenter();
    }
}
