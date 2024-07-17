package com.skhu.moodfriend.app.dto.auth;

import com.google.gson.annotations.SerializedName;
import com.skhu.moodfriend.app.entity.member.EmotionType;
import com.skhu.moodfriend.app.entity.member.LoginType;
import com.skhu.moodfriend.app.entity.member.RoleType;
import lombok.Data;

@Data
public class MemberInfo {
    private Long memberId;
    private String email;
    @SerializedName("verified_email")
    private Boolean verifiedEmail;
    private String name;
    @SerializedName("given_name")
    private String givenName;
    private long mileage;
    private EmotionType emotionType;
    private LoginType loginType;
    private RoleType roleType;
}