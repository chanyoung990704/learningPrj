package org.example.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.PostCategory;
import org.example.demo.dto.request.PostCategoryRequestDTO;
import org.example.demo.repository.PostCategoryRepository;
import org.example.demo.service.PostCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostCategoryServiceImpl implements PostCategoryService {

    private final PostCategoryRepository postCategoryRepository;


    @Override
    @Transactional
    public Long save(PostCategory object) {
        return postCategoryRepository.save(object).getId();
    }

    @Override
    @Transactional
    public void createCategory(PostCategoryRequestDTO requestDTO) {
        PostCategory parent = getParentCategory(requestDTO.getParentId());

        // 이미 중복된 카테고리인지 확인
        if (isDuplicatedCategory(parent, requestDTO.getName())) {
            throw new IllegalStateException("이미 " + requestDTO.getName() + " 카테고리가 존재합니다");
        }

        // 새로운 카테고리 생성 및 저장
        PostCategory newCategory = PostCategory.builder()
                .name(requestDTO.getName())
                .build();

        if (parent != null) {
            parent.addChild(newCategory);
        }

        save(newCategory);
    }

    @Override
    public PostCategory findById(Long id) {
        return postCategoryRepository.findById(id).orElseThrow(() ->
                new RuntimeException("PostCategory with id " + id + " not found"));
    }

    @Override
    public List<PostCategory> findAll() {
        return postCategoryRepository.findAll();
    }

    @Override
    public PostCategory findByName(String name) {
        return postCategoryRepository.findByName(name).orElseThrow(() ->
                new RuntimeException("Postcategory with name " + name + " not found"));
    }

    @Override
    @Transactional
    public Long deleteById(Long id) {
        if(!postCategoryRepository.existsById(id)) {
            throw new RuntimeException("PostCategory with id " + id + " not found");
        }
        postCategoryRepository.deleteById(id);
        return id;
    }
    // 사용 안함

    @Override
    @Transactional
    public Long update(Long id, PostCategory updatedCategory) {
        PostCategory existingCategory = findById(id);
        existingCategory.setName(updatedCategory.getName());
        return existingCategory.getId();
    }

    private PostCategory getParentCategory(Long parentId) {
        if (parentId == null) {
            return null;
        }
        // parentID가 존재하지 않는 경우 런타임 에러
        return findById(parentId);
    }

    private boolean isDuplicatedCategory(PostCategory parent, String categoryName) {
        if (parent == null) {
            return false; // 부모 카테고리가 없으면 중복 확인 필요 없음
        }
        return parent.getChildren()
                .stream().map(PostCategory::getName)
                .anyMatch(name -> name.equals(categoryName));
    }
}
