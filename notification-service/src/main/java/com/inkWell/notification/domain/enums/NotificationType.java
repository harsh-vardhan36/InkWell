package com.inkWell.notification.domain.enums;

public enum NotificationType {
    // In-app types
    COMMENT_REPLY,
    NEW_POST,
    MENTION,
    NEWSLETTER,
    SYSTEM,
    // Email-only types from auth-service
    VERIFICATION,
    LOGIN_ALERT,
    PASSWORD_RESET,
    AUTHOR_VERIFICATION,
    ACCOUNT_DEACTIVATION,
    NEWSLETTER_SUBSCRIPTION
}
