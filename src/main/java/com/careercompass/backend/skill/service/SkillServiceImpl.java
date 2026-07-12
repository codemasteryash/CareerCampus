package com.careercompass.backend.skill.service;

import com.careercompass.backend.exception.ResourceNotFoundException;
import com.careercompass.backend.security.UserPrincipal;
import com.careercompass.backend.skill.dto.AddSkillRequest;
import com.careercompass.backend.skill.dto.SkillResponse;
import com.careercompass.backend.skill.dto.UserSkillResponse;
import com.careercompass.backend.skill.entity.Skill;
import com.careercompass.backend.skill.entity.UserSkill;
import com.careercompass.backend.skill.repository.SkillRepository;
import com.careercompass.backend.skill.repository.UserSkillRepository;
import com.careercompass.backend.user.entity.User;
import com.careercompass.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;


    @Override
    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll()
                .stream()
                .map(this::mapToSkillResponse)
                .toList();
    }

    @Override
    public SkillResponse getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Skill not found with id: " + id));
        return mapToSkillResponse(skill);
    }


    @Override
    public List<UserSkillResponse> getCurrentUserSkills() {
        Long userId = getCurrentUserId();
        return userSkillRepository.findByUserId(userId)
                .stream()
                .map(this::mapToUserSkillResponse)
                .toList();
    }

    @Override
    public UserSkillResponse addSkillToProfile(AddSkillRequest request) {
        Long userId = getCurrentUserId();
        if (userSkillRepository.existsByUserIdAndSkillId(
                userId, request.getSkillId())) {
            throw new IllegalArgumentException(
                    "Skill already exists in your profile.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Skill not found with id: " + request.getSkillId()));

        UserSkill userSkill = UserSkill.builder()
                .user(user)
                .skill(skill)
                .proficiencyLevel(request.getProficiencyLevel())
                .source("SELF_DECLARED")
                .build();

        UserSkill saved = userSkillRepository.save(userSkill);
        return mapToUserSkillResponse(saved);
    }

    @Override
    public void removeSkillFromProfile(Long skillId) {
        Long userId = getCurrentUserId();

        UserSkill userSkill = userSkillRepository
                .findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Skill not found in your profile."));

        userSkillRepository.delete(userSkill);
    }

    private Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }

    private SkillResponse mapToSkillResponse(Skill skill) {
        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .build();
    }

    private UserSkillResponse mapToUserSkillResponse(UserSkill userSkill) {
        return UserSkillResponse.builder()
                .skillId(userSkill.getSkill().getId())
                .skillName(userSkill.getSkill().getName())
                .category(userSkill.getSkill().getCategory())
                .proficiencyLevel(userSkill.getProficiencyLevel())
                .source(userSkill.getSource())
                .build();
    }
}