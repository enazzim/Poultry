package com.poultry.platform.service;

import com.poultry.platform.domain.*;
import com.poultry.platform.repository.CategoryAttributeDefRepository;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class SchemaValidator {

    private final CategoryAttributeDefRepository attributeDefRepository;

    public SchemaValidator(CategoryAttributeDefRepository attributeDefRepository) {
        this.attributeDefRepository = attributeDefRepository;
    }

    public void validate(Long categoryId, Map<String, Object> attributes) {
        List<CategoryAttributeDef> defs = attributeDefRepository.findByCategoryIdOrderBySortOrderAsc(categoryId);
        Map<String, Object> attrs = attributes != null ? attributes : Map.of();

        for (CategoryAttributeDef def : defs) {
            Object value = attrs.get(def.getFieldKey());
            boolean missing = value == null || (value instanceof String s && s.isBlank());
            if (def.isRequired() && missing) {
                throw new IllegalArgumentException("필수 속성 누락: " + def.getLabel());
            }
            if (missing) {
                continue;
            }
            switch (def.getDataType()) {
                case STRING -> {
                    if (!(value instanceof String)) {
                        throw new IllegalArgumentException(def.getLabel() + "은(는) 문자열이어야 합니다.");
                    }
                }
                case NUMBER -> {
                    if (!(value instanceof Number) && !isNumericString(value)) {
                        throw new IllegalArgumentException(def.getLabel() + "은(는) 숫자여야 합니다.");
                    }
                }
                case BOOLEAN -> {
                    if (!(value instanceof Boolean) && !(value instanceof String s && ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)))) {
                        throw new IllegalArgumentException(def.getLabel() + "은(는) 불리언이어야 합니다.");
                    }
                }
                case DATE -> {
                    try {
                        LocalDate.parse(String.valueOf(value));
                    } catch (Exception e) {
                        throw new IllegalArgumentException(def.getLabel() + "은(는) YYYY-MM-DD 형식이어야 합니다.");
                    }
                }
                case ENUM -> {
                    String str = String.valueOf(value);
                    if (def.getEnumOptions() == null || !def.getEnumOptions().contains(str)) {
                        throw new IllegalArgumentException(def.getLabel() + " 값이 허용 목록에 없습니다.");
                    }
                }
            }
        }
    }

    public void validateSide(Category category, ListingSide side) {
        AllowedSides allowed = category.getAllowedSides();
        if (allowed == AllowedSides.BOTH) {
            return;
        }
        if (allowed == AllowedSides.OFFER && side != ListingSide.OFFER) {
            throw new IllegalArgumentException("이 카테고리는 OFFER만 허용합니다.");
        }
        if (allowed == AllowedSides.NEED && side != ListingSide.NEED) {
            throw new IllegalArgumentException("이 카테고리는 NEED만 허용합니다.");
        }
    }

    private boolean isNumericString(Object value) {
        try {
            new BigDecimal(String.valueOf(value));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
