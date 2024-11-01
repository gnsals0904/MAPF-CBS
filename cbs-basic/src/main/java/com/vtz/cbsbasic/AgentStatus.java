package com.vtz.cbsbasic;

public enum AgentStatus {
    IDLE,             // 대기 중
    MOVING_TO_TASK,   // 작업 위치로 이동 중
    PERFORMING_TASK,  // 작업 수행 중
    RETURNING_HOME    // 원래 위치로 복귀 중
}
