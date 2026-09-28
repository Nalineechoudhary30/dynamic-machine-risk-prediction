package com.example.machinerisk.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record MachineResponse(Long id, String machineName, Map<String, Object> values, LocalDateTime createdAt, LocalDateTime updatedAt) {}
