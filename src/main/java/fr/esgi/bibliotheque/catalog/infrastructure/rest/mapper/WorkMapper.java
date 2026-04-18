package fr.esgi.bibliotheque.catalog.infrastructure.rest.mapper;

import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.infrastructure.rest.dto.WorkDetailDto;
import fr.esgi.bibliotheque.catalog.infrastructure.rest.dto.WorkSummaryDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkMapper {

    public WorkSummaryDto toSummaryDto(Work work) {
        return WorkSummaryDto.from(work);
    }

    public WorkDetailDto toDetailDto(Work work) {
        return WorkDetailDto.from(work);
    }

    public List<WorkSummaryDto> toSummaryDtoList(List<Work> works) {
        return works.stream().map(this::toSummaryDto).toList();
    }
}
