package com.web.fitquest.model.guestbook;

import java.time.LocalDateTime;

import com.web.fitquest.model.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Guestbook {
    private Integer id;
    @NonNull private Integer targetId;
    @NonNull private Integer userId;
    @NonNull private String content;
    private LocalDateTime date;
    private User user;
}
