package com.careercompass.backend.user.service;

import com.careercompass.backend.exception.ResourceNotFoundException;
import com.careercompass.backend.security.UserPrincipal;
import com.careercompass.backend.user.dto.UpdateProfileRequest;
import com.careercompass.backend.user.dto.UserProfileResponse;
import com.careercompass.backend.user.entity.User;
import com.careercompass.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserProfileResponse getCurrentUserProfile() {
        User user = getCurrentUser();
        return mapToProfileResponse(user);
    }

    @Override
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        user.setName(request.getName());
        user.setTargetJobRole(request.getTargetJobRole());

        User updatedUser = userRepository.save(user);
        return mapToProfileResponse(updatedUser);
    }
    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal userPrincipal =
                (UserPrincipal) authentication.getPrincipal();

        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userPrincipal.getId()));
    }
    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .targetJobRole(user.getTargetJobRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}