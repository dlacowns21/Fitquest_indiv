package com.web.fitquest.service.guestbook;

import java.util.List;
import java.util.Optional;

import com.web.fitquest.model.guestbook.Guestbook;

public interface GuestbookService {
    Optional<List<Guestbook>> selectGuestbook(Integer userId);

    Optional<Integer> insertGuestbook(Guestbook guestbook);

    Optional<Integer> deleteGuestbook(Integer guestbookId);
}
