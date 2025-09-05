package io.github.sinri.drydock.aviation;

import io.github.sinri.drydock.naval.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.core.servant.queue.KeelQueue;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;

import javax.annotation.Nonnull;

/**
 * An abstract drone aircraft that operates from an aircraft carrier deck.
 * <p>
 * This class extends {@link KeelQueue} to provide queue-based task processing capabilities
 * and implements {@link Biplane} interface to define aircraft-specific operations.
 * <p>
 * A drone is an unmanned aerial vehicle designed for autonomous or remotely controlled
 * operations. It operates within the context of an aircraft carrier's deck system
 * and integrates with the carrier's issue recording and monitoring infrastructure.
 * <p>
 * The drone maintains a reference to its assigned aircraft carrier deck
 * and delegates issue recording to the deck's issue record center. Unlike manned
 * aircraft, drones can process tasks through a queue-based system for efficient
 * autonomous operation.
 * 
 * @since 3.0.0
 * @see Biplane
 * @see AircraftCarrierDeck
 * @see KeelQueue
 */
public abstract class Drone extends KeelQueue implements Biplane {
    /**
     * The aircraft carrier deck that this drone is assigned to.
     * <p>
     * This deck provides the operational context for the drone,
     * including issue recording and monitoring capabilities.
     */
    private final AircraftCarrierDeck deck;

    /**
     * Constructs a new drone aircraft assigned to the specified carrier deck.
     * <p>
     * The drone will be initialized with the given deck and will delegate
     * all issue recording operations to the deck's issue record center.
     * The drone can then process tasks through its queue-based system
     * for autonomous operation.
     *
     * @param deck the aircraft carrier deck to assign this drone to
     * @throws NullPointerException if the deck parameter is null
     */
    public Drone(@Nonnull AircraftCarrierDeck deck) {
        super();
        this.deck = deck;
    }

    /**
     * Returns the aircraft carrier deck that this drone is assigned to.
     * <p>
     * This method provides access to the deck instance that was provided
     * during construction, allowing the drone to interact with the
     * carrier's operational systems and queue-based task processing.
     *
     * @return the aircraft carrier deck instance, never null
     */
    @Nonnull
    @Override
    public final AircraftCarrierDeck getAircraftCarrierDeck() {
        return deck;
    }

    /**
     * Returns the issue record center associated with this drone.
     * <p>
     * This method delegates to the aircraft carrier deck's issue record center,
     * ensuring that all issues and events are recorded in the context of
     * the carrier's monitoring system. This is particularly important for
     * autonomous drone operations where comprehensive logging is essential.
     *
     * @return the issue record center from the assigned aircraft carrier deck
     */
    @Override
    public final KeelIssueRecordCenter getIssueRecordCenter() {
        return getAircraftCarrierDeck().getIssueRecordCenter();
    }
}
