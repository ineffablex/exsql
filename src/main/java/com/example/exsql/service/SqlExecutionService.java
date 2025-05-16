package com.example.exsql.service;

import com.example.exsql.model.ScriptExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SqlExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(SqlExecutionService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ScriptExecutionResult> executeSqlScripts(MultipartFile[] files) {
        List<ScriptExecutionResult> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(executeSingleScript(file));
        }
        return results;
    }

    private ScriptExecutionResult executeSingleScript(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String sqlContent = null;
        try {
            // Read script content
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                sqlContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }

            if (sqlContent == null || sqlContent.trim().isEmpty()) {
                return new ScriptExecutionResult(fileName, false, "File is empty or could not be read.", "");
            }

            // Sanitize and prepare SQL content - remove potential Byte Order Mark (BOM)
            if (sqlContent.startsWith("\uFEFF")) {
                sqlContent = sqlContent.substring(1);
            }
            
            // Trim leading/trailing whitespace from the whole script
            sqlContent = sqlContent.trim();

            logger.info("Executing script: {}. Content length: {}", fileName, sqlContent.length());
            // logger.debug("Script content for {}:\n{}", fileName, sqlContent); // Be cautious with logging full SQL in production

            // Determine execution type (simple heuristic)
            boolean isPlSqlBlock = sqlContent.toUpperCase().contains("DECLARE") ||
                                   sqlContent.toUpperCase().contains("BEGIN") || 
                                   sqlContent.trim().endsWith("/");

            if (isPlSqlBlock) {
                logger.info("Detected PL/SQL block for file: {}", fileName);
                // For PL/SQL blocks, execute the entire content as one statement.
                // Oracle typically expects PL/SQL blocks to be executed as a whole.
                // If it ends with '/', it's a strong indicator for tools like SQL*Plus, but JDBC might not need it explicitly removed if the block is well-formed.
                // We'll execute it as is. If it contains multiple PL/SQL blocks separated by '/', this approach might need refinement.
                jdbcTemplate.execute(sqlContent);
                return new ScriptExecutionResult(fileName, true, "PL/SQL block executed successfully.", sqlContent);
            } else {
                logger.info("Detected plain SQL statements for file: {}", fileName);
                // For plain SQL, split by semicolon if multiple statements exist.
                // This is a basic split; more complex scenarios (e.g., semicolons in strings/comments) aren't handled here.
                // Also, some databases might have issues with empty statements after splitting.
                String[] statements = sqlContent.split(";(?=(?:[^']*'[^']*')*[^']*$)"); // Split by semicolon, ignoring those inside quotes
                
                int executedCount = 0;
                for (String stmt : statements) {
                    String trimmedStmt = stmt.trim();
                    if (!trimmedStmt.isEmpty()) {
                        try {
                           logger.debug("Executing statement: {}", trimmedStmt);
                           jdbcTemplate.execute(trimmedStmt);
                           executedCount++;
                        } catch (Exception e) {
                            logger.error("Error executing statement in file {}: {}\nStatement: {}", fileName, e.getMessage(), trimmedStmt, e);
                            // Return on first error within the script for simplicity
                            return new ScriptExecutionResult(fileName, false, "Error executing statement: " + e.getMessage() + "\nProblematic statement: " + trimmedStmt, sqlContent);
                        }
                    }
                }
                return new ScriptExecutionResult(fileName, true, executedCount + " plain SQL statement(s) executed successfully.", sqlContent);
            }
        } catch (Exception e) {
            logger.error("Failed to execute script: {}. Error: {}", fileName, e.getMessage(), e);
            return new ScriptExecutionResult(fileName, false, "Failed to execute script: " + e.getMessage(), sqlContent != null ? sqlContent : "Could not read content");
        }
    }
} 