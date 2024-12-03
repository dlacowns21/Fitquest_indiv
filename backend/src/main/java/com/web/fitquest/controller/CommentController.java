package com.web.fitquest.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.web.fitquest.model.comment.Comment;
import com.web.fitquest.service.comment.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "댓글 API", description = "게시글 댓글 및 대댓글 관리 API")
public class CommentController {
    private final CommentService commentService;
    
    @Operation(summary = "게시글 댓글 목록 조회", description = "특정 게시글의 모든 댓글을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "204", description = "댓글 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // 특정 게시글의 모든 댓글 조회
    @GetMapping("/board/{boardId}")  // URL 경로 수정
    public ResponseEntity<?> getAllComments(@PathVariable int boardId) {
        try {
            Optional<List<Comment>> result = commentService.allComments(boardId);
            if(result.isPresent() && !result.get().isEmpty()) {
                return new ResponseEntity<List<Comment>>(result.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
            }
        } catch(Exception e) {
            return exceptionHandling(e);
        }
    }

    @Operation(summary = "특정 댓글 조회", description = "댓글 ID로 특정 댓글을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "204", description = "댓글 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // 특정 댓글 조회
    @GetMapping("/detail/{id}")  // URL 경로 중복을 피하기 위해 'detail' 추가
    public ResponseEntity<?> getComment(@PathVariable int commentId) {
        try {
            Optional<Comment> result = commentService.getComment(commentId);
            if(result.isPresent()) {
                return new ResponseEntity<Comment>(result.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
            }
        } catch(Exception e) {
            return exceptionHandling(e);
        }
    }

    @Operation(summary = "댓글 작성", 
              description = "새로운 댓글이나 대댓글을 작성합니다. parentId가 있으면 대댓글로 처리됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "작성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락 또는 유효하지 않은 부모 댓글)"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // 새 댓글 추가 (일반 댓글 or 대댓글)
    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody Comment comment) {
        try {
            // 필수 필드 검증 (기본 댓글 정보)
            if (comment.getBoardId() <= 0 ||
                comment.getUserId() <= 0 ||
                comment.getWriter() == null || comment.getWriter().trim().isEmpty() ||
                comment.getContent() == null || comment.getContent().trim().isEmpty()) {
                return new ResponseEntity<String>("필수 입력값이 누락되었습니다.", HttpStatus.BAD_REQUEST);
            }
            
            // 대댓글인 경우 추가 검증 (parentId null 체크 추가)
            if (comment.getParentId() != null && comment.getParentId() > 0) {
                Optional<Comment> parentComment = commentService.getComment(comment.getParentId());
                if (!parentComment.isPresent()) {
                    return new ResponseEntity<String>("부모 댓글이 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
                }
                // 동일 게시글 내의 댓글에만 대댓글 작성 가능
                if (parentComment.get().getBoardId() != comment.getBoardId()) {
                    return new ResponseEntity<String>("해당 게시글의 댓글에만 대댓글을 작성할 수 있습니다.", HttpStatus.BAD_REQUEST);
                }
            }
            
            Optional<Comment> result = commentService.addComment(comment);
            if (result.isPresent()) {
                // 생성된 댓글 정보를 응답에 포함
                return new ResponseEntity<Comment>(result.get(), HttpStatus.CREATED);
            } else {
                return new ResponseEntity<String>("댓글 작성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch(Exception e) {
            return exceptionHandling(e);
        }
    }

    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제 처리합니다 (is_delete 플래그 설정).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // 댓글 삭제 (is_delete 플래그 설정)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable int id) {
        try {
            Comment comment = new Comment();
            comment.setId(id);
            Optional<Comment> result = commentService.deleteComment(comment);
            if (result.isPresent()) {
                return new ResponseEntity<Void>(HttpStatus.OK);
            } else {
                return new ResponseEntity<String>("댓글 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch(Exception e) {
            return exceptionHandling(e);
        }
    }
    
    private ResponseEntity<String> exceptionHandling(Exception e) {
        e.printStackTrace();
        return new ResponseEntity<String>("Error : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}