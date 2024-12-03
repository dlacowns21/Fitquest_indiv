package com.web.fitquest.service.board;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.web.fitquest.model.SearchHistory;
import com.web.fitquest.model.board.Board;
import com.web.fitquest.model.searchCondition.SearchCondition;

public interface BoardService {
    
    Optional<List<Board>> allBoards(SearchCondition searchCondition);
	
	Optional<Board> getBoard(int boardId);
	
	Optional<Integer> addBoard(Board board);
	
	Optional<Integer> updateBoard(Board board);
	
	Optional<Integer> deleteBoard(int boardId);

	Optional<List<Board>> searchBoardsByCondition(SearchCondition searchCondition);

	String updatePostImage(Integer boardId, MultipartFile file) throws IOException;

	Optional<Integer> saveSearchHistory(SearchHistory searchHistory);

	Optional<List<String>> getSearchHistory(SearchHistory searchHistory);

	Optional<Integer> updateWriterChoseongByUserId(int userId, String writerChoseong);
}
