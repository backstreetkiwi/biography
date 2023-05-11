package de.nikolauswinter.biography.tools.shell;

import java.text.MessageFormat;

public class CommandTimeoutException extends RuntimeException {

    private static final long serialVersionUID = 233222769810939692L;

    private long timeoutSeconds;

    public CommandTimeoutException(long timeoutSeconds) {
        super(MessageFormat.format("Command timed out after {0} seconds.", timeoutSeconds));
        this.timeoutSeconds = timeoutSeconds;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

}
