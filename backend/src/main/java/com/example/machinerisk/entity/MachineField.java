package com.example.machinerisk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "machine_fields")
public class MachineField {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "field_name", nullable = false, unique = true, length = 80) private String fieldName;
    @Enumerated(EnumType.STRING) @Column(name = "field_type", nullable = false, length = 20) private FieldType fieldType;
    @Column(nullable = false) private boolean required;
    @Column(name = "dropdown_options", columnDefinition = "TEXT") private String dropdownOptions;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    protected MachineField() {}
    public MachineField(String fieldName, FieldType fieldType, boolean required, List<String> options) {
        this.fieldName = fieldName; this.fieldType = fieldType; this.required = required;
        this.dropdownOptions = options == null ? "" : String.join(",", options);
    }
    public void update(String fieldName, FieldType fieldType, boolean required, List<String> options) {
        this.fieldName = fieldName; this.fieldType = fieldType; this.required = required;
        this.dropdownOptions = options == null ? "" : String.join(",", options);
    }
    public Long getId() { return id; }
    public String getFieldName() { return fieldName; }
    public FieldType getFieldType() { return fieldType; }
    public boolean isRequired() { return required; }
    public List<String> getDropdownOptions() { return dropdownOptions == null || dropdownOptions.isBlank() ? List.of() : Arrays.asList(dropdownOptions.split(",")); }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
