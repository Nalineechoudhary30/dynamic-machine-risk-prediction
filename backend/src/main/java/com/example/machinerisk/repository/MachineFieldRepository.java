package com.example.machinerisk.repository;

import com.example.machinerisk.entity.MachineField;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MachineFieldRepository extends JpaRepository<MachineField, Long> {
    Optional<MachineField> findByFieldNameIgnoreCase(String fieldName);
}
