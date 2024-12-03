package com.web.fitquest.mapper.guestbook;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.web.fitquest.model.guestbook.Guestbook;

@Mapper
public interface GuestbookMapper {
    List<Guestbook> selectGuestbook(Integer userId);

    int insertGuestbook(Guestbook guestbook);

    int deleteGuestbook(Integer guestbookId);
}
