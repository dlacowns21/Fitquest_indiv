package com.web.fitquest.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.web.fitquest.exception.InvalidRequestException;
import com.web.fitquest.exception.ResourceNotFoundException;
import com.web.fitquest.model.SearchHistory;
import com.web.fitquest.model.board.Board;
import com.web.fitquest.model.searchCondition.SearchCondition;
import com.web.fitquest.service.board.BoardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "게시판 API", description = "게시글 CRUD 및 검색 관련 API")
public class BoardController {

    @Value("${upload.path}")
    private String uploadPath;

    private final BoardService boardService;

    @Operation(summary = "전체 게시글 조회", description = "검색 조건에 따른 전체 게시글 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "204", description = "데이터 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<List<Board>> getAllBoards(@ModelAttribute SearchCondition searchCondition) {
        log.debug("BoardController/getAllBoards");
        List<Board> boards = boardService.allBoards(searchCondition)
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new ResourceNotFoundException("게시글이 존재하지 않습니다."));
        return ResponseEntity.ok(boards);
    }

    @Operation(summary = "특정 게시글 조회", description = "게시글 ID로 특정 게시글을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "204", description = "게시글 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // 특정 게시글 조회
    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoard(@PathVariable int id) {
        log.debug("BoardController/getBoard");
        Board board = boardService.getBoard(id)
                .orElseThrow(() -> new ResourceNotFoundException("ID가 " + id + "인 게시글을 찾을 수 없습니다."));
        return ResponseEntity.ok(board);
    }

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // 게시글 작성
    @PostMapping
    public ResponseEntity<Board> addBoard(@RequestBody Board board) {
        log.debug("BoardController/addBoard");
        boardService.addBoard(board)
                .orElseThrow(() -> new InvalidRequestException("게시글 작성에 실패했습니다."));
        return ResponseEntity.status(HttpStatus.CREATED).body(board);
    }

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "게시글 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<Board> updateBoard(@PathVariable int id, @RequestBody Board board) {
        log.debug("BoardController/updateBoard");
        board.setId(id);
        boardService.updateBoard(board)
                .orElseThrow(() -> new ResourceNotFoundException("수정할 게시글을 찾을 수 없습니다."));
        return ResponseEntity.ok(board);
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "게시글 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    }) 
    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable int id) {
        log.debug("BoardController/deleteBoard");
        boardService.deleteBoard(id)
                .orElseThrow(() -> new ResourceNotFoundException("삭제할 게시글을 찾을 수 없습니다."));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시글 검색", description = "검색 조건에 맞는 게시글을 검색합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "204", description = "검색 결과 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/search")
    public ResponseEntity<List<Board>> searchBoards(@RequestBody SearchCondition searchCondition) {
        log.debug("BoardController/searchBoards: searchCondition = {}", searchCondition);
        List<Board> boards = boardService.searchBoardsByCondition(searchCondition)
                .orElse(List.of());
        return ResponseEntity.ok(boards);
    }

    @Operation(summary = "게시글 이미지 업로드", description = "게시글에 이미지를 업로드합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "업로드 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{boardId}/post-image")
    public ResponseEntity<Map<String, String>> updatePostImage(
            @PathVariable Integer boardId,
            @RequestParam("image") MultipartFile image) throws IOException {
        String imageUrl = boardService.updatePostImage(boardId, image);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @Operation(summary = "게시글 이미지 조회", description = "업로드된 게시글 이미지를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이미지 조회 성공"),
        @ApiResponse(responseCode = "404", description = "이미지 없음")
    })
    @GetMapping("/uploads/posts/{filename:.+}")
    public ResponseEntity<Resource> servePostFile(@PathVariable String filename) {
        try {
            File file = new File(uploadPath + "/posts/" + filename);
            Resource resource = new UrlResource(file.toURI());
            
            if (!resource.exists() && !resource.isReadable()) {
                throw new ResourceNotFoundException("파일을 찾을 수 없습니다: " + filename);
            }

            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException("파일 처리 중 오류가 발생했습니다.", e);
        }
    }

    @Operation(summary = "검색 기록 저장", description = "사용자의 검색 기록을 저장합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "저장 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/history")
    public ResponseEntity<Map<String, String>> saveSearchHistory(@RequestBody SearchHistory searchHistory) {
        log.debug("BoardController/saveSearchHistory: searchHistory = {}", searchHistory);
        boardService.saveSearchHistory(searchHistory)
                .orElseThrow(() -> new InvalidRequestException("검색어 저장에 실패했습니다."));
        return ResponseEntity.ok(Map.of("message", "검색어 저장 성공"));
    }

    @Operation(summary = "검색 기록 조회", description = "특정 사용자의 검색 기록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/history/{userId}/{content}")
    public ResponseEntity<List<String>> getSearchHistory(
            @PathVariable int userId, 
            @PathVariable String content) {
        log.debug("BoardController/getSearchHistory: userId = {}, content = {}", userId, content);
        SearchHistory searchHistory = SearchHistory.builder()
                .userId(userId)
                .content(content)
                .build();
                
        List<String> history = boardService.getSearchHistory(searchHistory)
                .orElse(List.of());
        return ResponseEntity.ok(history);
    }
}