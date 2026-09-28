package com.example.machinerisk.dto;

import com.example.machinerisk.entity.FieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record FieldRequest(@NotBlank String fieldName, @NotNull FieldType fieldType, boolean required, List<String> dropdownOptions) {}
