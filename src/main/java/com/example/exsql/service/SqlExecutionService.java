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

    private final JdbcTemplate primaryJdbcTemplate;
    private final JdbcTemplate secondaryJdbcTemplate;

    @Autowired
    public SqlExecutionService(JdbcTemplate jdbcTemplate,
                               @org.springframework.beans.factory.annotation.Qualifier("dataSource2") javax.sql.DataSource dataSource2) {
        this.primaryJdbcTemplate = jdbcTemplate; // This is the default JdbcTemplate from the primary DataSource
        this.secondaryJdbcTemplate = new JdbcTemplate(dataSource2);
    }

    public List<ScriptExecutionResult> executeSqlScripts(MultipartFile[] files, String dataSourceName) {
        List<ScriptExecutionResult> results = new ArrayList<>();
        JdbcTemplate selectedJdbcTemplate;

        if ("secondary".equalsIgnoreCase(dataSourceName)) {
            selectedJdbcTemplate = secondaryJdbcTemplate;
            logger.info("Executing scripts on secondary data source.");
        } else if ("primary".equalsIgnoreCase(dataSourceName) || dataSourceName == null || dataSourceName.isEmpty()) {
            selectedJdbcTemplate = primaryJdbcTemplate;
            logger.info("Executing scripts on primary data source.");
        } else {
            logger.error("Invalid data source name provided: {}", dataSourceName);
            // Handle invalid data source name, perhaps by returning an error for all files
            // or throwing an IllegalArgumentException. For now, log and skip execution.
            for (MultipartFile file : files) {
                results.add(new ScriptExecutionResult(file.getOriginalFilename(), false, "Invalid data source name: " + dataSourceName, ""));
            }
            return results;
        }

        for (MultipartFile file : files) {
            results.add(executeSingleScript(file, selectedJdbcTemplate));
        }
        return results;
    }

    private ScriptExecutionResult executeSingleScript(MultipartFile file, JdbcTemplate jdbcTemplate) {
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

            logger.info("Executing script: {} using {}. Content length: {}", fileName, 
                (jdbcTemplate == primaryJdbcTemplate ? "primary" : "secondary") + " JdbcTemplate", 
                sqlContent.length());

            // Determine execution type (simple heuristic)
            boolean isPlSqlBlock = sqlContent.toUpperCase().contains("DECLARE") ||
                                   sqlContent.toUpperCase().contains("BEGIN") || 
                                   sqlContent.trim().endsWith("/");

            if (isPlSqlBlock) {
                logger.info("Detected PL/SQL block for file: {}", fileName);
                jdbcTemplate.execute(sqlContent);
                return new ScriptExecutionResult(fileName, true, "PL/SQL block executed successfully.", sqlContent);
            } else {
                logger.info("Detected plain SQL statements for file: {}", fileName);
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