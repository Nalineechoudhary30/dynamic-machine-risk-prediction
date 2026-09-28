package com.example.machinerisk.service;

import com.example.machinerisk.dto.FieldRequest;
import com.example.machinerisk.entity.*;
import com.example.machinerisk.repository.MachineFieldRepository;
import com.example.machinerisk.repository.MachineValueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FieldService {
    private final MachineFieldRepository repository;
    private final MachineValueRepository values;
    public FieldService(MachineFieldRepository repository, MachineValueRepository values) { this.repository = repository; this.values = values; }
    public List<MachineField> findAll() { return repository.findAll(); }
    public MachineField findById(Long id) { return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Field " + id + " was not found")); }
    public MachineField create(FieldRequest request) {
        String name = request.fieldName().trim();
        if (repository.findByFieldNameIgnoreCase(name).isPresent()) throw new IllegalArgumentException("A field with this name already exists");
        List<String> options = request.dropdownOptions() == null ? List.of() : request.dropdownOptions().stream().map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
        if (request.fieldType() == FieldType.DROPDOWN && options.isEmpty()) throw new IllegalArgumentException("Dropdown fields need at least one option");
        if (request.fieldType() != FieldType.DROPDOWN && !options.isEmpty()) throw new IllegalArgumentException("Only dropdown fields can have options");
        return repository.save(new MachineField(name, request.fieldType(), request.required(), options));
    }
    @Transactional public MachineField update(Long id, FieldRequest request) {
        MachineField field = findById(id);
        String name = request.fieldName().trim();
        repository.findByFieldNameIgnoreCase(name).filter(other -> !other.getId().equals(id)).ifPresent(other -> { throw new IllegalArgumentException("A field with this name already exists"); });
        List<String> options = optionsFor(request);
        field.update(name, request.fieldType(), request.required(), options);
        return repository.save(field);
    }
    @Transactional public void delete(Long id) {
        MachineField field = findById(id);
        if (values.countByFieldId(id) > 0) throw new IllegalStateException("This field is used by machine records and cannot be deleted");
        repository.delete(field);
    }
    private List<String> optionsFor(FieldRequest request) {
        List<String> options = request.dropdownOptions() == null ? List.of() : request.dropdownOptions().stream().map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
        if (request.fieldType() == FieldType.DROPDOWN && options.isEmpty()) throw new IllegalArgumentException("Dropdown fields need at least one option");
        if (request.fieldType() != FieldType.DROPDOWN && !options.isEmpty()) throw new IllegalArgumentException("Only dropdown fields can have options");
        return options;
    }
}
