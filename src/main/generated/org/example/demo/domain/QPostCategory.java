package org.example.demo.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPostCategory is a Querydsl query type for PostCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostCategory extends EntityPathBase<PostCategory> {

    private static final long serialVersionUID = -260953077L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPostCategory postCategory = new QPostCategory("postCategory");

    public final org.example.demo.domain.base.QUserAuditableEntity _super = new org.example.demo.domain.base.QUserAuditableEntity(this);

    public final ListPath<PostCategory, QPostCategory> children = this.<PostCategory, QPostCategory>createList("children", PostCategory.class, QPostCategory.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final QPostCategory parent;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public QPostCategory(String variable) {
        this(PostCategory.class, forVariable(variable), INITS);
    }

    public QPostCategory(Path<? extends PostCategory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPostCategory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPostCategory(PathMetadata metadata, PathInits inits) {
        this(PostCategory.class, metadata, inits);
    }

    public QPostCategory(Class<? extends PostCategory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parent = inits.isInitialized("parent") ? new QPostCategory(forProperty("parent"), inits.get("parent")) : null;
    }

}

