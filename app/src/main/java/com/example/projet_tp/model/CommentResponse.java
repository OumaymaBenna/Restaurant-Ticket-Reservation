package com.example.projet_tp.model;

import java.util.List;

public class CommentResponse {
    private boolean success;
    private List<StudentComment> comments;
    private int count;

    public CommentResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<StudentComment> getComments() {
        return comments;
    }

    public void setComments(List<StudentComment> comments) {
        this.comments = comments;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}


