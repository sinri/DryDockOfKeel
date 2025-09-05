package io.github.sinri.drydock.aviation;

import io.github.sinri.drydock.naval.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.web.http.KeelHttpServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * An abstract fighter aircraft that operates from an aircraft carrier deck.
 * <p>
 * This class extends {@link KeelHttpServer} to provide HTTP server capabilities
 * and implements {@link Biplane} interface to define aircraft-specific operations.
 * <p>
 * A fighter is a military aircraft designed primarily for air-to-air combat
 * against other aircraft. It operates within the context of an aircraft carrier's
 * deck system and integrates with the carrier's issue recording and monitoring
 * infrastructure. Additionally, it provides HTTP server functionality for
 * communication and control interfaces.
 * <p>
 * The fighter maintains a reference to its assigned aircraft carrier deck
 * and delegates issue recording to the deck's issue record center. It also
 * configures an HTTP server port for external communication and control.
 * 
 * @since 3.0.0
 * @see Biplane
 * @see AircraftCarrierDeck
 * @see KeelHttpServer
 */
public abstract class Fighter extends KeelHttpServer implements Biplane {
    /**
     * The default HTTP server port for fighter aircraft.
     * <p>
     * This port is used when no specific port is provided during construction.
     */
    private static final int DEFAULT_PORT = 8080;
    
    /**
     * The aircraft carrier deck that this fighter is assigned to.
     * <p>
     * This deck provides the operational context for the fighter,
     * including issue recording and monitoring capabilities.
     */
    private final AircraftCarrierDeck deck;
    
    /**
     * The HTTP server port configured for this fighter.
     * <p>
     * This port is used for external communication and control interfaces.
     */
    private final int port;

    /**
     * Constructs a new fighter aircraft assigned to the specified carrier deck.
     * <p>
     * The fighter will be initialized with the given deck and will delegate
     * all issue recording operations to the deck's issue record center.
     * The fighter will also configure an HTTP server on the specified port
     * for external communication and control interfaces.
     * <p>
     * If no port is specified (null), the default port 8080 will be used.
     *
     * @param deck the aircraft carrier deck to assign this fighter to
     * @param port the HTTP server port to use, or null to use the default port
     * @throws NullPointerException if the deck parameter is null
     */
    public Fighter(@Nonnull AircraftCarrierDeck deck, @Nullable Integer port) {
        super();
        this.deck = deck;
        this.port = Objects.requireNonNullElse(port, DEFAULT_PORT);
    }

    /**
     * Returns the aircraft carrier deck that this fighter is assigned to.
     * <p>
     * This method provides access to the deck instance that was provided
     * during construction, allowing the fighter to interact with the
     * carrier's operational systems and HTTP server infrastructure.
     *
     * @return the aircraft carrier deck instance, never null
     */
    @Nonnull
    @Override
    public final AircraftCarrierDeck getAircraftCarrierDeck() {
        return deck;
    }

    /**
     * Returns the issue record center associated with this fighter.
     * <p>
     * This method delegates to the aircraft carrier deck's issue record center,
     * ensuring that all issues and events are recorded in the context of
     * the carrier's monitoring system. This is particularly important for
     * fighter operations where comprehensive logging and monitoring are essential
     * for mission success and safety.
     *
     * @return the issue record center from the assigned aircraft carrier deck
     */
    @Override
    public final KeelIssueRecordCenter getIssueRecordCenter() {
        return getAircraftCarrierDeck().getIssueRecordCenter();
    }

    /**
     * Returns the HTTP server port configured for this fighter.
     * <p>
     * This port is used for external communication and control interfaces,
     * allowing external systems to interact with the fighter's HTTP server
     * for monitoring, control, and data exchange purposes.
     *
     * @return the HTTP server port number
     */
    @Override
    public final int getHttpServerPort() {
        return port;
    }

}
