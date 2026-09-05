package com.example.team_navigation_server.board;

import java.time.Instant;

public class PostDetailResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String author;
    private final Instant createdAt;
    private final int views;
    private final int likes;
    private final int dislikes;
    private final long commentCount;

    public PostDetailResponse(Post post, long commentCount) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.author = post.getAuthorName();
        this.createdAt = post.getCreatedAt();
        this.views = post.getViews();
        this.likes = post.getLikes();
        this.dislikes = post.getDislikes();
        this.commentCount = commentCount;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public Instant getCreatedAt() { return createdAt; }
    public int getViews() { return views; }
    public int getLikes() { return likes; }
    public int getDislikes() { return dislikes; }
    public long getCommentCount() { return commentCount; }
}
