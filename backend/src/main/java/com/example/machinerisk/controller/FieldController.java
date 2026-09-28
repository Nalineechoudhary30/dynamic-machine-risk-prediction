package com.example.machinerisk.controller;

import com.example.machinerisk.dto.FieldRequest;
import com.example.machinerisk.entity.MachineField;
import com.example.machinerisk.service.FieldService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/fields")
public class FieldController {
    private final FieldService service;
    public FieldController(FieldService service) { this.service = service; }
    @GetMapping public List<MachineField> getFields() { return service.findAll(); }
    @GetMapping("/{id}") public MachineField getField(@PathVariable Long id) { return service.findById(id); }
    @PostMapping public MachineField createField(@Valid @RequestBody FieldRequest request) { return service.create(request); }
    @PutMapping("/{id}") public MachineField updateField(@PathVariable Long id, @Valid @RequestBody FieldRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public void deleteField(@PathVariable Long id) { service.delete(id); }
}
