package com.inkWell.admin.service;

import com.inkWell.admin.domain.entity.AuditLog;
import com.inkWell.admin.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void logAction(String action, String targetType, String targetId, String details) {
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        
        AuditLog log = AuditLog.builder()
                .adminUsername(adminUsername)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .build();
        
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
