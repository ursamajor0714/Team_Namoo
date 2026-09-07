package com.example.team_navigation_server.board;

import com.example.team_navigation_server.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostVoteRepository extends JpaRepository<PostVote, Long> {
    Optional<PostVote> findByPostAndMember(Post post, Member member);
}
