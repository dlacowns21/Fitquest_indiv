package com.web.fitquest.mapper.comment;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.web.fitquest.model.comment.Comment;

@Mapper
public interface CommentMapper {
   // 게시글 ID로 댓글 목록 조회
   List<Comment> allComments(int boardId);
   
   // 특정 댓글 조회 (대댓글 검증용)
   Comment getComment(int id);  // commentId -> id로 변경

   // 댓글 추가 - int 반환으로 변경 (성공:1, 실패:0)
   int addComment(Comment comment);

   // 댓글 삭제 - int 반환으로 변경 (성공:1, 실패:0)
   int deleteComment(Comment comment);

   List<Comment> findChildComments(int parentId);
}