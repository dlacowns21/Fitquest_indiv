package com.web.fitquest.mapper.board;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.web.fitquest.model.SearchHistory;
import com.web.fitquest.model.board.Board;
import com.web.fitquest.model.board.BoardChoseong;
import com.web.fitquest.model.searchCondition.SearchCondition;

@Mapper
public interface BoardMapper {
    
    List<Board> allBoards(SearchCondition searchCondition);
	
	Board getBoard(int boardId);
	
	Integer addBoard(Board board);
	
	Integer addBoardChoseong(BoardChoseong boardChoseong);
	
	Integer updateBoard(Board board);

	Integer updateBoardChoseong(BoardChoseong boardChoseong);
	
	Integer deleteBoard(int boardId);

	List<Board> searchBoardsByCondition(SearchCondition searchCondition);

	Integer saveSearchHistory(SearchHistory searchHistory);

	List<String> getSearchHistory(SearchHistory searchHistory);

	Integer updateWriterChoseongByUserId(@Param("userId") int userId, @Param("writerChoseong") String writerChoseong);

	BoardChoseong getBoardChoseong(int boardId);
}
