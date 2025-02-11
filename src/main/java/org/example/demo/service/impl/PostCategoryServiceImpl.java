package org.example.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.PostCategory;
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
    public PostCategory findById(Long id) {
        return postCategoryRepository.findById(id).orElseThrow(() ->
                new RuntimeException("PostCategory with id " + id + " not found"));
    }

    @Override
    public List<PostCategory> findAll() {
        return postCategoryRepository.findAll();
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

    @Override
    public PostCategory findByName(String name) {
        return postCategoryRepository.findByName(name).orElseThrow(() ->
                new RuntimeException("Postcategory with name " + name + " not found"));
    }
}
