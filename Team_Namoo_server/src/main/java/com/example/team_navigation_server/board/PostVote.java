package com.example.team_navigation_server.board;

import com.example.team_navigation_server.member.Member;
import jakarta.persistence.*;

// 회원 1명당 게시글 1개에 추천/비추천 1개 - 유니크 제약으로 중복 방지 (로그인 회원만 대상)
@Entity
@Table(name = "post_votes", uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "member_id"}))
public class PostVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostVoteType type;

    protected PostVote() {
    }

    public PostVote(Post post, Member member, PostVoteType type) {
        this.post = post;
        this.member = member;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public Member getMember() {
        return member;
    }

    public PostVoteType getType() {
        return type;
    }

    public void changeType(PostVoteType type) {
        this.type = type;
    }
}
