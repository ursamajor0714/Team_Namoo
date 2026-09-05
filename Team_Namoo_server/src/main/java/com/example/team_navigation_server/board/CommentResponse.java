package com.example.team_navigation_server.board;

import java.time.Instant;

public class CommentResponse {
    private final Long id;
    private final String author;
    private final Instant createdAt;
    private final String content;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.author = comment.getAuthorName();
        this.createdAt = comment.getCreatedAt();
        this.content = comment.getContent();
    }

    public Long getId() { return id; }
    public String getAuthor() { return author; }
    public Instant getCreatedAt() { return createdAt; }
    public String getContent() { return content; }
}
