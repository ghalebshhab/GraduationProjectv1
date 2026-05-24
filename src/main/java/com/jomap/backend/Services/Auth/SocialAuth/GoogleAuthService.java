package com.jomap.backend.Services.Auth.SocialAuth;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.social.SocialUserInfo;

public interface GoogleAuthService {

    ApiResponse<SocialUserInfo> verifyGoogleToken(String idToken);
}