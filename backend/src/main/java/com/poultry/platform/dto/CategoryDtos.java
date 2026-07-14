package com.poultry.platform.dto;

import com.poultry.platform.domain.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CategoryDtos {

    public record AttributeDefDto(
            Long id,
            String fieldKey,
            String label,
            AttributeDataType dataType,
            boolean required,
            List<String> enumOptions,
            String placeholder,
            int sortOrder,
            boolean matchable,
            boolean showInList,
            boolean showInNotify
    ) {}

    public record CategoryDto(
            Long id,
            String code,
            String name,
            String description,
            AllowedSides allowedSides,
            String defaultUnit,
            CategoryStatus status,
            int sortOrder,
            List<AttributeDefDto> attributes
    ) {}

    public record CreateCategoryRequest(
            @NotBlank String code,
            @NotBlank String name,
            String description,
            @NotNull AllowedSides allowedSides,
            String defaultUnit,
            Integer sortOrder
    ) {}

    public record UpdateCategoryRequest(
            @NotBlank String name,
            String description,
            @NotNull AllowedSides allowedSides,
            String defaultUnit,
            @NotNull CategoryStatus status,
            Integer sortOrder
    ) {}

    public record CreateAttributeRequest(
            @NotBlank String fieldKey,
            @NotBlank String label,
            @NotNull AttributeDataType dataType,
            boolean required,
            List<String> enumOptions,
            String placeholder,
            int sortOrder,
            boolean matchable,
            boolean showInList,
            boolean showInNotify
    ) {}

    public record UpdateAttributeRequest(
            @NotBlank String label,
            @NotNull AttributeDataType dataType,
            boolean required,
            List<String> enumOptions,
            String placeholder,
            int sortOrder,
            boolean matchable,
            boolean showInList,
            boolean showInNotify
    ) {}
}
