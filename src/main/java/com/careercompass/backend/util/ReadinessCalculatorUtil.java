package com.careercompass.backend.util;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReadinessCalculatorUtil {
    private static final double MUST_HAVE_WEIGHT = 0.80;
    private static final double NICE_TO_HAVE_WEIGHT = 0.20;

    public int calculateScore(
            List<String> userSkillNames,
            List<String> mustHaveSkills,
            List<String> niceToHaveSkills) {

        if (mustHaveSkills.isEmpty() && niceToHaveSkills.isEmpty()) {
            return 0;
        }
        long mustHavePresent = mustHaveSkills.stream()
                .filter(skill -> containsIgnoreCase(userSkillNames, skill))
                .count();

        long niceToHavePresent = niceToHaveSkills.stream()
                .filter(skill -> containsIgnoreCase(userSkillNames, skill))
                .count();

        double mustHaveScore = mustHaveSkills.isEmpty() ? 1.0 :
                (double) mustHavePresent / mustHaveSkills.size();

        double niceToHaveScore = niceToHaveSkills.isEmpty() ? 1.0 :
                (double) niceToHavePresent / niceToHaveSkills.size();

        double rawScore = (mustHaveScore * MUST_HAVE_WEIGHT)
                + (niceToHaveScore * NICE_TO_HAVE_WEIGHT);
        return (int) Math.round(rawScore * 100);
    }

    public String calculateLevel(int score) {
        if (score >= 80) return "Job Ready";
        if (score >= 60) return "Almost Ready";
        if (score >= 40) return "Developing";
        if (score >= 20) return "Early Stage";
        return "Beginner";
    }

    private boolean containsIgnoreCase(
            List<String> list, String target) {
        return list.stream()
                .anyMatch(s -> s.equalsIgnoreCase(target));
    }
}