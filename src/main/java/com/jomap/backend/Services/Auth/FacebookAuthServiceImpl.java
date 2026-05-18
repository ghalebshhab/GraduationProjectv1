package com.jomap.backend.Services.Auth;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.social.SocialUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FacebookAuthServiceImpl implements FacebookAuthService {

    private final RestTemplate restTemplate;

    @Value("${app.facebook.app-id}")
    private String facebookAppId;

    @Value("${app.facebook.app-secret}")
    private String facebookAppSecret;

    @Override
    public ApiResponse<SocialUserInfo> verifyFacebookToken(String accessToken) {

        if (accessToken == null || accessToken.isBlank()) {
            return new ApiResponse<>(false, "Facebook token is required", null);
        }

        String appAccessToken = facebookAppId + "|" + facebookAppSecret;

        try {
            String debugUrl = UriComponentsBuilder
                    .fromPath("https://graph.facebook.com/debug_token")
                    .queryParam("input_token", accessToken)
                    .queryParam("access_token", appAccessToken)
                    .toUriString();

            Map<?, ?> debugResponse = restTemplate.getForObject(debugUrl, Map.class);

            if (debugResponse == null || debugResponse.get("data") == null) {
                return new ApiResponse<>(false, "Invalid Facebook token response", null);
            }

            Map<?, ?> data = (Map<?, ?>) debugResponse.get("data");

            Object isValidObj = data.get("is_valid");
            Object appIdObj = data.get("app_id");
            Object userIdObj = data.get("user_id");

            boolean isValid = Boolean.TRUE.equals(isValidObj);
            String appId = appIdObj == null ? null : String.valueOf(appIdObj);
            String userId = userIdObj == null ? null : String.valueOf(userIdObj);

            if (!isValid) {
                return new ApiResponse<>(false, "Facebook token is not valid", null);
            }

            if (appId == null || !appId.equals(facebookAppId)) {
                return new ApiResponse<>(false, "Facebook token app id does not match", null);
            }

            if (userId == null || userId.isBlank()) {
                return new ApiResponse<>(false, "Facebook user id not found", null);
            }

            String meUrl = UriComponentsBuilder
                    .fromPath("https://graph.facebook.com/me")
                    .queryParam("fields", "id,name,email,picture")
                    .queryParam("access_token", accessToken)
                    .toUriString();

            Map<?, ?> userResponse = restTemplate.getForObject(meUrl, Map.class);

            if (userResponse == null) {
                return new ApiResponse<>(false, "Could not fetch Facebook user info", null);
            }

            String email = userResponse.get("email") == null
                    ? null
                    : String.valueOf(userResponse.get("email"));

            String name = userResponse.get("name") == null
                    ? null
                    : String.valueOf(userResponse.get("name"));

            String picture = extractPictureUrl(userResponse);

            /*
             Important:
             Facebook sometimes does not return email unless the user gives email permission.
            */
            if (email == null || email.isBlank()) {
                return new ApiResponse<>(
                        false,
                        "Facebook email not found. Please make sure email permission is enabled.",
                        null
                );
            }

            SocialUserInfo userInfo = new SocialUserInfo(
                    userId,
                    email,
                    name,
                    picture
            );

            return new ApiResponse<>(true, "Facebook token verified successfully", userInfo);

        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to verify Facebook token", null);
        }
    }

    private String extractPictureUrl(Map<?, ?> userResponse) {

        try {
            Object pictureObj = userResponse.get("picture");

            if (!(pictureObj instanceof Map<?, ?> pictureMap)) {
                return null;
            }

            Object dataObj = pictureMap.get("data");

            if (!(dataObj instanceof Map<?, ?> dataMap)) {
                return null;
            }

            Object urlObj = dataMap.get("url");

            return urlObj == null ? null : String.valueOf(urlObj);

        } catch (Exception e) {
            return null;
        }
    }
}