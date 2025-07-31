package art.lapov.vavapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pricing_interval")
public class PricingInterval {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private Integer hourlyPriceInCents;
    private LocalTime startDate;
    private LocalTime endDate;

    @ManyToOne
    private Station station;
}
