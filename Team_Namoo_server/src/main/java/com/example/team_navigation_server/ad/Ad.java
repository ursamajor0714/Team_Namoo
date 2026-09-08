package com.example.team_navigation_server.ad;

import com.example.team_navigation_server.member.Member;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ads")
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String page;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdSide side;

    private String imageUrl;

    private String linkUrl;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Member createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Ad() {
    }

    public Ad(String page, AdSide side, String imageUrl, String linkUrl,
               LocalDateTime startAt, LocalDateTime endAt, Member createdBy) {
        this.page = page;
        this.side = side;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.startAt = startAt;
        this.endAt = endAt;
        this.createdBy = createdBy;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public String getPage() {
        return page;
    }
    public AdSide getSide() {
        return side;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getLinkUrl() {
        return linkUrl;
    }
    public LocalDateTime getStartAt() {
        return startAt;
    }
    public LocalDateTime getEndAt() {
        return endAt;
    }
    public Member getCreatedBy() {
        return createdBy;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // 등록자 회원이 탈퇴하면 광고는 남기고 연결만 끊는다(created_by 는 nullable).
    public void detachCreatedBy() {
        this.createdBy = null;
    }
}
