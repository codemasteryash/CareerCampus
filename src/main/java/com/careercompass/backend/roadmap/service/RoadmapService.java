package com.careercompass.backend.roadmap.service;

import com.careercompass.backend.roadmap.dto.ProgressResponse;
import com.careercompass.backend.roadmap.dto.RoadmapResponse;
import com.careercompass.backend.roadmap.dto.UpdateProgressRequest;

public interface RoadmapService {

    RoadmapResponse getMyRoadmap();

    ProgressResponse updateStepProgress(UpdateProgressRequest request);
}