package com.careercompass.backend.user.service;

import com.careercompass.backend.user.dto.UpdateProfileRequest;
import com.careercompass.backend.user.dto.UserProfileResponse;

public interface UserService {

    UserProfileResponse getCurrentUserProfile();

    UserProfileResponse updateProfile(UpdateProfileRequest request);
}