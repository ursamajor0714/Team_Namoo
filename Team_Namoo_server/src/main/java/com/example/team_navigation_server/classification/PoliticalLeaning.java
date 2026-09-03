package com.example.team_navigation_server.classification;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 뉴스 정치성향. 분류 모델 응답 라벨(진보/중립/보수/판단불가)과 매핑되고,
 * CachedNewsArticle 에 저장되며 프론트에는 getLabel() 값(한글)으로 내려간다.
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
