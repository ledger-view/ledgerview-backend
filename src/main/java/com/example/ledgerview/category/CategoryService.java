package com.example.ledgerview.category;

import com.example.ledgerview.transaction.TransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final TransactionRepository transactionRepository;
    private final ColorPaletteService colorPaletteService;

    public CategoryService(CategoryRepository repository,
                           TransactionRepository transactionRepository,
                           ColorPaletteService colorPaletteService) {
        this.repository = repository;
        this.transactionRepository = transactionRepository;
        this.colorPaletteService = colorPaletteService;
    }

    @Transactional(readOnly = true)
    public List<CategoryWithCount> list(UUID userId) {
        List<Category> categories = repository.findAllByUserIdOrderByName(userId);
        Map<UUID, Long> counts = transactionRepository.countGroupByCategoryId(userId)
                .stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
        return categories.stream()
                .map(c -> new CategoryWithCount(c, counts.getOrDefault(c.getId(), 0L)))
                .toList();
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
        try {
            repository.delete(category);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category is used by existing transactions");
        }
    }

    private void apply(Category category, CategoryController.CategoryRequest req) {
        category.setName(req.name());
        category.setColor(colorPaletteService.normalize(req.color()));
        category.setType(req.type());
    }
}
