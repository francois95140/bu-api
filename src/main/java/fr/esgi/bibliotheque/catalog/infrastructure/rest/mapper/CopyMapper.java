package fr.esgi.bibliotheque.catalog.infrastructure.rest.mapper;

import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.infrastructure.rest.dto.CopyDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CopyMapper {

    public CopyDto toDto(Copy copy) {
        return CopyDto.from(copy);
    }

    public List<CopyDto> toDtoList(List<Copy> copies) {
        return copies.stream().map(this::toDto).toList();
    }
}
