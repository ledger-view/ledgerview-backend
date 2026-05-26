package com.example.ledgerview.category;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Category> list(UUID userId) {
        return repository.findAllByUserIdOrderByName(userId);
    }

    @Transactional
    public Category create(UUID userId, CategoryController.CategoryRequest req) {
        Category category = new Category();
        category.setUserId(userId);
        apply(category, req);
        return repository.save(category);
    }

    @Transactional
    public Category update(UUID userId, UUID id, CategoryController.CategoryRequest req) {
        Category category = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        apply(category, req);
        return repository.save(category);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Category category = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        repository.delete(category);
    }

    private void apply(Category category, CategoryController.CategoryRequest req) {
        category.setName(req.name());
        category.setColor(req.color());
        category.setType(req.type());
    }
}
