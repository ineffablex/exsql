package com.example.exsql.service;

import static org.junit.jupiter.api.Assertions.*;

import com.newland.computer.boss.bossbiz.bosscomponent.cachestore.datasource.CNLDBConnectMgr;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class CustomPasswordRetrievalServiceTest {

  @InjectMocks private CustomPasswordRetrievalService customPasswordRetrievalService;

  private MockedStatic<CNLDBConnectMgr> mockedCNLDBConnectMgr;

  @BeforeEach
  void setUp() {
    // Mock static CNLDBConnectMgr methods
    mockedCNLDBConnectMgr = Mockito.mockStatic(CNLDBConnectMgr.class);

    // Initialize the service, which calls CNLDBConnectMgr.init() via @PostConstruct
    // We need to set the private fields that would normally be injected by @Value
    ReflectionTestUtils.setField(
        customPasswordRetrievalService, "cndlAuthFilePath", "test-auth-file-path");
    ReflectionTestUtils.setField(customPasswordRetrievalService, "primaryCndlNDbType", 1);
    ReflectionTestUtils.setField(customPasswordRetrievalService, "primaryCndlTns", "primaryTns");
    ReflectionTestUtils.setField(
        customPasswordRetrievalService, "primaryCndlGetPasswdUrl", "primaryUrl");
    ReflectionTestUtils.setField(
        customPasswordRetrievalService, "primaryCndlGetPasswdDummyPassword", "primaryDummy");

    // Call initialize manually as @PostConstruct might not run in this unit test setup without
    // Spring context
    // Or, ensure cndlMgrInitialized is true after mocking init
    mockedCNLDBConnectMgr
        .when(CNLDBConnectMgr::init)
        .thenAnswer(invocation -> null); // Make init() do nothing
    customPasswordRetrievalService.initialize(); // Manually call to set cndlMgrInitialized = true
  }

  @AfterEach
  void tearDown() {
    mockedCNLDBConnectMgr.close();
  }

  @Test
  void retrievePrimaryPassword_success() throws Exception {
    String expectedPassword = "primaryTestPassword";
    String username = "primaryUser";

    mockedCNLDBConnectMgr
        .when(
            () ->
                CNLDBConnectMgr.getPasswd(1, "primaryTns", username, "primaryUrl", "primaryDummy"))
        .thenReturn(expectedPassword);

    String actualPassword = customPasswordRetrievalService.retrievePrimaryPassword(username);

    assertEquals(expectedPassword, actualPassword);
    mockedCNLDBConnectMgr.verify(
        () -> CNLDBConnectMgr.getPasswd(1, "primaryTns", username, "primaryUrl", "primaryDummy"));
  }

  @Test
  void retrievePrimaryPassword_CNLDBReturnsNull_throwsRuntimeException() {
    String username = "primaryUserNull";
    mockedCNLDBConnectMgr
        .when(
            () ->
                CNLDBConnectMgr.getPasswd(1, "primaryTns", username, "primaryUrl", "primaryDummy"))
        .thenReturn(null);

    Exception exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              customPasswordRetrievalService.retrievePrimaryPassword(username);
            });
    assertTrue(exception.getMessage().contains("CNLDBConnectMgr returned null for primary user"));
  }

  @Test
  void retrievePrimaryPassword_CNLDBThrowsException_rethrowsRuntimeException() {
    String username = "primaryUserError";
    mockedCNLDBConnectMgr
        .when(
            () ->
                CNLDBConnectMgr.getPasswd(1, "primaryTns", username, "primaryUrl", "primaryDummy"))
        .thenThrow(new Exception("CNLDB Error"));

    Exception exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              customPasswordRetrievalService.retrievePrimaryPassword(username);
            });
    assertTrue(
        exception
            .getMessage()
            .contains("Error retrieving password for primary datasource via CNLDBConnectMgr"));
  }

  @Test
  void retrievePassword_secondaryDatasource_success() throws Exception {
    String expectedPassword = "secondaryTestPassword";
    String username = "secondaryUser";
    int nDbType = 2;
    String tns = "secondaryTns";
    String url = "secondaryUrl";
    String dummy = "secondaryDummy";
    String dsName = "secondary";

    mockedCNLDBConnectMgr
        .when(() -> CNLDBConnectMgr.getPasswd(nDbType, tns, username, url, dummy))
        .thenReturn(expectedPassword);

    String actualPassword =
        customPasswordRetrievalService.retrievePassword(username, nDbType, tns, url, dummy, dsName);

    assertEquals(expectedPassword, actualPassword);
    mockedCNLDBConnectMgr.verify(
        () -> CNLDBConnectMgr.getPasswd(nDbType, tns, username, url, dummy));
  }

  @Test
  void retrievePassword_secondaryDatasource_CNLDBReturnsNull_throwsRuntimeException() {
    String username = "secondaryUserNull";
    int nDbType = 2;
    String tns = "secondaryTns";
    String url = "secondaryUrl";
    String dummy = "secondaryDummy";
    String dsName = "secondary";

    mockedCNLDBConnectMgr
        .when(() -> CNLDBConnectMgr.getPasswd(nDbType, tns, username, url, dummy))
        .thenReturn(null);

    Exception exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              customPasswordRetrievalService.retrievePassword(
                  username, nDbType, tns, url, dummy, dsName);
            });
    assertTrue(exception.getMessage().contains("CNLDBConnectMgr returned null for secondary user"));
  }

  @Test
  void retrievePassword_secondaryDatasource_CNLDBThrowsException_rethrowsRuntimeException() {
    String username = "secondaryUserError";
    int nDbType = 2;
    String tns = "secondaryTns";
    String url = "secondaryUrl";
    String dummy = "secondaryDummy";
    String dsName = "secondary";

    mockedCNLDBConnectMgr
        .when(() -> CNLDBConnectMgr.getPasswd(nDbType, tns, username, url, dummy))
        .thenThrow(new Exception("CNLDB Error"));

    Exception exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              customPasswordRetrievalService.retrievePassword(
                  username, nDbType, tns, url, dummy, dsName);
            });
    assertTrue(
        exception
            .getMessage()
            .contains("Error retrieving password for secondary datasource via CNLDBConnectMgr"));
  }

  @Test
  void retrievePassword_notInitialized_throwsIllegalStateException() {
    // Force service to be uninitialized for this test
    ReflectionTestUtils.setField(customPasswordRetrievalService, "cndlMgrInitialized", false);

    Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              customPasswordRetrievalService.retrievePrimaryPassword("anyUser");
            });
    assertEquals("CNLDBConnectMgr not initialized for primary datasource.", exception.getMessage());

    Exception exception2 =
        assertThrows(
            IllegalStateException.class,
            () -> {
              customPasswordRetrievalService.retrievePassword("anyUser", 1, "t", "u", "d", "ds");
            });
    assertEquals("CNLDBConnectMgr not initialized for datasource: ds", exception2.getMessage());
  }
}
