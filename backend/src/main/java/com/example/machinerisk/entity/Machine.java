package com.example.machinerisk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "machines")
public class Machine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MachineValue> values = new ArrayList<>();
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();
    public Machine() {}
    public void replaceValues(List<MachineValue> newValues) { values.clear(); newValues.forEach(this::addValue); updatedAt = LocalDateTime.now(); }
    public void addValue(MachineValue value) { values.add(value); value.setMachine(this); }
    public Long getId() { return id; }
    public String getMachineName() { return values.stream().filter(v -> v.getField().getFieldName().equalsIgnoreCase("Machine Name")).map(MachineValue::getValue).findFirst().orElse("Unnamed machine"); }
    public List<MachineValue> getValues() { return values; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
