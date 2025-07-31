package art.lapov.vavapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private ReservationStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer totalCostInCents;
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
