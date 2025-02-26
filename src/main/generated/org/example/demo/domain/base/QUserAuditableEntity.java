package org.example.demo.domain.base;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserAuditableEntity is a Querydsl query type for UserAuditableEntity
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QUserAuditableEntity extends EntityPathBase<UserAuditableEntity> {

    private static final long serialVersionUID = -573030243L;

    public static final QUserAuditableEntity userAuditableEntity = new QUserAuditableEntity("userAuditableEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath createdBy = createString("createdBy");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath updatedBy = createString("updatedBy");

    public QUserAuditableEntity(String variable) {
        super(UserAuditableEntity.class, forVariable(variable));
    }

    public QUserAuditableEntity(Path<? extends UserAuditableEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserAuditableEntity(PathMetadata metadata) {
        super(UserAuditableEntity.class, metadata);
    }

}

