package site.peaklee.framework.enums;

/**
 * @author PeakLee
 * @version 2023
 * @serial ServerEvent
 * @since 2023/3/31
 */
public enum HandlerEvent {
    Registered,
    UnRegistered,
    Active,
    Inactive,
    BeforeMessage,
    Message,
    AfterMessage,
    Complete,
    Idle,
    Error
}
