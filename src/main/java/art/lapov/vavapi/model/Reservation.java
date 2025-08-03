package art.lapov.vavapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "reservation")
public class Reservation implements BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @NotNull
    private ReservationStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @Positive
    private Integer totalCostInCents;
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime paidAt;

    @ManyToOne
    private User client;
    @ManyToOne
    private Station station;
    @OneToOne
    private Payment payment;
    @OneToOne
    private Review review;
}
