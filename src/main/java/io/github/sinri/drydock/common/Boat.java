package io.github.sinri.drydock.common;

/**
 * @since 3.0.0
 */
public interface Boat extends CommonUnit {

    /**
     * Initiates the launching process for the current instance.
     * This typically involves setting up configurations, initializing systems, and starting the main processing logic.
     *
     * @param args An array of command-line arguments used during the launch.
     *             These arguments may be passed to customize and influence the launch behavior.
     */
    void launch(String[] args);

    /**
     * Handles an event of catastrophic failure for the instance. Logs the exception details and exits the application
     * with a predetermined failure code.
     *
     * @param throwable the error or exception that triggered the shipwreck handling
     */
    void handleError(Throwable throwable);

    /**
     * Triggers the process for sinking the current instance and exiting the system.
     */
    void finish();
}
