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

    public void setVisibility(PostVisibility visibility) {
        this.visibility = visibility;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
