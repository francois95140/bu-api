package fr.esgi.bibliotheque.catalog.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "works")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "work_seq")
    @SequenceGenerator(name = "work_seq", sequenceName = "work_seq", allocationSize = 1)
    @Column(name = "technical_id")
    private Long technicalId;

    @Embedded
    private WorkId id;

    private String isbn;
    private String title;
    private String authors;
    private String publisher;
    private Integer year;
    private String subject;
    private String language;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "work", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Copy> copies = new ArrayList<>();

    public static Work create(WorkId id, String isbn, String title, String authors,
                               String publisher, Integer year, String subject,
                               String language, String description) {
        return Work.builder()
            .id(id)
            .isbn(isbn)
            .title(title)
            .authors(authors)
            .publisher(publisher)
            .year(year)
            .subject(subject)
            .language(language)
            .description(description)
            .build();
    }

    public void update(String title, String authors, String publisher,
                        Integer year, String subject, String language, String description) {
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.year = year;
        this.subject = subject;
        this.language = language;
        this.description = description;
    }
}
