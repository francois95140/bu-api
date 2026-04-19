package fr.esgi.bibliotheque.policy.infrastructure.rest;

import fr.esgi.bibliotheque.policy.application.models.UpdatePolicyRequest;
import fr.esgi.bibliotheque.policy.application.usecases.GetPolicyForCategory;
import fr.esgi.bibliotheque.policy.application.usecases.UpdatePolicy;
import fr.esgi.bibliotheque.policy.infrastructure.rest.dto.PolicyDto;
import fr.esgi.bibliotheque.users.domain.UserCategory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final GetPolicyForCategory getPolicyForCategory;
    private final UpdatePolicy updatePolicy;

    public PolicyController(GetPolicyForCategory getPolicyForCategory, UpdatePolicy updatePolicy) {
        this.getPolicyForCategory = getPolicyForCategory;
        this.updatePolicy = updatePolicy;
    }

    @GetMapping("/{category}")
    public PolicyDto getByCategory(@PathVariable UserCategory category) {
        return PolicyDto.from(getPolicyForCategory.handle(category));
    }

    @PutMapping("/{category}")
    public ResponseEntity<Void> update(@PathVariable UserCategory category,
                                        @Valid @RequestBody UpdatePolicyRequest request) {
        updatePolicy.handle(category, request);
        return ResponseEntity.ok().build();
    }
}
