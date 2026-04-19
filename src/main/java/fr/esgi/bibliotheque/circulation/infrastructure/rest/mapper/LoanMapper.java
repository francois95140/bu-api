package fr.esgi.bibliotheque.circulation.infrastructure.rest.mapper;

import fr.esgi.bibliotheque.circulation.domain.Loan;
import fr.esgi.bibliotheque.circulation.infrastructure.rest.dto.LoanDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoanMapper {

    public LoanDto toDto(Loan loan) {
        return LoanDto.from(loan);
    }

    public List<LoanDto> toDtoList(List<Loan> loans) {
        return loans.stream().map(this::toDto).toList();
    }
}
