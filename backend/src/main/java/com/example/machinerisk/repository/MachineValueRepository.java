package com.example.machinerisk.repository;

import com.example.machinerisk.entity.MachineValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineValueRepository extends JpaRepository<MachineValue, Long> {
    long countByFieldId(Long fieldId);
}
