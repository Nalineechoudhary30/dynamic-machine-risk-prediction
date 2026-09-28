package com.example.machinerisk.controller;

import com.example.machinerisk.dto.*;
import com.example.machinerisk.service.MachineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/machines")
public class MachineController {
    private final MachineService service;
    public MachineController(MachineService service) { this.service = service; }
    @GetMapping public List<MachineResponse> getMachines() { return service.findAll(); }
    @GetMapping("/{id}") public MachineResponse getMachine(@PathVariable Long id) { return service.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public MachineResponse create(@Valid @RequestBody MachineRequest request) { return service.create(request); }
    @PutMapping("/{id}") public MachineResponse update(@PathVariable Long id, @Valid @RequestBody MachineRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
    @PostMapping("/{id}/predict") public PredictionResponse predict(@PathVariable Long id) { return service.predict(id); }
}
