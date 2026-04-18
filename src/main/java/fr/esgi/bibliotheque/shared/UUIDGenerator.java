package fr.esgi.bibliotheque.shared;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class UUIDGenerator implements DomainIdGenerator {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
