package com.plm.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for automatic audit logging.
 * When applied to a service method, the AuditAspect will automatically
 * capture entity changes and record them in the audit_logs table.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * The type of action being performed (CREATE, UPDATE, DELETE).
     * If not specified, it will be inferred from the method name.
     */
    String action() default "";

    /**
     * The entity type being audited.
     * If not specified, it will be inferred from the method's return type or parameters.
     */
    String entityType() default "";
}
