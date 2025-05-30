package com.example.exsql.service;

import com.example.exsql.model.DataSourceDefinition;
import com.example.exsql.model.ScriptExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SqlExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(SqlExecutionService.class);

    private final Map<String, DataSourceDefinition> dataSourceDefinitions;
    private final CustomPasswordRetrievalService passwordRetrievalService;

    @Autowired
    public SqlExecutionService(Map<String, DataSourceDefinition> dataSourceDefinitions,
                               CustomPasswordRetrievalService passwordRetrievalService) {
        this.dataSourceDefinitions = dataSourceDefinitions;
        this.passwordRetrievalService = passwordRetrievalService;
    }

    public List<ScriptExecutionResult> executeSqlScripts(MultipartFile[] files, String dataSourceName) {
        List<ScriptExecutionResult> results = new ArrayList<>();
        
        String effectiveDataSourceName = (dataSourceName == null || dataSourceName.trim().isEmpty()) ? "primary" : dataSourceName.toLowerCase();
        logger.info("Attempting to execute scripts on data source: {}", effectiveDataSourceName);

        DataSourceDefinition definition = dataSourceDefinitions.get(effectiveDataSourceName);

        if (definition == null) {
            logger.error("Data source definition not found for name: {}", effectiveDataSourceName);
            for (MultipartFile file : files) {
                results.add(new ScriptExecutionResult(file.getOriginalFilename(), false, "Data source definition not found: " + effectiveDataSourceName, ""));
            }
            return results;
        }

        logger.info("Found data source definition for: {}", definition.getName());
        String password;
        try {
            if ("primary".equals(definition.getName())) {
                 logger.info("Retrieving password for primary data source using its specific CNLDB/HTTP mechanism.");
                 // This relies on CustomPasswordRetrievalService.retrievePrimaryPassword using its own @Value injected CNLDB params.
                 // If primary also had HTTP password definition, that logic would be inside retrievePrimaryPassword or called from here.
                password = passwordRetrievalService.retrievePrimaryPassword(definition.getUsername());
            } else {
                logger.info("Retrieving password for data source '{}' using CNLDB parameters from its definition.", definition.getName());
                password = passwordRetrievalService.retrievePassword(
                        definition.getUsername(),
                        definition.getCndlNdbType(),
                        definition.getCndlTns(),
                        definition.getCndlGetPasswdUrl(),
                        definition.getCndlGetPasswdDummyPassword(),
                        definition.getName()
                );
            }
            if (!StringUtils.hasText(password)) {
                 logger.error("Retrieved password was empty for data source: {}", definition.getName());
                 throw new RuntimeException("Retrieved password was empty for data source " + definition.getName() + ".");
            }
            logger.info("Password retrieved successfully for data source: {}", definition.getName());
        } catch (Exception e) {
            logger.error("Failed to retrieve password for data source {}: {}", definition.getName(), e.getMessage(), e);
            for (MultipartFile file : files) {
                results.add(new ScriptExecutionResult(file.getOriginalFilename(), false, "Failed to retrieve password for data source " + definition.getName() + ": " + e.getMessage(), ""));
            }
            return results;
        }

        DataSource dynamicDataSource;
        try {
            dynamicDataSource = DataSourceBuilder.create()
                    .url(definition.getUrl())
                    .username(definition.getUsername())
                    .password(password)
                    .driverClassName(definition.getDriverClassName())
                    .build();
            logger.info("Dynamically created DataSource for: {}", definition.getName());
        } catch (Exception e) {
            logger.error("Failed to create DataSource for {}: {}", definition.getName(), e.getMessage(), e);
            for (MultipartFile file : files) {
                results.add(new ScriptExecutionResult(file.getOriginalFilename(), false, "Failed to create DataSource for " + definition.getName() + ": " + e.getMessage(), ""));
            }
            return results;
        }
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dynamicDataSource);
        logger.info("JdbcTemplate created for data source: {}", definition.getName());

        for (MultipartFile file : files) {
            results.add(executeSingleScript(file, jdbcTemplate, definition.getName()));
        }
        return results;
    }

    private ScriptExecutionResult executeSingleScript(MultipartFile file, JdbcTemplate jdbcTemplate, String dataSourceName) {
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

            logger.info("Executing script: {} on data source: {}. Content length: {}", fileName, dataSourceName, sqlContent.length());

            // Determine execution type (simple heuristic)
            boolean isPlSqlBlock = sqlContent.toUpperCase().contains("DECLARE") ||
                                   sqlContent.toUpperCase().contains("BEGIN") || 
                                   sqlContent.trim().endsWith("/");

            if (isPlSqlBlock) {
                logger.info("Detected PL/SQL block for file: {} on data source: {}", fileName, dataSourceName);
                jdbcTemplate.execute(sqlContent);
                return new ScriptExecutionResult(fileName, true, "PL/SQL block executed successfully on " + dataSourceName + ".", sqlContent);
            } else {
                logger.info("Detected plain SQL statements for file: {} on data source: {}", fileName, dataSourceName);
                String[] statements = sqlContent.split(";(?=(?:[^']*'[^']*')*[^']*$)"); // Split by semicolon, ignoring those inside quotes
                
                int executedCount = 0;
                for (String stmt : statements) {
                    String trimmedStmt = stmt.trim();
                    if (!trimmedStmt.isEmpty()) {
                        try {
                           logger.debug("Executing statement on {}: {}", dataSourceName, trimmedStmt);
                           jdbcTemplate.execute(trimmedStmt);
                           executedCount++;
                        } catch (Exception e) {
                            logger.error("Error executing statement in file {} on data source {}: {}\nStatement: {}", fileName, dataSourceName, e.getMessage(), trimmedStmt, e);
                            return new ScriptExecutionResult(fileName, false, "Error on " + dataSourceName + ": " + e.getMessage() + "\nProblematic statement: " + trimmedStmt, sqlContent);
                        }
                    }
                }
                return new ScriptExecutionResult(fileName, true, executedCount + " plain SQL statement(s) executed successfully on " + dataSourceName + ".", sqlContent);
            }
        } catch (Exception e) {
            logger.error("Failed to execute script: {} on data source: {}. Error: {}", fileName, dataSourceName, e.getMessage(), e);
            return new ScriptExecutionResult(fileName, false, "Failed to execute script on " + dataSourceName + ": " + e.getMessage(), sqlContent != null ? sqlContent : "Could not read content");
        }
    }
} 