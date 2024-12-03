package com.web.fitquest.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@Schema(description = "사용자Info DTO")
public class UserInfoResponse {
    private int id;
    private String email;
    private String name;
    private String profileImage;
    private int isAdmin;
    private String description;
}
