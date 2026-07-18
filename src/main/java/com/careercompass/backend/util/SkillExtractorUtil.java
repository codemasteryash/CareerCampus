package com.careercompass.backend.util;

import com.careercompass.backend.skill.entity.Skill;
import com.careercompass.backend.skill.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SkillExtractorUtil {

    private final SkillRepository skillRepository;
    public List<Skill> matchSkillsFromCatalogue(List<String> aiExtractedSkillNames) {
        List<Skill> matchedSkills = new ArrayList<>();

        for (String skillName : aiExtractedSkillNames) {
            skillRepository.findByName(skillName.trim())
                    .ifPresent(matchedSkills::add);
        }

        return matchedSkills;
    }
}