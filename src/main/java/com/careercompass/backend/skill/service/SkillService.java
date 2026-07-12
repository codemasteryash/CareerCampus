package com.careercompass.backend.skill.service;

import com.careercompass.backend.skill.dto.AddSkillRequest;
import com.careercompass.backend.skill.dto.SkillResponse;
import com.careercompass.backend.skill.dto.UserSkillResponse;

import java.util.List;

public interface SkillService {

    List<SkillResponse> getAllSkills();
    SkillResponse getSkillById(Long id);

    List<UserSkillResponse> getCurrentUserSkills();
    UserSkillResponse addSkillToProfile(AddSkillRequest request);
    void removeSkillFromProfile(Long skillId);
}