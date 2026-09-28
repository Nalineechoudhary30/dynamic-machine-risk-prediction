package com.example.machinerisk.service;

import com.example.machinerisk.dto.*;
import com.example.machinerisk.entity.*;
import com.example.machinerisk.repository.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.util.*;

@Service
public class MachineService {
    private final MachineRepository machines;
    private final MachineFieldRepository fields;
    private final RestClient mlClient;
    public MachineService(MachineRepository machines, MachineFieldRepository fields, RestClient mlClient) {
        this.machines = machines; this.fields = fields; this.mlClient = mlClient;
    }
    public List<MachineResponse> findAll() { return machines.findAll().stream().map(this::toResponse).toList(); }
    public MachineResponse findById(Long id) { return toResponse(getMachine(id)); }
    @Transactional public MachineResponse create(MachineRequest request) {
        requireMachineName(request.values());
        Machine machine = new Machine();
        machine.replaceValues(validateAndMap(request.values()));
        return toResponse(machines.save(machine));
    }
    @Transactional public MachineResponse update(Long id, MachineRequest request) {
        Machine machine = getMachine(id);
        requireMachineName(request.values());
        machine.replaceValues(validateAndMap(request.values()));
        return toResponse(machines.save(machine));
    }
    public void delete(Long id) { machines.delete(getMachine(id)); }
    public PredictionResponse predict(Long id) {
        Machine machine = getMachine(id);
        Map<String, String> values = new HashMap<>();
        machine.getValues().forEach(v -> values.put(v.getField().getFieldName().toLowerCase(Locale.ROOT), v.getValue()));
        Map<String, Object> body = Map.of("temperature", number(values, "temperature"), "pressure", number(values, "pressure"), "vibration", requiredValue(values, "vibration"));
        try {
            Map<?, ?> response = mlClient.post().uri("/predict").contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);
            if (response == null || response.get("risk") == null) throw new IllegalStateException("ML service returned an empty response");
            return new PredictionResponse(id, machine.getMachineName(), response.get("risk").toString());
        } catch (RestClientException e) { throw new MlServiceUnavailableException("Could not reach the local ML service. Start it on port 8000."); }
    }
    private List<MachineValue> validateAndMap(Map<String, Object> submitted) {
        Map<String,Object> input = new HashMap<>(); submitted.forEach((k,v) -> input.put(k.toLowerCase(Locale.ROOT), v));
        List<MachineField> definitions = fields.findAll();
        if (definitions.isEmpty()) throw new IllegalStateException("Configure at least one machine field first");
        List<MachineValue> result = new ArrayList<>();
        for (MachineField field : definitions) {
            Object raw = input.containsKey(field.getFieldName().toLowerCase(Locale.ROOT))
                ? input.get(field.getFieldName().toLowerCase(Locale.ROOT)) : input.get(String.valueOf(field.getId()));
            String value = raw == null ? "" : raw.toString().trim();
            if (field.isRequired() && value.isBlank()) throw new IllegalArgumentException(field.getFieldName() + " is required");
            if (value.isBlank()) continue;
            if (field.getFieldType() == FieldType.NUMBER) {
                try { if (!Double.isFinite(Double.parseDouble(value))) throw new NumberFormatException(); }
                catch (NumberFormatException e) { throw new IllegalArgumentException(field.getFieldName() + " must be a number"); }
            }
            if (field.getFieldType() == FieldType.DROPDOWN && field.getDropdownOptions().stream().noneMatch(o -> o.equalsIgnoreCase(value)))
                throw new IllegalArgumentException("Invalid option for " + field.getFieldName());
            result.add(new MachineValue(field, value));
        }
        if (definitions.stream().noneMatch(f -> f.getFieldName().equalsIgnoreCase("Machine Name"))) throw new IllegalStateException("Machine Name field is missing");
        return result;
    }
    private String requireMachineName(Map<String, Object> values) {
        MachineField nameField = fields.findByFieldNameIgnoreCase("Machine Name").orElse(null);
        Object name = values.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase("Machine Name") || (nameField != null && e.getKey().equals(String.valueOf(nameField.getId())))).map(Map.Entry::getValue).findFirst().orElse(null);
        if (name == null || name.toString().isBlank()) throw new IllegalArgumentException("Machine Name is required");
        return name.toString().trim();
    }
    private double number(Map<String,String> values, String key) { try { return Double.parseDouble(requiredValue(values,key)); } catch (NumberFormatException ex) { throw new IllegalArgumentException(key + " must be a number for prediction"); } }
    private String requiredValue(Map<String,String> values, String key) { String v=values.get(key); if (v == null || v.isBlank()) throw new IllegalArgumentException("Machine is missing " + key + " required for prediction"); return v; }
    private Machine getMachine(Long id) { return machines.findById(id).orElseThrow(() -> new NoSuchElementException("Machine " + id + " was not found")); }
    private MachineResponse toResponse(Machine machine) {
        Map<String,Object> values = new LinkedHashMap<>(); values.put("Machine Name", machine.getMachineName());
        machine.getValues().forEach(v -> values.put(v.getField().getFieldName(), v.getField().getFieldType() == FieldType.NUMBER ? parseNumber(v.getValue()) : v.getValue()));
        return new MachineResponse(machine.getId(), machine.getMachineName(), values, machine.getCreatedAt(), machine.getUpdatedAt());
    }
    private Object parseNumber(String value) { try { return Double.valueOf(value); } catch (NumberFormatException ex) { return value; } }
    public static class MlServiceUnavailableException extends RuntimeException { public MlServiceUnavailableException(String message) { super(message); } }
}
