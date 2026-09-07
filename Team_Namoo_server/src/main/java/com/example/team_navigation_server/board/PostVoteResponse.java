package com.example.team_navigation_server.board;

public class PostVoteResponse {

    private final int likes;
    private final int dislikes;
    private final String myVote;

    public PostVoteResponse(int likes, int dislikes, String myVote) {
        this.likes = likes;
        this.dislikes = dislikes;
        this.myVote = myVote;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public String getMyVote() {
        return myVote;
    }
}
