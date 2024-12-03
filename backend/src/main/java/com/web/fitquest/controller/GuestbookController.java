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
import org.springframework.web.server.ResponseStatusException;

import com.web.fitquest.model.guestbook.Guestbook;
import com.web.fitquest.service.guestbook.GuestbookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/guestbook")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "방명록 API", description = "방명록 관리 API")
public class GuestbookController {
    private final GuestbookService guestbookService;

    @Operation(summary = "방명록 목록 조회", description = "사용자의 방명록 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방명록 목록 조회 성공"),
            @ApiResponse(responseCode = "204", description = "방명록 목록이 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping("/{targetId}")
    public ResponseEntity<List<Guestbook>> getGuestbook(@PathVariable Integer targetId) {
        try {
            Optional<List<Guestbook>> guestbooks = guestbookService.selectGuestbook(targetId);
            if (guestbooks.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NO_CONTENT);
            }
            return ResponseEntity.ok(guestbooks.get());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "방명록 등록", description = "방명록을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방명록 등록 성공"),
            @ApiResponse(responseCode = "400", description = "방명록 등록 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @PostMapping("")
    public ResponseEntity<Integer> postGuestbook(@RequestBody Guestbook guestbook) {
        try {
            Optional<Integer> result = guestbookService.insertGuestbook(guestbook);
            if (result.isPresent() && result.get() == 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(0);
            }
            return ResponseEntity.ok(result.orElse(0));
        } catch (Exception e) {
            log.error("방명록 등록 실패:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "방명록 삭제", description = "방명록을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방명록 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "방명록 삭제 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @DeleteMapping("/{guestbookId}")
    public ResponseEntity<Integer> deleteGuestbook(@PathVariable Integer guestbookId) {
        try {
            Optional<Integer> result = guestbookService.deleteGuestbook(guestbookId);
            if (result.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            return ResponseEntity.ok(result.get());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
