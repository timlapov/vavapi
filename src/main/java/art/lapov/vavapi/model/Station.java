package art.lapov.vavapi.model;

import art.lapov.vavapi.utils.UrlUtil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "station")
public class Station implements BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Positive
    private Long maxPowerWatt;
    @NotNull
    private ConnectorType connectorType;
    @NotNull
    private Boolean enabled;
    private String description;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    private String photoUrl;
    private Boolean deleted;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations =  new ArrayList<>();
    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PricingInterval> pricingIntervals = new ArrayList<>();

    @ManyToOne
    private Location location;

    @Transient
    public String getFullPhotoUrl() {
        return UrlUtil.buildImageUrl(photoUrl, "stations", false);
    }

    @Transient
    public String getMiniPhotoUrl() {
        return UrlUtil.buildImageUrl(photoUrl, "stations", true);
    }
}