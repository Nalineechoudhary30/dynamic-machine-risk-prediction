package com.example.machinerisk.repository;

import com.example.machinerisk.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineRepository extends JpaRepository<Machine, Long> {}
