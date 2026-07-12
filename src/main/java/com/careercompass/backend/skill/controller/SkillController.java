package com.careercompass.backend.skill.controller;

import com.careercompass.backend.skill.dto.AddSkillRequest;
import com.careercompass.backend.skill.dto.SkillResponse;
import com.careercompass.backend.skill.dto.UserSkillResponse;
import com.careercompass.backend.skill.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;
    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getSkillById(
            @PathVariable Long id) {
        return ResponseEntity.ok(skillService.getSkillById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<UserSkillResponse>> getMySkills() {
        return ResponseEntity.ok(skillService.getCurrentUserSkills());
    }

    @PostMapping("/me")
    public ResponseEntity<UserSkillResponse> addSkill(
            @Valid @RequestBody AddSkillRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(skillService.addSkillToProfile(request));
    }

    @DeleteMapping("/me/{skillId}")
    public ResponseEntity<Void> removeSkill(
            @PathVariable Long skillId) {
        skillService.removeSkillFromProfile(skillId);
        return ResponseEntity.noContent().build();
    }
}