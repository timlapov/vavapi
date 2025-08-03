package art.lapov.vavapi.mapper;

import art.lapov.vavapi.model.BaseEntity;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.mapstruct.TargetType;

@AllArgsConstructor
public abstract class ReferenceMapper {

    private EntityManager entityManager;

    public <T extends BaseEntity> T toEntity(Long id, @TargetType Class<T> entityClass) {
        return id != null ? entityManager.find(entityClass, id) : null;
    }

}
