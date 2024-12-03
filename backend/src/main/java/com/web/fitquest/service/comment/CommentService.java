package com.web.fitquest.service.comment;

import java.util.List;
import java.util.Optional;

import com.web.fitquest.model.comment.Comment;

public interface CommentService {
    // 현재 메서드들은 유지
    Optional<List<Comment>> allComments(int boardId);
    Optional<Comment> deleteComment(Comment comment);
    Optional<Comment> getComment(int id);  // commentId -> id로 변경

    // addComment 메서드 수정
    Optional<Comment> addComment(Comment comment);
}