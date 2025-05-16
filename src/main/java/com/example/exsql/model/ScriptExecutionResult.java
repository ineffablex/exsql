package com.example.exsql.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptExecutionResult {
    private String fileName;
    private boolean success;
    private String message;
    private String sqlExecuted; // Optional: to show what was executed
} 