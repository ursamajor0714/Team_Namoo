package com.example.team_navigation_server.board;

import java.time.Instant;

/** 게시글 목록 한 줄. num 은 공지가 아닌 글에만 매긴다(공지는 null - 프론트가 "공지"로 표시). */
public class PostSummaryResponse {
    private final Long id;
    private final Integer num;
    private final String title;
    private final String author;
    private final Instant createdAt;
    private final int views;
    private final int likes;
    private final long commentCount;

    public PostSummaryResponse(Post post, Integer num, long commentCount) {
        this.id = post.getId();
        this.num = num;
        this.title = post.getTitle();
        this.author = post.getAuthorName();
        this.createdAt = post.getCreatedAt();
        this.views = post.getViews();
        this.likes = post.getLikes();
        this.commentCount = commentCount;
    }

    public Long getId() { return id; }
    public Integer getNum() { return num; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public Instant getCreatedAt() { return createdAt; }
    public int getViews() { return views; }
    public int getLikes() { return likes; }
    public long getCommentCount() { return commentCount; }
}
