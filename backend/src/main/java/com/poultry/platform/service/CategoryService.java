package com.poultry.platform.service;

import com.poultry.platform.domain.*;
import com.poultry.platform.dto.CategoryDtos;
import com.poultry.platform.repository.CategoryAttributeDefRepository;
import com.poultry.platform.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryAttributeDefRepository attributeDefRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           CategoryAttributeDefRepository attributeDefRepository) {
        this.categoryRepository = categoryRepository;
        this.attributeDefRepository = attributeDefRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDtos.CategoryDto> listActive() {
        return categoryRepository.findByStatusOrderBySortOrderAsc(CategoryStatus.ACTIVE).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryDtos.CategoryDto> listAll() {
        return categoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDtos.CategoryDto get(Long id) {
        return toDto(categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 없습니다.")));
    }

    @Transactional
    public CategoryDtos.CategoryDto create(CategoryDtos.CreateCategoryRequest request) {
        String code = request.code().trim().toUpperCase();
        if (categoryRepository.existsByCode(code)) {
            throw new IllegalArgumentException("이미 존재하는 카테고리 코드입니다.");
        }
        Category category = new Category();
        category.setCode(code);
        category.setName(request.name());
        category.setDescription(request.description());
        category.setAllowedSides(request.allowedSides());
        category.setDefaultUnit(request.defaultUnit());
        category.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        category.setStatus(CategoryStatus.ACTIVE);
        categoryRepository.save(category);
        return toDto(category);
    }

    @Transactional
    public CategoryDtos.CategoryDto update(Long id, CategoryDtos.UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 없습니다."));
        category.setName(request.name());
        category.setDescription(request.description());
        category.setAllowedSides(request.allowedSides());
        category.setDefaultUnit(request.defaultUnit());
        category.setStatus(request.status());
        if (request.sortOrder() != null) {
            category.setSortOrder(request.sortOrder());
        }
        return toDto(category);
    }

    @Transactional
    public CategoryDtos.AttributeDefDto addAttribute(Long categoryId, CategoryDtos.CreateAttributeRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 없습니다."));
        CategoryAttributeDef def = new CategoryAttributeDef();
        def.setCategory(category);
        def.setFieldKey(request.fieldKey().trim());
        applyAttribute(def, request.label(), request.dataType(), request.required(), request.enumOptions(),
                request.placeholder(), request.sortOrder(), request.matchable(), request.showInList(), request.showInNotify());
        attributeDefRepository.save(def);
        return toAttrDto(def);
    }

    @Transactional
    public CategoryDtos.AttributeDefDto updateAttribute(Long categoryId, Long attrId, CategoryDtos.UpdateAttributeRequest request) {
        CategoryAttributeDef def = attributeDefRepository.findByIdAndCategoryId(attrId, categoryId)
                .orElseThrow(() -> new IllegalArgumentException("속성 정의가 없습니다."));
        applyAttribute(def, request.label(), request.dataType(), request.required(), request.enumOptions(),
                request.placeholder(), request.sortOrder(), request.matchable(), request.showInList(), request.showInNotify());
        return toAttrDto(def);
    }

    private void applyAttribute(CategoryAttributeDef def, String label, AttributeDataType dataType, boolean required,
                                List<String> enumOptions, String placeholder, int sortOrder,
                                boolean matchable, boolean showInList, boolean showInNotify) {
        def.setLabel(label);
        def.setDataType(dataType);
        def.setRequired(required);
        def.setEnumOptions(enumOptions != null ? enumOptions : List.of());
        def.setPlaceholder(placeholder);
        def.setSortOrder(sortOrder);
        def.setMatchable(matchable);
        def.setShowInList(showInList);
        def.setShowInNotify(showInNotify);
    }

    private CategoryDtos.CategoryDto toDto(Category category) {
        List<CategoryDtos.AttributeDefDto> attrs = attributeDefRepository
                .findByCategoryIdOrderBySortOrderAsc(category.getId())
                .stream().map(this::toAttrDto).toList();
        return new CategoryDtos.CategoryDto(
                category.getId(), category.getCode(), category.getName(), category.getDescription(),
                category.getAllowedSides(), category.getDefaultUnit(), category.getStatus(),
                category.getSortOrder(), attrs
        );
    }

    private CategoryDtos.AttributeDefDto toAttrDto(CategoryAttributeDef def) {
        return new CategoryDtos.AttributeDefDto(
                def.getId(), def.getFieldKey(), def.getLabel(), def.getDataType(), def.isRequired(),
                def.getEnumOptions(), def.getPlaceholder(), def.getSortOrder(),
                def.isMatchable(), def.isShowInList(), def.isShowInNotify()
        );
    }
}
