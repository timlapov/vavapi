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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "location")
public class Location implements BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @NotBlank
    private String name;
    private String description;
    @NotBlank
    private String address;
    @NotBlank
    private String city;
    @NotNull
    @Positive
    @Min(value = 10000)
    @Max(value = 99999)
    private Integer postalCode;
    @NotBlank
    private String country;
    private String photoUrl;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    private Boolean deleted;
    private Double latitude;
    private Double longitude;

    @ManyToOne
    private User owner;
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Station> stations = new ArrayList<>();

    @Transient
    public String getFullPhotoUrl() {
        return UrlUtil.buildImageUrl(photoUrl, "locations", false);
    }

    @Transient
    public String getMiniPhotoUrl() {
        return UrlUtil.buildImageUrl(photoUrl, "locations", true);
    }
}