package com.jomap.backend.Services.Auth;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.social.SocialUserInfo;

public interface FacebookAuthService {

    ApiResponse<SocialUserInfo> verifyFacebookToken(String accessToken);
}