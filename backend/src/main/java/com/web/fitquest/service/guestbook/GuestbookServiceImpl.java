package com.web.fitquest.service.guestbook;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.fitquest.mapper.guestbook.GuestbookMapper;
import com.web.fitquest.model.guestbook.Guestbook;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class GuestbookServiceImpl implements GuestbookService {
    private final GuestbookMapper guestbookMapper;

    @Override
    public Optional<List<Guestbook>> selectGuestbook(Integer userId) {
        return Optional.ofNullable(guestbookMapper.selectGuestbook(userId));
    }

    @Override
    public Optional<Integer> insertGuestbook(Guestbook guestbook) {
        return Optional.ofNullable(guestbookMapper.insertGuestbook(guestbook));
    }

    @Override
    public Optional<Integer> deleteGuestbook(Integer guestbookId) {
        return Optional.ofNullable(guestbookMapper.deleteGuestbook(guestbookId));
    }
}
