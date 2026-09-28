package com.example.machinerisk.service;

import com.example.machinerisk.dto.FieldRequest;
import com.example.machinerisk.entity.FieldType;
import com.example.machinerisk.entity.MachineField;
import com.example.machinerisk.repository.MachineFieldRepository;
import com.example.machinerisk.repository.MachineValueRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldServiceTest {
    private final MachineFieldRepository fields = mock(MachineFieldRepository.class);
    private final MachineValueRepository values = mock(MachineValueRepository.class);
    private final FieldService service = new FieldService(fields, values);

    @Test void createsAnOptionalNumberField() {
        when(fields.findByFieldNameIgnoreCase("Humidity")).thenReturn(Optional.empty());
        when(fields.save(any())).thenAnswer(call -> call.getArgument(0));
        MachineField created = service.create(new FieldRequest("Humidity", FieldType.NUMBER, false, List.of()));
        assertEquals("Humidity", created.getFieldName());
        assertEquals(FieldType.NUMBER, created.getFieldType());
        assertFalse(created.isRequired());
    }

    @Test void requiresOptionsForDropdownFields() {
        when(fields.findByFieldNameIgnoreCase("State")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.create(new FieldRequest("State", FieldType.DROPDOWN, true, List.of())));
        verify(fields, never()).save(any());
    }

    @Test void retrievesConfiguredFields() {
        MachineField humidity = new MachineField("Humidity", FieldType.NUMBER, false, List.of());
        when(fields.findAll()).thenReturn(List.of(humidity));
        assertEquals("Humidity", service.findAll().get(0).getFieldName());
    }
}
