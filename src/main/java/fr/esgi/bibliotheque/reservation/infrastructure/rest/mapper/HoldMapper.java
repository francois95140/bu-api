package fr.esgi.bibliotheque.reservation.infrastructure.rest.mapper;

import fr.esgi.bibliotheque.reservation.domain.Hold;
import fr.esgi.bibliotheque.reservation.infrastructure.rest.dto.HoldDto;
import org.springframework.stereotype.Component;

@Component
public class HoldMapper {

    public HoldDto toDto(Hold hold) {
        return HoldDto.from(hold);
    }
}
