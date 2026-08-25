package com.example.team_navigation_server.classification;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 정치성향 분류 테스트 파이프라인 전용 - 확인 끝나면 이 패키지 전체를 삭제할 예정.
 */
public enum PoliticalLeaning {
    PROGRESSIVE("진보"),
    NEUTRAL("중립"),
    CONSERVATIVE("보수"),
    UNDETERMINED("판단불가");

    private final String label;

    PoliticalLeaning(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static PoliticalLeaning fromLabel(String label) {
        for (PoliticalLeaning value : values()) {
            if (value.label.equals(label)) {
                return value;
            }
        }
        return UNDETERMINED;
    }
}
