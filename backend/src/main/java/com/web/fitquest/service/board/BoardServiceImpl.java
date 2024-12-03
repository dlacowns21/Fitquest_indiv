package com.web.fitquest.service.board;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.web.fitquest.mapper.board.BoardMapper;
import com.web.fitquest.model.SearchHistory;
import com.web.fitquest.model.board.Board;
import com.web.fitquest.model.board.BoardChoseong;
import com.web.fitquest.model.searchCondition.SearchCondition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BoardServiceImpl implements BoardService {

	private final BoardMapper boardMapper;

	@Value("${upload.path}")
	private String uploadPath;

	@Override
	public Optional<List<Board>> allBoards(SearchCondition searchCondition) {
		return Optional.ofNullable(boardMapper.allBoards(searchCondition));
	}

	@Override
	public Optional<Board> getBoard(int boardId) {
		return Optional.ofNullable(boardMapper.getBoard(boardId));
	}

	@Override
	public Optional<Integer> addBoard(Board board) {
		boardMapper.addBoard(board);

		BoardChoseong boardChoseong = board.getChoseong();
		boardChoseong.setBoardId(board.getId());

		boardMapper.addBoardChoseong(boardChoseong);
		return Optional.ofNullable(board.getId());
	}

	@Override
	public Optional<Integer> updateBoard(Board board) {
		boardMapper.updateBoard(board);
		BoardChoseong boardChoseong = board.getChoseong();
		boardChoseong.setBoardId(board.getId());
		
		if (boardMapper.getBoardChoseong(board.getId()) == null) {
			boardMapper.addBoardChoseong(boardChoseong);
		} else {
			boardMapper.updateBoardChoseong(boardChoseong);
		}

		return Optional.ofNullable(boardMapper.updateBoard(board));
	}

	@Override
	public Optional<List<Board>> searchBoardsByCondition(SearchCondition searchCondition) {
		return Optional.ofNullable(boardMapper.searchBoardsByCondition(searchCondition));
	}

	@Override
	public Optional<Integer> deleteBoard(int boardId) {
		// 게시글 삭제 전에 이미지 파일 삭제
		try {
			Board board = boardMapper.getBoard(boardId);
			if (board != null && board.getPostImage() != null && !board.getPostImage().isEmpty()) {
				// DB에 저장된 경로에서 파일명만 추출
				String fileName = board.getPostImage().substring(board.getPostImage().lastIndexOf("/") + 1);
				File imageFile = new File(uploadPath + "/posts/" + fileName);
				if (imageFile.exists()) {
					if (imageFile.delete()) {
						log.info("게시글 삭제 시 이미지 파일 삭제 성공: {}", imageFile.getAbsolutePath());
					} else {
						log.warn("게시글 삭제 시 이미지 파일 삭제 실패: {}", imageFile.getAbsolutePath());
					}
				}
			}
		} catch (Exception e) {
			log.error("게시글 이미지 삭제 중 오류 발생: ", e);
		}
		return Optional.ofNullable(boardMapper.deleteBoard(boardId));
	}

	@Override
	public String updatePostImage(Integer boardId, MultipartFile file) throws IOException {
		// 기존 이미지 삭제 로직 추가
		Board board = boardMapper.getBoard(boardId);
		if (board.getPostImage() != null && !board.getPostImage().isEmpty()) {
			// DB에 저장된 경로에서 파일명만 추출
			String oldFileName = board.getPostImage().substring(board.getPostImage().lastIndexOf("/") + 1);
			File oldFile = new File(uploadPath + "/posts/" + oldFileName);
			if (oldFile.exists()) {
				if (oldFile.delete()) {
					log.info("이전 포스트 이미지 삭제 성공: {}", oldFile.getAbsolutePath());
				} else {
					log.warn("이전 포스트 이미지 삭제 실패: {}", oldFile.getAbsolutePath());
				}
			}
		}

		String uniqueFileName = UUID.randomUUID().toString() + getExtension(file.getOriginalFilename());

		// posts 디렉토리 생성
		File uploadDir = new File(uploadPath + "/posts");
		if (!uploadDir.exists()) {
			uploadDir.mkdirs();
		}

		// 파일 저장 (uploads/posts 폴더 아래에)
		String filePath = uploadPath + "/posts/" + uniqueFileName;
		File savedFile = new File(filePath);

		log.info("파일 저장 경로: {}", filePath); // 로그 추가

		file.transferTo(savedFile);

		// DB에 저장할 경로 (/uploads/profiles/파일명 형식)
		String dbPath = "/uploads/posts/" + uniqueFileName;
		board.setPostImage(dbPath);
		boardMapper.updateBoard(board);

		return dbPath;
	}

	private String getExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex == -1) {
			return "";
		}
		return filename.substring(dotIndex);
	}

	@Override
	public Optional<Integer> saveSearchHistory(SearchHistory searchHistory) {
		return Optional.ofNullable(boardMapper.saveSearchHistory(searchHistory));
	}

	@Override
	public Optional<List<String>> getSearchHistory(SearchHistory searchHistory) {
		return Optional.ofNullable(boardMapper.getSearchHistory(searchHistory));
	}

	@Override
	public Optional<Integer> updateWriterChoseongByUserId(int userId, String writerChoseong) {
		return Optional.ofNullable(boardMapper.updateWriterChoseongByUserId(userId, writerChoseong));
	}
}
