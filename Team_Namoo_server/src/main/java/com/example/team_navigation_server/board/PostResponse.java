package com.example.team_navigation_server.board;

import java.time.LocalDateTime;

public class PostResponse {

    private final Long id;
    private final int boardId;
    private final String title;
    private final String content;
    private final String author;
    private final LocalDateTime createdAt;
    private final int viewCount;
    private final int likeCount;
    private final long commentCount;

    public PostResponse(Post post, long commentCount) {
        this.id = post.getId();
        this.boardId = post.getBoardId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.author = post.getAuthor().getNickname();
        this.createdAt = post.getCreatedAt();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.commentCount = commentCount;
    }

    public Long getId() {
        return id;
    }

    public int getBoardId() {
        return boardId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getViewCount() {
        return viewCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public long getCommentCount() {
        return commentCount;
    }
}
