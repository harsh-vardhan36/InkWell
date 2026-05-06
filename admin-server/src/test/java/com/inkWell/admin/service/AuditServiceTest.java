package com.inkWell.admin.service;

import com.inkWell.admin.domain.entity.AuditLog;
import com.inkWell.admin.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void logAction_Success() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("adminUser");
        
        auditService.logAction("DELETE", "POST", "123", "Deleted a post");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void getAllLogs_Success() {
        AuditLog log = new AuditLog();
        when(auditLogRepository.findAllByOrderByTimestampDesc()).thenReturn(Arrays.asList(log));

        List<AuditLog> result = auditService.getAllLogs();

        assertEquals(1, result.size());
    }
}
