package com.web.fitquest.model.user;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString 
@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor
@RequiredArgsConstructor
@Schema(description = "사용자 DTO")
@Builder
public class User {
    private int id;
    @NonNull private String email;
    @NonNull private String password;
    @NonNull private String name;
    private String profileImage;
    private Integer isAdmin;
    private String description;
    private List<String> categories;
}
