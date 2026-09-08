package com.example.team_navigation_server.classification;

/**
 * 분류 모델의 판정 결과. leaning 은 진보/중립/보수/판단불가,
 * confidence 는 모델이 그 판정에 준 확률(0~1).
 */
public record ClassificationResult(PoliticalLeaning leaning, double confidence) {
}
