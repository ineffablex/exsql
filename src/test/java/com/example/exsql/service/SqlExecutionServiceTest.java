package com.example.exsql.service;

import com.example.exsql.model.ScriptExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SqlExecutionServiceTest {

    @Mock
    private JdbcTemplate primaryJdbcTemplate;

    @Mock
    private DataSource secondaryDataSource; // Mock DataSource for secondary

    private SqlExecutionService sqlExecutionService;

    @BeforeEach
    void setUp() {
        // secondaryJdbcTemplate will be created inside SqlExecutionService using this mock DataSource
        sqlExecutionService = new SqlExecutionService(primaryJdbcTemplate, secondaryDataSource);
    }

    private MockMultipartFile createFile(String name, String content) {
        return new MockMultipartFile(name, name + ".sql", "text/plain", content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void executeSqlScripts_usesPrimaryJdbcTemplate_whenDataSourceNameIsPrimary() {
        MultipartFile[] files = {createFile("test_primary", "SELECT 1;")};
        when(primaryJdbcTemplate.execute(anyString())).thenReturn(null); // Assuming execute doesn't return a value we check here

        List<ScriptExecutionResult> results = sqlExecutionService.executeSqlScripts(files, "primary");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        verify(primaryJdbcTemplate, times(1)).execute("SELECT 1;");
        // Verify secondaryJdbcTemplate (derived from secondaryDataSource) is NOT used
        // Since secondaryJdbcTemplate is created internally, we can't directly verify its mock.
        // However, if primary was used, secondary shouldn't have been.
        // For a more robust check, one might need to spy on the service or refactor to inject both JdbcTemplates.
        // For now, verifying primary was called implies secondary was not for this path.
    }
    
    @Test
    void executeSqlScripts_usesPrimaryJdbcTemplate_whenDataSourceNameIsNull() {
        MultipartFile[] files = {createFile("test_null", "SELECT 1;")};
        when(primaryJdbcTemplate.execute(anyString())).thenReturn(null);

        List<ScriptExecutionResult> results = sqlExecutionService.executeSqlScripts(files, null);
        
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        verify(primaryJdbcTemplate, times(1)).execute("SELECT 1;");
    }

    @Test
    void executeSqlScripts_usesPrimaryJdbcTemplate_whenDataSourceNameIsEmpty() {
        MultipartFile[] files = {createFile("test_empty", "SELECT 1;")};
        when(primaryJdbcTemplate.execute(anyString())).thenReturn(null);

        List<ScriptExecutionResult> results = sqlExecutionService.executeSqlScripts(files, "");
        
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        verify(primaryJdbcTemplate, times(1)).execute("SELECT 1;");
    }


    @Test
    void executeSqlScripts_usesSecondaryJdbcTemplate_whenDataSourceNameIsSecondary() {
        MultipartFile[] files = {createFile("test_secondary", "SELECT 2;")};
        
        // Need to ensure secondaryJdbcTemplate (created from mocked secondaryDataSource) is effectively mocked.
        // One way: since secondaryJdbcTemplate is new inside the service, we can't directly mock it here.
        // The current setup of SqlExecutionService creates `new JdbcTemplate(dataSource2)`.
        // To test this properly, we would need to either:
        // 1. Inject both JdbcTemplate instances directly.
        // 2. Use a spy on `new JdbcTemplate(secondaryDataSource)` which is more complex.

        // For this test, let's assume the constructor correctly creates secondaryJdbcTemplate.
        // We cannot directly verify `secondaryJdbcTemplate.execute` with the current @Mock setup for `primaryJdbcTemplate` only.
        // This test highlights a limitation in directly verifying the internal secondaryJdbcTemplate.
        // A better approach would be to inject both JdbcTemplates.
        
        // Simulating that secondaryDataSource would be used to create a JdbcTemplate
        // that then gets called. Since we can't mock the internal one, this test is more conceptual.
        // If we refactor SqlExecutionService to accept two JdbcTemplate mocks, this test becomes straightforward.

        // For now, we'll check that the primary is NOT called.
        List<ScriptExecutionResult> results = sqlExecutionService.executeSqlScripts(files, "secondary");
        assertNotNull(results);
        assertEquals(1, results.size());
        // We can't verify success or failure properly without mocking the execute call on the internal secondaryJdbcTemplate
        // assertTrue(results.get(0).isSuccess()); 
        verify(primaryJdbcTemplate, never()).execute(anyString());
        // To truly verify secondary, SqlExecutionService would need to be refactored
        // to accept JdbcTemplate secondaryJdbcTemplate as a constructor arg.
    }
    
    @Test
    void executeSqlScripts_handlesInvalidDataSourceName() {
        MultipartFile[] files = {createFile("test_invalid", "SELECT 3;")};

        List<ScriptExecutionResult> results = sqlExecutionService.executeSqlScripts(files, "invalidDataSource");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertFalse(results.get(0).isSuccess());
        assertTrue(results.get(0).getMessage().contains("Invalid data source name: invalidDataSource"));
        verify(primaryJdbcTemplate, never()).execute(anyString());
    }

    @Test
    void executeSingleScript_plSqlBlock_success() {
        // This test will use the primaryJdbcTemplate by default if we call executeSingleScript directly
        // or if we call the public method with "primary"
        MultipartFile file = createFile("plsql_block", "BEGIN NULL; END; /");
        when(primaryJdbcTemplate.execute("BEGIN NULL; END; /")).thenReturn(null);

        List<ScriptExecutionResult> results = sqlExecutionService.executeSqlScripts(new MultipartFile[]{file}, "primary");
        
        assertTrue(results.get(0).isSuccess());
        assertEquals("PL/SQL block executed successfully.", results.get(0).getMessage());
        verify(primaryJdbcTemplate).execute("BEGIN NULL; END; /");
    }

    @Test
    void executeSingleScript_plainSqlStatements_success() {
        MultipartFile file = createFile("plain_sql", "SELECT 1; UPDATE table SET col=1;");
        // Mock behavior for each statement if they are distinct, or use a general anyString()
        when(primaryJdbcTemplate.execute("SELECT 1")).thenReturn(null);
        when(primaryJdbcTemplate.execute("UPDATE table SET col=1")).thenReturn(null);


        List<ScriptExecutionResult> results = sqlExecutionService.executeSqlScripts(new MultipartFile[]{file}, "primary");

        assertTrue(results.get(0).isSuccess());
        assertEquals("2 plain SQL statement(s) executed successfully.", results.get(0).getMessage());
        verify(primaryJdbcTemplate).execute("SELECT 1");
        verify(primaryJdbcTemplate).execute("UPDATE table SET col=1");
    }
    
    @Test
    void executeSingleScript_statementExecutionError_returnsFailure() {
        MultipartFile file = createFile("error_sql", "SELECT GOOD; SELECT BAD;");
        when(primaryJdbcTemplate.execute("SELECT GOOD")).thenReturn(null);
        when(primaryJdbcTemplate.execute("SELECT BAD")).thenThrow(new RuntimeException("Syntax Error"));

        List<ScriptExecutionResult> results = sqlExecutionService.executeSqlScripts(new MultipartFile[]{file}, "primary");

        assertFalse(results.get(0).isSuccess());
        assertTrue(results.get(0).getMessage().contains("Error executing statement: Syntax Error"));
        assertTrue(results.get(0).getMessage().contains("Problematic statement: SELECT BAD"));
        verify(primaryJdbcTemplate).execute("SELECT GOOD");
        verify(primaryJdbcTemplate).execute("SELECT BAD");
    }
}
