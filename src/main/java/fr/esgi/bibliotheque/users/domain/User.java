package fr.esgi.bibliotheque.users.domain;

import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.TimeProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1)
    @Column(name = "technical_id")
    private Long technicalId;

    @Embedded
    private UserId id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private UserCategory category;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private Instant createdAt;

    public static User create(String firstName, String lastName, String email,
                               String passwordHash, UserCategory category,
                               DomainIdGenerator gen, TimeProvider time) {
        var user = new User();
        user.id = new UserId(gen.generate());
        user.firstName = firstName;
        user.lastName = lastName;
        user.email = email;
        user.passwordHash = passwordHash;
        user.category = category;
        user.status = UserStatus.ACTIVE;
        user.createdAt = time.now();
        return user;
    }

    public void update(String firstName, String lastName, UserCategory category) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.category = category;
    }

    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    public void unblock() {
        this.status = UserStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}
