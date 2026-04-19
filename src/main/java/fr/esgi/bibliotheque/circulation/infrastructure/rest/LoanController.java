package fr.esgi.bibliotheque.circulation.infrastructure.rest;

import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.circulation.application.models.BorrowCopyRequest;
import fr.esgi.bibliotheque.circulation.application.models.RenewLoanRequest;
import fr.esgi.bibliotheque.circulation.application.usecases.*;
import fr.esgi.bibliotheque.circulation.domain.LoanId;
import fr.esgi.bibliotheque.circulation.infrastructure.rest.dto.LoanDto;
import fr.esgi.bibliotheque.circulation.infrastructure.rest.mapper.LoanMapper;
import fr.esgi.bibliotheque.users.domain.UserId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final BorrowCopy borrowCopy;
    private final ReturnCopy returnCopy;
    private final RenewLoan renewLoan;
    private final DeclareLost declareLost;
    private final SearchLoansByUser searchLoansByUser;
    private final LoanMapper mapper;

    public LoanController(BorrowCopy borrowCopy, ReturnCopy returnCopy,
                           RenewLoan renewLoan, DeclareLost declareLost,
                           SearchLoansByUser searchLoansByUser, LoanMapper mapper) {
        this.borrowCopy = borrowCopy;
        this.returnCopy = returnCopy;
        this.renewLoan = renewLoan;
        this.declareLost = declareLost;
        this.searchLoansByUser = searchLoansByUser;
        this.mapper = mapper;
    }

    @GetMapping
    public List<LoanDto> getByUser(@RequestParam String userId) {
        return mapper.toDtoList(searchLoansByUser.handle(new UserId(userId)));
    }

    @PostMapping
    public ResponseEntity<LoanDto> borrow(@Valid @RequestBody BorrowCopyRequest request,
                                           @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                           UriComponentsBuilder ucb) {
        var loan = borrowCopy.handle(request, idempotencyKey);
        var uri = ucb.path("/api/loans/{id}").buildAndExpand(loan.getId().value()).toUri();
        return ResponseEntity.created(uri).body(mapper.toDto(loan));
    }

    @PostMapping("/return")
    public ResponseEntity<Void> returnCopy(@RequestParam String copyId,
                                            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        returnCopy.handle(new CopyId(copyId), idempotencyKey);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<Void> renew(@PathVariable String id) {
        renewLoan.handle(new RenewLoanRequest(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/lost")
    public ResponseEntity<Void> declareLost(@PathVariable String id) {
        declareLost.handle(new LoanId(id));
        return ResponseEntity.ok().build();
    }
}
