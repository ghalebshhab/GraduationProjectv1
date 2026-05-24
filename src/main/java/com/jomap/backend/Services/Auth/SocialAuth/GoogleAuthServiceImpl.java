package com.jomap.backend.Services.Auth.SocialAuth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.social.SocialUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleAuthServiceImpl implements GoogleAuthService {

    @Value("${app.google.client-id}")
    private String googleClientId;

    @Override
    public ApiResponse<SocialUserInfo> verifyGoogleToken(String idTokenString) {

        if (idTokenString == null || idTokenString.isBlank()) {
            return new ApiResponse<>(false, "Google token is required", null);
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                return new ApiResponse<>(false, "Invalid Google token", null);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String providerId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            if (providerId == null || providerId.isBlank()) {
                return new ApiResponse<>(false, "Google provider id not found", null);
            }

            if (email == null || email.isBlank()) {
                return new ApiResponse<>(false, "Google email not found", null);
            }

            SocialUserInfo userInfo = new SocialUserInfo(
                    providerId,
                    email,
                    name,
                    picture
            );

            return new ApiResponse<>(true, "Google token verified successfully", userInfo);

        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to verify Google token", null);
        }
    }
}