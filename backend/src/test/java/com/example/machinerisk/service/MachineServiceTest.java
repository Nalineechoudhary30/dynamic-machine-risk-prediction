package com.example.machinerisk.service;

import com.example.machinerisk.dto.MachineRequest;
import com.example.machinerisk.entity.*;
import com.example.machinerisk.repository.MachineFieldRepository;
import com.example.machinerisk.repository.MachineRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MachineServiceTest {
    private final MachineRepository machines = mock(MachineRepository.class);
    private final MachineFieldRepository fields = mock(MachineFieldRepository.class);
    private final MachineService service = new MachineService(machines, fields, mock(RestClient.class));
    private final List<MachineField> definitions = List.of(
        new MachineField("Machine Name", FieldType.TEXT, true, List.of()),
        new MachineField("Temperature", FieldType.NUMBER, true, List.of()),
        new MachineField("Pressure", FieldType.NUMBER, true, List.of()),
        new MachineField("Vibration", FieldType.DROPDOWN, true, List.of("Low", "Medium", "High"))
    );

    @Test void createsMachineAndKeepsDynamicValues() {
        when(fields.findAll()).thenReturn(definitions);
        when(machines.save(any())).thenAnswer(call -> call.getArgument(0));
        Map<String,Object> input = Map.of("Machine Name", "CNC-001", "Temperature", "85", "Pressure", "120", "Vibration", "High");
        var result = service.create(new MachineRequest(input));
        assertEquals("CNC-001", result.machineName());
        assertEquals(85.0, result.values().get("Temperature"));
        assertEquals("High", result.values().get("Vibration"));
    }

    @Test void rejectsMissingRequiredValues() {
        when(fields.findAll()).thenReturn(definitions);
        Map<String,Object> input = Map.of("Machine Name", "CNC-001", "Pressure", 120, "Vibration", "High");
        assertEquals("Temperature is required", assertThrows(IllegalArgumentException.class, () -> service.create(new MachineRequest(input))).getMessage());
        verify(machines, never()).save(any());
    }

    @Test void retrievesMachineValues() {
        when(fields.findAll()).thenReturn(definitions);
        Machine machine = new Machine();
        machine.replaceValues(List.of(new MachineValue(definitions.get(0), "CNC-002"), new MachineValue(definitions.get(1), "60")));
        when(machines.findById(7L)).thenReturn(Optional.of(machine));
        var result = service.findById(7L);
        assertEquals("CNC-002", result.machineName());
        assertEquals(60.0, result.values().get("Temperature"));
    }
}
