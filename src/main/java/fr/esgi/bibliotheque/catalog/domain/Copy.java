package fr.esgi.bibliotheque.catalog.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "copies")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Copy {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "copy_seq")
    @SequenceGenerator(name = "copy_seq", sequenceName = "copy_seq", allocationSize = 1)
    @Column(name = "technical_id")
    private Long technicalId;

    @Embedded
    private CopyId id;

    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_technical_id")
    private Work work;

    @Enumerated(EnumType.STRING)
    private CopyStatus status;

    private String campusId;
    private String shelf;
    private String condition;

    public static Copy create(CopyId id, String barcode, Work work,
                               String campusId, String shelf, String condition) {
        return Copy.builder()
            .id(id)
            .barcode(barcode)
            .work(work)
            .status(CopyStatus.AVAILABLE)
            .campusId(campusId)
            .shelf(shelf)
            .condition(condition)
            .build();
    }

    public void updateStatus(CopyStatus newStatus) {
        this.status = newStatus;
    }
}
