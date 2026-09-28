package com.example.machinerisk.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record MachineRequest(@NotNull Map<String, Object> values) {}
