package org.example.demo.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.demo.SpringBootTestWithProfile;
import org.example.demo.domain.PostCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTestWithProfile("test")
@Transactional
class PostCategoryServiceImplTest {
    @Autowired
    PostCategoryService postCategoryServiceImpl;

    @PersistenceContext
    EntityManager em;

    @Test
    @DisplayName("게시글 카테고리 저장 테스트")
    void saveCategory() {
        // Given: 새로운 카테고리 생성
        PostCategory category = PostCategory.builder()
                .name("Sports")
                .build();

        // When: 카테고리 저장
        Long savedId = postCategoryServiceImpl.save(category);

        // Then: 저장된 카테고리 검증
        assertDoesNotThrow(() -> postCategoryServiceImpl.findById(savedId));
        PostCategory savedCategory = postCategoryServiceImpl.findById(savedId);
        assertEquals(category.getName(), savedCategory.getName());
    }

    @Test
    @DisplayName("모든 카테고리 조회 테스트")
    void findAllCategories() {
        // Given: 여러 카테고리 저장
        PostCategory category1 = PostCategory.builder()
                .name("Sports")
                .build();
        PostCategory category2 = PostCategory.builder()
                .name("Technology")
                .build();

        postCategoryServiceImpl.save(category1);
        postCategoryServiceImpl.save(category2);

        // When: 모든 카테고리 조회
        List<PostCategory> categories = postCategoryServiceImpl.findAll();

        // Then: 저장된 카테고리가 모두 조회되는지 확인
        assertTrue(categories.stream().anyMatch(c -> c.getName().equals("Sports")));
        assertTrue(categories.stream().anyMatch(c -> c.getName().equals("Technology")));
    }

    @Test
    @DisplayName("부모-자식 카테고리 연관관계 설정 테스트")
    void addChildToParent() {
        // Given: 부모와 자식 카테고리 생성
        PostCategory parent = PostCategory.builder()
                .name("Parent Category")
                .build();
        PostCategory child = PostCategory.builder()
                .name("Child Category")
                .build();

        parent.addChild(child);

        // When: 부모 카테고리 저장
        Long parentId = postCategoryServiceImpl.save(parent);

        em.flush();
        em.clear();

        // Then: 부모-자식 관계 검증
        PostCategory savedParent = postCategoryServiceImpl.findById(parentId);
        assertEquals(1, savedParent.getChildren().size());
        assertEquals("Child Category", savedParent.getChildren().get(0).getName());
    }

    @Test
    @DisplayName("부모-자식 카테고리 연관관계 제거 테스트")
    void removeChildFromParent() {
        // Given: 부모와 자식 카테고리 생성 및 저장
        PostCategory parent = PostCategory.builder()
                .name("Parent Category")
                .build();
        PostCategory child = PostCategory.builder()
                .name("Child Category")
                .build();

        parent.addChild(child);
        Long parentId = postCategoryServiceImpl.save(parent);

        em.flush();
        em.clear();

        // When: 부모-자식 관계 제거
        PostCategory savedParent = postCategoryServiceImpl.findById(parentId);
        savedParent.removeChild(savedParent.getChildren().get(0));

        em.flush();
        em.clear();

        // Then: 자식이 고아 객체로 삭제되었는지 확인 (orphanRemoval 적용)
        PostCategory updatedParent = postCategoryServiceImpl.findById(parentId);
        assertEquals(0, updatedParent.getChildren().size());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                postCategoryServiceImpl.findByName("Child Category"));

        assertEquals("Postcategory with name Child Category not found", exception.getMessage());
    }

    @Test
    @DisplayName("카테고리 삭제 테스트")
    void deleteCategory() {
        // Given: 새로운 카테고리 생성 및 저장
        PostCategory category = PostCategory.builder()
                .name("Sports")
                .build();

        Long savedId = postCategoryServiceImpl.save(category);

        em.flush();
        em.clear();

        // When: 카테고리 삭제
        Long deletedId = postCategoryServiceImpl.deleteById(savedId);

        // Then: 삭제된 카테고리가 더 이상 존재하지 않는지 확인
        assertEquals(savedId, deletedId);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                postCategoryServiceImpl.findById(savedId));

        assertEquals("PostCategory with id " + savedId + " not found", exception.getMessage());
    }
}