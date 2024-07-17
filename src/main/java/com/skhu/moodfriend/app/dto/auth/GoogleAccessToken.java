package com.skhu.moodfriend.app.dto.auth;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GoogleAccessToken {
    @SerializedName("access_token")
    private String accessToken;
}
