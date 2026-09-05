package com.example.team_navigation_server.board;

import java.util.List;

public class PostListResponse {
    private final List<PostSummaryResponse> notices;
    private final List<PostSummaryResponse> posts;
    private final long totalCount;

    public PostListResponse(List<PostSummaryResponse> notices, List<PostSummaryResponse> posts, long totalCount) {
        this.notices = notices;
        this.posts = posts;
        this.totalCount = totalCount;
    }

    public List<PostSummaryResponse> getNotices() { return notices; }
    public List<PostSummaryResponse> getPosts() { return posts; }
    public long getTotalCount() { return totalCount; }
}
