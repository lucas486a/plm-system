package com.plm.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.plm.entity.AuditLog;
import com.plm.service.AuditLogService;
import jakarta.persistence.Entity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * AOP Aspect for automatic audit logging of entity changes.
 * Intercepts methods annotated with {@link Auditable} and records
 * entity changes in the audit_logs table.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    /**
     * Around advice for methods annotated with @Auditable.
     * Captures entity changes before and after method execution.
     */
    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        // Get method signature
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();

        // Determine action type
        String action = determineAction(auditable.action(), methodName);

        // Get entity type from annotation or infer from method
        String entityType = auditable.entityType();
        if (entityType.isEmpty()) {
            entityType = inferEntityType(signature);
        }

        // Get current user and IP address
        Long userId = getCurrentUserId();
        String ipAddress = getClientIpAddress();

        // Capture old value for UPDATE and DELETE operations
        String oldValueJson = null;
        if ("UPDATE".equals(action) || "DELETE".equals(action)) {
            oldValueJson = captureOldValue(joinPoint, action);
        }

        // Execute the method
        Object result = joinPoint.proceed();

        // Capture new value for CREATE and UPDATE operations
        String newValueJson = null;
        if ("CREATE".equals(action) || "UPDATE".equals(action)) {
            newValueJson = captureNewValue(result, joinPoint.getArgs(), action);
        }

        // Determine entity ID
        Long entityId = determineEntityId(result, joinPoint.getArgs(), action);

        // Record audit log
        if (entityType != null && entityId != null) {
            try {
                auditLogService.recordAuditLog(
                        entityType, entityId, action,
                        oldValueJson, newValueJson,
                        userId, ipAddress
                );
                log.debug("Audit log recorded: {} {} (id: {})", action, entityType, entityId);
            } catch (Exception e) {
                // Don't let audit logging failures break the main operation
                log.error("Failed to record audit log: {} {} (id: {})", action, entityType, entityId, e);
            }
        }

        return result;
    }

    /**
     * Determine the action type from annotation or method name.
     */
    private String determineAction(String annotatedAction, String methodName) {
        if (!annotatedAction.isEmpty()) {
            return annotatedAction.toUpperCase();
        }

        String lowerMethodName = methodName.toLowerCase();
        if (lowerMethodName.startsWith("create") || lowerMethodName.startsWith("add") ||
            lowerMethodName.startsWith("save") || lowerMethodName.startsWith("insert")) {
            return "CREATE";
        } else if (lowerMethodName.startsWith("update") || lowerMethodName.startsWith("modify") ||
                   lowerMethodName.startsWith("edit")) {
            return "UPDATE";
        } else if (lowerMethodName.startsWith("delete") || lowerMethodName.startsWith("remove")) {
            return "DELETE";
        }

        // Default to UPDATE for save operations (could be create or update)
        return "UPDATE";
    }

    /**
     * Infer entity type from method signature.
     */
    private String inferEntityType(MethodSignature signature) {
        // Try to get from return type
        Class<?> returnType = signature.getReturnType();
        if (returnType.isAnnotationPresent(Entity.class)) {
            return returnType.getSimpleName();
        }

        // Try to get from parameters
        Class<?>[] parameterTypes = signature.getParameterTypes();
        for (Class<?> paramType : parameterTypes) {
            if (paramType.isAnnotationPresent(Entity.class)) {
                return paramType.getSimpleName();
            }
        }

        // Try to infer from DTO name (e.g., PartDTO -> Part)
        String returnTypeName = returnType.getSimpleName();
        if (returnTypeName.endsWith("DTO")) {
            return returnTypeName.substring(0, returnTypeName.length() - 3);
        }

        return null;
    }

    /**
     * Capture old value for UPDATE and DELETE operations.
     */
    private String captureOldValue(ProceedingJoinPoint joinPoint, String action) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                // For UPDATE: first arg is usually the ID, entity might be in DTO form
                // For DELETE: first arg is usually the ID
                // We'll try to serialize the first argument that looks like an entity or DTO
                for (Object arg : args) {
                    if (arg != null && isEntityOrDto(arg.getClass())) {
                        return serializeToJson(arg);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to capture old value", e);
        }
        return null;
    }

    /**
     * Capture new value for CREATE and UPDATE operations.
     */
    private String captureNewValue(Object result, Object[] args, String action) {
        try {
            // Try to serialize the result first
            if (result != null && isEntityOrDto(result.getClass())) {
                return serializeToJson(result);
            }

            // For CREATE: result is usually the created entity/DTO
            // For UPDATE: result is usually the updated entity/DTO
            if (args != null) {
                for (Object arg : args) {
                    if (arg != null && isEntityOrDto(arg.getClass())) {
                        return serializeToJson(arg);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to capture new value", e);
        }
        return null;
    }

    /**
     * Determine entity ID from result or arguments.
     */
    private Long determineEntityId(Object result, Object[] args, String action) {
        // Try to get ID from result
        if (result != null) {
            Long id = extractIdFromObject(result);
            if (id != null) {
                return id;
            }
        }

        // Try to get ID from arguments
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof Long) {
                    return (Long) arg;
                }
                if (arg != null) {
                    Long id = extractIdFromObject(arg);
                    if (id != null) {
                        return id;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Extract ID from an object using reflection.
     */
    private Long extractIdFromObject(Object obj) {
        try {
            // Try getId() method
            Method getIdMethod = obj.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(obj);
            if (id instanceof Long) {
                return (Long) id;
            }
        } catch (Exception e) {
            // Ignore - method might not exist
        }
        return null;
    }

    /**
     * Check if a class is an entity or DTO.
     */
    private boolean isEntityOrDto(Class<?> clazz) {
        return clazz.isAnnotationPresent(Entity.class) ||
               clazz.getSimpleName().endsWith("DTO") ||
               clazz.getPackageName().contains(".entity") ||
               clazz.getPackageName().contains(".dto");
    }

    /**
     * Serialize object to JSON string.
     */
    private String serializeToJson(Object obj) {
        try {
            ObjectMapper mapper = objectMapper.copy();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get current user ID from SecurityContext.
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                // Try to extract user ID from principal
                Object principal = authentication.getPrincipal();
                if (principal instanceof com.plm.entity.User) {
                    return ((com.plm.entity.User) principal).getId();
                }
                // For simple string principals (e.g., "admin"), return null
                // In a real application, you'd have a UserDetailsService that returns a UserDetails with ID
            }
        } catch (Exception e) {
            log.debug("Could not get current user ID", e);
        }
        return null;
    }

    /**
     * Get client IP address from request.
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // Check for proxy headers
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }

                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }

                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not get client IP address", e);
        }
        return null;
    }
}
