package com.careercompass.backend.resume.service;

import com.careercompass.backend.ai.AiClient;
import com.careercompass.backend.exception.InvalidResumeException;
import com.careercompass.backend.exception.ResourceNotFoundException;
import com.careercompass.backend.resume.dto.ResumeAnalysisResponse;
import com.careercompass.backend.resume.dto.ResumeUploadResponse;
import com.careercompass.backend.resume.entity.Resume;
import com.careercompass.backend.resume.repository.ResumeRepository;
import com.careercompass.backend.security.UserPrincipal;
import com.careercompass.backend.skill.entity.Skill;
import com.careercompass.backend.skill.entity.UserSkill;
import com.careercompass.backend.skill.repository.SkillRepository;
import com.careercompass.backend.skill.repository.UserSkillRepository;
import com.careercompass.backend.user.entity.User;
import com.careercompass.backend.user.repository.UserRepository;
import com.careercompass.backend.util.PdfParserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;
    private final PdfParserUtil pdfParserUtil;
    private final AiClient aiClient;

    private static final Map<String, String> ALIASES = new HashMap<>();
    static {
        ALIASES.put("reactjs", "React.js");
        ALIASES.put("react js", "React.js");
        ALIASES.put("react", "React.js");
        ALIASES.put("nodejs", "Node.js");
        ALIASES.put("node js", "Node.js");
        ALIASES.put("node", "Node.js");
        ALIASES.put("expressjs", "Express.js");
        ALIASES.put("express js", "Express.js");
        ALIASES.put("express", "Express.js");
        ALIASES.put("nextjs", "Next.js");
        ALIASES.put("next js", "Next.js");
        ALIASES.put("vuejs", "Vue.js");
        ALIASES.put("vue js", "Vue.js");
        ALIASES.put("vue", "Vue.js");
        ALIASES.put("postgres", "PostgreSQL");
        ALIASES.put("postgresql", "PostgreSQL");
        ALIASES.put("mongo", "MongoDB");
        ALIASES.put("mongodb", "MongoDB");
        ALIASES.put("k8s", "Kubernetes");
        ALIASES.put("kubernetes", "Kubernetes");
        ALIASES.put("docker", "Docker");
        ALIASES.put("aws", "AWS");
        ALIASES.put("amazon web services", "AWS");
        ALIASES.put("gcp", "Google Cloud Platform");
        ALIASES.put("google cloud", "Google Cloud Platform");
        ALIASES.put("google cloud platform", "Google Cloud Platform");
        ALIASES.put("azure", "Microsoft Azure");
        ALIASES.put("microsoft azure", "Microsoft Azure");
        ALIASES.put("springboot", "Spring Boot");
        ALIASES.put("spring boot", "Spring Boot");
        ALIASES.put("spring-boot", "Spring Boot");
        ALIASES.put("spring framework", "Spring Framework");
        ALIASES.put("javascript", "JavaScript");
        ALIASES.put("js", "JavaScript");
        ALIASES.put("typescript", "TypeScript");
        ALIASES.put("ts", "TypeScript");
        ALIASES.put("python", "Python");
        ALIASES.put("java", "Java");
        ALIASES.put("kotlin", "Kotlin");
        ALIASES.put("swift", "Swift");
        ALIASES.put("go lang", "Go");
        ALIASES.put("golang", "Go");
        ALIASES.put("rust lang", "Rust");
        ALIASES.put("c sharp", "C#");
        ALIASES.put("c-sharp", "C#");
        ALIASES.put("dotnet", "ASP.NET Core");
        ALIASES.put(".net", "ASP.NET Core");
        ALIASES.put("asp.net", "ASP.NET Core");
        ALIASES.put("html", "HTML5");
        ALIASES.put("html 5", "HTML5");
        ALIASES.put("css", "CSS3");
        ALIASES.put("css 3", "CSS3");
        ALIASES.put("tailwind", "Tailwind CSS");
        ALIASES.put("tailwindcss", "Tailwind CSS");
        ALIASES.put("rest", "REST API");
        ALIASES.put("rest api", "REST API");
        ALIASES.put("restful", "REST API");
        ALIASES.put("restful api", "REST API");
        ALIASES.put("graphql", "GraphQL");
        ALIASES.put("kafka", "Apache Kafka");
        ALIASES.put("apache kafka", "Apache Kafka");
        ALIASES.put("redis", "Redis");
        ALIASES.put("elasticsearch", "Elasticsearch");
        ALIASES.put("git", "Git");
        ALIASES.put("github", "GitHub");
        ALIASES.put("gitlab", "GitLab");
        ALIASES.put("linux", "Linux");
        ALIASES.put("bash", "Bash");
        ALIASES.put("shell scripting", "Bash");
        ALIASES.put("terraform", "Terraform");
        ALIASES.put("ansible", "Ansible");
        ALIASES.put("jenkins", "Jenkins");
        ALIASES.put("github actions", "GitHub Actions");
        ALIASES.put("ci/cd", "GitHub Actions");
        ALIASES.put("microservices", "Microservices");
        ALIASES.put("micro services", "Microservices");
        ALIASES.put("jwt", "JWT");
        ALIASES.put("oauth", "OAuth 2.0");
        ALIASES.put("oauth2", "OAuth 2.0");
        ALIASES.put("machine learning", "Machine Learning");
        ALIASES.put("ml", "Machine Learning");
        ALIASES.put("deep learning", "Deep Learning");
        ALIASES.put("dl", "Deep Learning");
        ALIASES.put("nlp", "Natural Language Processing");
        ALIASES.put("natural language processing", "Natural Language Processing");
        ALIASES.put("tensorflow", "TensorFlow");
        ALIASES.put("pytorch", "PyTorch");
        ALIASES.put("scikit-learn", "Scikit-learn");
        ALIASES.put("sklearn", "Scikit-learn");
        ALIASES.put("pandas", "Pandas");
        ALIASES.put("numpy", "NumPy");
        ALIASES.put("flutter", "Flutter");
        ALIASES.put("react native", "React Native");
        ALIASES.put("android", "Android Development");
        ALIASES.put("ios", "iOS Development");
        ALIASES.put("swift ui", "SwiftUI");
        ALIASES.put("jetpack compose", "Jetpack Compose");
        ALIASES.put("mysql", "MySQL");
        ALIASES.put("sqlite", "SQLite");
        ALIASES.put("dynamodb", "DynamoDB");
        ALIASES.put("firebase", "Firestore");
        ALIASES.put("firestore", "Firestore");
        ALIASES.put("prometheus", "Prometheus");
        ALIASES.put("grafana", "Grafana");
        ALIASES.put("helm", "Helm");
        ALIASES.put("nginx", "Nginx");
        ALIASES.put("apache", "Apache");
        ALIASES.put("junit", "JUnit");
        ALIASES.put("mockito", "Mockito");
        ALIASES.put("jest", "Jest");
        ALIASES.put("selenium", "Selenium");
        ALIASES.put("cypress", "Cypress");
        ALIASES.put("playwright", "Playwright");
        ALIASES.put("postman", "Postman");
        ALIASES.put("agile", "Agile / Scrum");
        ALIASES.put("scrum", "Agile / Scrum");
        ALIASES.put("system design", "System Design");
        ALIASES.put("design patterns", "Design Patterns");
        ALIASES.put("solid principles", "Design Patterns");
        ALIASES.put("oops", "Design Patterns");
        ALIASES.put("oop", "Design Patterns");
        ALIASES.put("hibernate", "Spring Framework");
        ALIASES.put("jpa", "Spring Framework");
        ALIASES.put("maven", "Tools & Practices");
        ALIASES.put("gradle", "Tools & Practices");
        ALIASES.put("django", "Django");
        ALIASES.put("flask", "Flask");
        ALIASES.put("fastapi", "FastAPI");
        ALIASES.put("nestjs", "NestJS");
        ALIASES.put("nest js", "NestJS");
        ALIASES.put("grpc", "gRPC");
        ALIASES.put("websocket", "WebSockets");
        ALIASES.put("websockets", "WebSockets");
        ALIASES.put("rabbitm", "RabbitMQ");
        ALIASES.put("rabbitmq", "RabbitMQ");
        ALIASES.put("solidity", "Solidity");
        ALIASES.put("blockchain", "Blockchain");
        ALIASES.put("unity", "Unity");
        ALIASES.put("unreal engine", "Unreal Engine");
        ALIASES.put("c++", "C++");
        ALIASES.put("cpp", "C++");
        ALIASES.put("scala", "Scala");
        ALIASES.put("ruby", "Ruby");
        ALIASES.put("ruby on rails", "Ruby on Rails");
        ALIASES.put("rails", "Ruby on Rails");
        ALIASES.put("php", "PHP");
        ALIASES.put("laravel", "Laravel");
        ALIASES.put("r language", "R");
        ALIASES.put("r programming", "R");
        ALIASES.put("power bi", "Power BI");
        ALIASES.put("tableau", "Tableau");
        ALIASES.put("spark", "Apache Spark");
        ALIASES.put("apache spark", "Apache Spark");
        ALIASES.put("hadoop", "Hadoop");
        ALIASES.put("airflow", "Airflow");
        ALIASES.put("mlflow", "MLflow");
        ALIASES.put("hugging face", "Hugging Face");
        ALIASES.put("langchain", "LangChain");
        ALIASES.put("opengl", "OpenGL");
        ALIASES.put("embedded", "Embedded Systems");
        ALIASES.put("arduino", "Arduino");
        ALIASES.put("raspberry pi", "Raspberry Pi");
        ALIASES.put("iot", "IoT");
    }

    @Override
    public ResumeUploadResponse uploadResume(MultipartFile file) {

        if (file.isEmpty()) throw new InvalidResumeException("Please upload a file.");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new InvalidResumeException("Only PDF files are supported.");
        }

        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        String extractedText = pdfParserUtil.extractText(file);
        if (extractedText == null || extractedText.isBlank()) {
            throw new InvalidResumeException(
                    "Could not extract text from the PDF. " +
                            "Ensure the PDF is not scanned/image-based.");
        }

        Resume resume = resumeRepository.findByUserId(userId).orElse(new Resume());
        resume.setUser(user);
        resume.setOriginalFileName(originalFilename);
        resume.setFileStorageKey(userId + "_resume.pdf");
        resume.setExtractedText(extractedText);
        resume.setUploadedAt(LocalDateTime.now());
        Resume savedResume = resumeRepository.save(resume);

        List<String> aiSkillNames = extractSkillsWithAi(extractedText);
        log.info("AI extracted {} skill names: {}", aiSkillNames.size(), aiSkillNames);

        List<Skill> matchedSkills = strictMatchAgainstCatalogue(aiSkillNames);
        log.info("Strictly matched {} skills from catalogue", matchedSkills.size());

        int newSkillsAdded = 0;
        for (Skill skill : matchedSkills) {
            if (!userSkillRepository.existsByUserIdAndSkillId(userId, skill.getId())) {
                userSkillRepository.save(UserSkill.builder()
                        .user(user).skill(skill)
                        .proficiencyLevel("INTERMEDIATE")
                        .source("RESUME_PARSED")
                        .build());
                newSkillsAdded++;
            }
        }

        return ResumeUploadResponse.builder()
                .resumeId(savedResume.getId())
                .originalFileName(savedResume.getOriginalFileName())
                .uploadedAt(savedResume.getUploadedAt())
                .message(newSkillsAdded + " new skills extracted. "
                        + matchedSkills.size() + " total matched from catalogue.")
                .build();
    }

    @Override
    public ResumeAnalysisResponse getResumeAnalysis() {
        Long userId = getCurrentUserId();

        Resume resume = resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No resume found. Please upload your resume first."));

        List<String> extractedSkills = userSkillRepository.findByUserId(userId)
                .stream()
                .filter(us -> "RESUME_PARSED".equals(us.getSource()))
                .map(us -> us.getSkill().getName())
                .collect(Collectors.toList());

        String summaryPrompt = """
                You are a professional resume reviewer.
                Analyze this resume text and provide a concise 2-3 sentence summary
                focusing on: experience level, key technical strengths, and notable skills.
                Be specific and professional. Do not mention names.
                
                Resume text:
                """ + resume.getExtractedText().substring(0,
                Math.min(3000, resume.getExtractedText().length()));

        String summary = aiClient.chat(summaryPrompt);

        return ResumeAnalysisResponse.builder()
                .resumeId(resume.getId())
                .originalFileName(resume.getOriginalFileName())
                .extractedSkills(extractedSkills)
                .summary(summary)
                .build();
    }


    private List<String> extractSkillsWithAi(String resumeText) {
        String truncated = resumeText.substring(0, Math.min(4000, resumeText.length()));

        String prompt = """
                You are an expert technical recruiter.
                
                Extract ONLY the technical skills, tools, technologies, frameworks, languages,
                and platforms that are EXPLICITLY mentioned in this resume text.
                
                CRITICAL RULES:
                - ONLY extract skills that are ACTUALLY written in the resume
                - Do NOT infer or guess skills that are not explicitly mentioned
                - Do NOT add skills based on job titles or experience descriptions alone
                - Do NOT include soft skills (leadership, communication, teamwork, etc.)
                - Do NOT include generic terms (software development, programming, coding)
                - Use standard names: "React.js" not "ReactJS", "PostgreSQL" not "Postgres"
                - Return ONLY a comma-separated list — no bullets, no numbers, no explanations
                
                Example good output:
                Java, Spring Boot, PostgreSQL, Docker, React.js, AWS, Git, REST API, Redis, Kafka
                
                Resume text:
                """ + truncated;

        try {
            String response = aiClient.chat(prompt);
            String cleaned = response
                    .replaceAll("(?s)```.*?```", "")
                    .replaceAll("[*_`#]", "")
                    .replaceAll("(?m)^[-•·]\\s*", "")
                    .trim();

            if (!cleaned.contains(",") && cleaned.contains("\n")) {
                cleaned = cleaned.replace("\n", ",");
            }

            return Arrays.stream(cleaned.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .filter(s -> s.length() >= 2 && s.length() <= 50)
                    .filter(s -> !s.matches(".*\\d{4}.*"))
                    .filter(s -> !s.toLowerCase().matches(
                            ".*(experience|years|proficient|knowledge|familiar|understanding|responsible).*"))
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("AI extraction failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }


    private List<Skill> strictMatchAgainstCatalogue(List<String> aiNames) {
        // Build catalogue lookup map: lowercase name → Skill entity
        List<Skill> catalogue = skillRepository.findAll();
        Map<String, Skill> catalogueByLowerName = new LinkedHashMap<>();
        for (Skill s : catalogue) {
            catalogueByLowerName.put(s.getName().toLowerCase(), s);
        }

        Set<Long> addedIds = new LinkedHashSet<>();
        List<Skill> result = new ArrayList<>();

        for (String aiName : aiNames) {
            String lower = aiName.toLowerCase().trim();

            Skill match = catalogueByLowerName.get(lower);

            if (match == null) {
                String canonicalName = ALIASES.get(lower);
                if (canonicalName != null) {
                    match = catalogueByLowerName.get(canonicalName.toLowerCase());
                }
            }

            if (match != null && !addedIds.contains(match.getId())) {
                result.add(match);
                addedIds.add(match.getId());
            }
        }

        return result;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((UserPrincipal) auth.getPrincipal()).getId();
    }
}