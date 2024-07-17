package com.skhu.moodfriend.app.service.auth;

import com.google.gson.Gson;
import com.skhu.moodfriend.app.dto.auth.GoogleAccessToken;
import com.skhu.moodfriend.app.dto.auth.MemberInfo;
import com.skhu.moodfriend.app.entity.member.EmotionType;
import com.skhu.moodfriend.app.entity.member.LoginType;
import com.skhu.moodfriend.app.entity.member.Member;
import com.skhu.moodfriend.app.entity.member.RoleType;
import com.skhu.moodfriend.app.repository.MemberRepository;
import com.skhu.moodfriend.global.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthLoginService {

    //콘솔에 Authorization code 출력
//    public void socialLogin(String code, String registrationId) {
//        System.out.println("code = " + code);
//        System.out.println("registrationId = " + registrationId);
//    }
    @Value("${client-id}")  // value import 할때 lombok으로 하면 안됨.
    private String GOOGLE_CLIENT_ID;

    @Value("${client-secret}")
    private String GOOGLE_CLIENT_SECRET;

    private final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private final String GOOGLE_REDIRECT_URI = "http://localhost:8080/login/oauth2/code/google";

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public String getGoogleAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> params = Map.of(
                "code", code,
                "scope", "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email",
                "client_id", GOOGLE_CLIENT_ID,
                "client_secret", GOOGLE_CLIENT_SECRET,
                "redirect_uri", GOOGLE_REDIRECT_URI,
                "grant_type", "authorization_code"
        );

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(GOOGLE_TOKEN_URL, params, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String json = responseEntity.getBody();
            Gson gson = new Gson();

            return gson.fromJson(json, GoogleAccessToken.class)
                    .getAccessToken();
        }

        throw new RuntimeException("구글 엑세스 토큰을 가져오는데 실패했습니다.");
    }

    public GoogleAccessToken loginOrSignUp(String googleAccessToken) {
        MemberInfo memberInfo = getMemberInfo(googleAccessToken);

        if (!memberInfo.getVerifiedEmail()) {
            throw new RuntimeException("이메일 인증이 되지 않은 유저입니다.");
        }


        Member member = memberRepository.findByEmail(memberInfo.getEmail()).orElseGet(() ->
                memberRepository.save(Member.builder()
                        .email(memberInfo.getEmail())
                        .name(memberInfo.getName())
                        .mileage(memberInfo.getMileage())
                        .emotionType(EmotionType.JOY) // 기본 감정 유형 설정
                        .loginType(LoginType.GOOGLE_LOGIN) // 구글 로그인으로 설정
                        .roleType(RoleType.ROLE_USER) // 기본 역할 설정
                        .build())
        );

        return tokenProvider.createAccessToken(member);
    }

    public MemberInfo getMemberInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url));
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String json = responseEntity.getBody();
            Gson gson = new Gson();
            return gson.fromJson(json, MemberInfo.class);
        }

        throw new RuntimeException("유저 정보를 가져오는데 실패했습니다.");
    }
}