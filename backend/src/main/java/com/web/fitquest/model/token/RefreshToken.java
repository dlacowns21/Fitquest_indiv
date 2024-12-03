package com.web.fitquest.model.token;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RefreshToken {
    private int userId;
    private String token;
    private LocalDateTime expiryDate;
} 
