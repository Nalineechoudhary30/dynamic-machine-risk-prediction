package com.example.machinerisk.service;

import com.example.machinerisk.entity.*;
import com.example.machinerisk.repository.MachineFieldRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private final MachineFieldRepository fields;
    public DataInitializer(MachineFieldRepository fields) { this.fields = fields; }
    @Override public void run(String... args) {
        if (fields.count() != 0) return;
        fields.saveAll(List.of(
            new MachineField("Machine Name", FieldType.TEXT, true, List.of()),
            new MachineField("Temperature", FieldType.NUMBER, true, List.of()),
            new MachineField("Pressure", FieldType.NUMBER, true, List.of()),
            new MachineField("Vibration", FieldType.DROPDOWN, true, List.of("Low", "Medium", "High"))
        ));
    }
}
