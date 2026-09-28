package com.example.machinerisk.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "machine_values", uniqueConstraints = @UniqueConstraint(columnNames = {"machine_id", "field_id"}))
public class MachineValue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "machine_id", nullable = false) private Machine machine;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "field_id", nullable = false) private MachineField field;
    @Column(name = "value_text", columnDefinition = "TEXT") private String value;
    protected MachineValue() {}
    public MachineValue(MachineField field, String value) { this.field = field; this.value = value; }
    void setMachine(Machine machine) { this.machine = machine; }
    public MachineField getField() { return field; }
    public String getValue() { return value; }
}
