package com.poultry.platform.web;

import com.poultry.platform.dto.CategoryDtos;
import com.poultry.platform.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public List<CategoryDtos.CategoryDto> listActive() {
        return categoryService.listActive();
    }

    @GetMapping("/admin/categories")
    public List<CategoryDtos.CategoryDto> listAll() {
        return categoryService.listAll();
    }

    @PostMapping("/admin/categories")
    public CategoryDtos.CategoryDto create(@Valid @RequestBody CategoryDtos.CreateCategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/admin/categories/{id}")
    public CategoryDtos.CategoryDto update(@PathVariable Long id,
                                           @Valid @RequestBody CategoryDtos.UpdateCategoryRequest request) {
        return categoryService.update(id, request);
    }

    @PostMapping("/admin/categories/{id}/attributes")
    public CategoryDtos.AttributeDefDto addAttribute(@PathVariable Long id,
                                                     @Valid @RequestBody CategoryDtos.CreateAttributeRequest request) {
        return categoryService.addAttribute(id, request);
    }

    @PutMapping("/admin/categories/{categoryId}/attributes/{attrId}")
    public CategoryDtos.AttributeDefDto updateAttribute(@PathVariable Long categoryId,
                                                        @PathVariable Long attrId,
                                                        @Valid @RequestBody CategoryDtos.UpdateAttributeRequest request) {
        return categoryService.updateAttribute(categoryId, attrId, request);
    }
}
