package com.example.team_navigation_server.board;

import java.time.Instant;

public class CommentResponse {
    private final Long id;
    private final String author;
    private final Instant createdAt;
    private final String content;
    /** 이 댓글을 보고 있는 사람이 작성자 본인인가 - 프론트의 수정/삭제 버튼 노출 기준. */
    private final boolean mine;

    public CommentResponse(Comment comment, Long viewerMemberId) {
        this.id = comment.getId();
        this.author = comment.getAuthorName();
        this.createdAt = comment.getCreatedAt();
        this.content = comment.getContent();
        this.mine = viewerMemberId != null
                && comment.getAuthorMember() != null
                && viewerMemberId.equals(comment.getAuthorMember().getId());
    }

    public Long getId() { return id; }
    public String getAuthor() { return author; }
    public Instant getCreatedAt() { return createdAt; }
    public String getContent() { return content; }
    public boolean isMine() { return mine; }
}
