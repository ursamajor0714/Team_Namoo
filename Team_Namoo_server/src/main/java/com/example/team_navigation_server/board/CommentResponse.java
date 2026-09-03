package com.example.team_navigation_server.board;

import java.time.LocalDateTime;

public class CommentResponse {

    private final Long id;
    private final String author;
    private final String content;
    private final LocalDateTime createdAt;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.author = comment.getAuthor().getNickname();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
