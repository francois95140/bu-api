package fr.esgi.bibliotheque.shared;

import java.time.Clock;
import java.time.Instant;

public class SystemUtcTimeProvider implements TimeProvider {

    private final Clock clock;

    public SystemUtcTimeProvider(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Instant now() {
        return Instant.now(clock);
    }
}
