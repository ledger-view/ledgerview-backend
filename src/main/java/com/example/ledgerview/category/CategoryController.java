package com.example.ledgerview.category;

import com.example.ledgerview.security.user.AuthenticatedUser;
import com.example.ledgerview.util.ApiPaths;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.CATEGORIES)
public class CategoryController {

    private final CategoryService service;
    private final ColorPaletteService colorPaletteService;

    public CategoryController(CategoryService service, ColorPaletteService colorPaletteService) {
        this.service = service;
        this.colorPaletteService = colorPaletteService;
    }

    @GetMapping("/palette")
    public ColorPaletteProperties colors() {
        return colorPaletteService.getProperties();
    }

    @GetMapping
    public List<CategoryResponse> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.list(user.id()).stream().map(CategoryResponse::fromCategoryWithCount).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@RequestBody @Valid CategoryRequest req,
                                   @AuthenticationPrincipal AuthenticatedUser user) {
        return new CategoryResponse(service.create(user.id(), req), 0L);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id,
                                   @RequestBody @Valid CategoryRequest req,
                                   @AuthenticationPrincipal AuthenticatedUser user) {
        return new CategoryResponse(service.update(user.id(), id, req), 0L);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser user) {
        service.delete(user.id(), id);
    }

    record CategoryRequest(@NotBlank String name, @NotBlank String color, @NotNull CategoryType type) {
    }

    record CategoryResponse(UUID id, String name, String color, CategoryType type, long transactionCount) {
        CategoryResponse(Category c, long transactionCount) {
            this(c.getId(), c.getName(), c.getColor(), c.getType(), transactionCount);
        }

        static CategoryResponse fromCategoryWithCount(CategoryWithCount cwc) {
            return new CategoryResponse(cwc.category(), cwc.transactionCount());
        }
    }
}
