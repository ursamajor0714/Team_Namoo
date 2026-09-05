package com.example.team_navigation_server.admin;

import com.example.team_navigation_server.board.Post;
import com.example.team_navigation_server.board.PostVisibility;

import java.time.Instant;

public class AdminPostResponse {
    private final Long id;
    private final int num;
    private final String title;
    private final String author;
    private final Instant createdAt;
    private final PostVisibility visibility;
    private final int views;
    private final int likes;

    public AdminPostResponse(Post post, int num) {
        this.id = post.getId();
        this.num = num;
        this.title = post.getTitle();
        this.author = post.getAuthorName();
        this.createdAt = post.getCreatedAt();
        this.visibility = post.getVisibility();
        this.views = post.getViews();
        this.likes = post.getLikes();
    }

    public Long getId() { return id; }
    public int getNum() { return num; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public Instant getCreatedAt() { return createdAt; }
    public PostVisibility getVisibility() { return visibility; }
    public int getViews() { return views; }
    public int getLikes() { return likes; }
}
