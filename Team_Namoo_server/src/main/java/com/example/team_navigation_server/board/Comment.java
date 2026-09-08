package com.example.team_navigation_server.board;

import com.example.team_navigation_server.member.Member;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_member_id")
    private Member authorMember;

    @Column(nullable = false)
    private String authorName;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostVisibility visibility = PostVisibility.NORMAL;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Comment() {
    }

    public Comment(Post post, Member authorMember, String authorName, String content) {
        this.post = post;
        this.authorMember = authorMember;
        this.authorName = authorName;
        this.content = content;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public Member getAuthorMember() {
        return authorMember;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getContent() {
        return content;
    }

    // 본인 댓글 수정
    public void updateContent(String content) {
        this.content = content;
    }

    public PostVisibility getVisibility() {
        return visibility;
    }

    // 회원 탈퇴 시 호출 - 댓글은 남기고 작성자 연결만 끊는다.
    public void detachAuthor(String anonymizedName) {
        this.authorMember = null;
        this.authorName = anonymizedName;
    }

    public void setVisibility(PostVisibility visibility) {
        this.visibility = visibility;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
