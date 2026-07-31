<div align="center">

<img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
<img src="https://img.shields.io/badge/PostgreSQL-17-316192?style=for-the-badge&logo=postgresql&logoColor=white"/>
<img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"/>

# 🧭 CareerCompass — Backend

### AI-Powered Career Intelligence Platform

*Know exactly where you stand. Know exactly what to do next.*

[🌐 Live Demo](https://career-compass-frontend-two.vercel.app) · [📁 Frontend Repo](https://github.com/codemasteryash/CareerCompass-Frontend) · [🐛 Report Bug](https://github.com/codemasteryash/CareerCompass/issues)

</div>

---

## 📖 About

CareerCompass is a full-stack AI-powered career intelligence platform. Users upload their resume, select a target job role, and receive a complete career intelligence report:

- **Skill Gap Analysis** — exact missing skills, separated by priority
- **Job Readiness Score** — a precise 0–100 score with AI feedback
- **Personalized Learning Roadmap** — AI-generated, step-by-step learning path
- **Certification Recommendations** — AI picks the most relevant certs for your gaps
- **Project Suggestions** — portfolio projects tailored to your role
- **AI Career Mentor** — a conversational AI that knows your profile

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    React Frontend (Vercel)                │
└─────────────────────┬───────────────────────────────────┘
                      │ HTTPS + JWT
┌─────────────────────▼───────────────────────────────────┐
│              Spring Boot Backend (Render)                 │
│                                                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐ │
│  │   Auth   │  │  Skills  │  │ Analysis │  │ Roadmap │ │
│  │ Module   │  │  Module  │  │  Module  │  │ Module  │ │
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘ │
│                                                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │         AiClient Interface (GroqClient)           │   │
│  └──────────────────────────────────────────────────┘   │
└──────┬──────────────────────────────────┬───────────────┘
       │                                  │
┌──────▼──────┐                  ┌────────▼────────┐
│  Supabase   │                  │   Groq API       │
│ PostgreSQL  │                  │ LLaMA 3.3 70B    │
└─────────────┘                  └─────────────────┘
```

---

## 🧩 Module Map

| Module | Responsibility | Has DB? |
|--------|---------------|---------|
| `auth` | Registration, login, JWT issuance | ❌ |
| `user` | Profile management | ✅ |
| `skill` | Skill catalogue + user skill profile | ✅ |
| `role` | Job role catalogue + required skills | ✅ |
| `resume` | PDF upload, AI skill extraction | ✅ |
| `certification` | AI cert recommendations + tracking | ✅ |
| `projectrecommendation` | AI project suggestions | ✅ |
| `analysis` | Skill gap + readiness score (live computation) | ❌ |
| `roadmap` | AI roadmap generation + progress tracking | ✅ |
| `mentor` | Stateless AI career mentor chat | ❌ |
| `dashboard` | Aggregation facade | ❌ |
| `notification` | Async email via Resend API | ❌ |
| `security` | JWT filter chain, Spring Security config | ❌ |
| `ai` | AiClient interface + GroqClient impl | ❌ |

---

## ⚙️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Security | Spring Security 6 + JWT (JJWT 0.12.6) |
| AI Integration | Spring AI 2.0.0-M8 → Groq (LLaMA 3.3 70B) |
| Database | PostgreSQL 17 via Supabase |
| ORM | Spring Data JPA + Hibernate 7 |
| Connection Pool | HikariCP |
| PDF Parsing | Apache PDFBox 3.0.3 |
| Email | Resend API |
| Build | Maven 3.9 |
| Containerization | Docker (multi-stage build) |
| Deployment | Render (Free tier) |

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL (local) or Supabase account
- Groq API key (free at [console.groq.com](https://console.groq.com))

### 1. Clone the repository

```bash
git clone https://github.com/codemasteryash/CareerCompass.git
cd CareerCompass
```

### 2. Set environment variables

In IntelliJ → Edit Configurations → Environment Variables:

```
DB_PORT=5432
DB_NAME=careercompass
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password
JWT_SECRET=your_base64_64byte_secret
JWT_EXPIRATION=604800000
GROQ_API_KEY=gsk_your_groq_key
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your_app_password
```

Generate JWT secret:
```bash
openssl rand -base64 64
```

### 3. Create local database

```sql
CREATE DATABASE careercompass;
```

### 4. Run the application

```bash
mvn spring-boot:run
```

Backend starts at `http://localhost:8080`

### 5. Seed the database

After first startup (Hibernate creates tables automatically), run `seed_data.sql` in pgAdmin or psql:

```bash
psql -U postgres -d careercompass -f seed_data.sql
```

---

## 🔐 API Reference

All protected endpoints require: `Authorization: Bearer <token>`

### Auth
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | ❌ | Register new user |
| POST | `/api/auth/login` | ❌ | Login, receive JWT |
| POST | `/api/auth/logout` | ❌ | Client-side logout |

### Skills
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/skills` | ✅ | All skills in catalogue |
| GET | `/api/skills/me` | ✅ | Current user's skills |
| POST | `/api/skills/me` | ✅ | Add skill to profile |
| DELETE | `/api/skills/me/{id}` | ✅ | Remove skill |

### Analysis
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/analysis` | ✅ | Full skill gap + readiness score |
| GET | `/api/dashboard` | ✅ | Aggregated career snapshot |

### Roadmap
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/roadmap` | ✅ | Get/generate roadmap |
| PATCH | `/api/roadmap/progress` | ✅ | Update step status |

### AI Features
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/mentor/chat` | ✅ | AI career mentor chat |
| GET | `/api/certifications/recommendations` | ✅ | AI cert picks |
| GET | `/api/projects/recommendations` | ✅ | AI project ideas |

---

## 🐳 Docker

```bash
# Build image
docker build -t careercompass-backend .

# Run container
docker run -p 8080:8080 \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_secret \
  -e GROQ_API_KEY=your_key \
  -e SPRING_PROFILES_ACTIVE=prod \
  careercompass-backend
```

---

## 🌍 Deployment (Render)

1. Push to GitHub
2. New Web Service → select repo → Language: **Docker**
3. Add environment variables in Render dashboard
4. Deploy — Render builds Docker image and starts the container
5. Seed database via Supabase SQL editor after first startup

**Keep-alive:** Set up UptimeRobot to ping `/actuator/health` every 14 minutes to prevent Render's 15-minute sleep.

---

## 🧠 Key Design Decisions

**AiClient Interface**
All AI calls go through a single `AiClient` interface. The provider was switched OpenAI → Gemini → Groq during development with zero changes to any service class — only the implementation changed.

**Stateless JWT Authentication**
No server-side sessions. Every request carries a self-contained JWT. The `JwtAuthenticationFilter` validates it on every request and populates the `SecurityContextHolder`.

**Roadmap Caching**
AI generates a roadmap once per job role and saves it to the database. All subsequent users targeting the same role get the pre-generated roadmap instantly — no repeated AI calls.

**Lazy Loading + JOIN FETCH**
`open-in-view=false` forces all lazy relationships to be resolved inside service methods. Repositories that need related entities use `@Query` with `JOIN FETCH` to load them in a single SQL query.

**Resume Skill Extraction**
A two-step process: AI extracts skill names from resume text, then a strict exact + curated alias matching maps them to the skills catalogue. No fuzzy substring matching prevents false positives.

---

## 📁 Project Structure

```
src/main/java/com/careercompass/backend/
├── ai/                    # AiClient interface + GroqClient
├── analysis/              # Skill gap + readiness score
├── auth/                  # Registration + login
├── certification/         # AI cert recommendations + tracking
├── config/                # CORS, OpenAPI, Application config
├── dashboard/             # Aggregation facade
├── exception/             # GlobalExceptionHandler + custom exceptions
├── mentor/                # AI career mentor chat
├── notification/          # Async email (Resend)
├── projectrecommendation/ # AI project suggestions
├── resume/                # PDF upload + AI skill extraction
├── roadmap/               # AI roadmap + progress tracking
├── role/                  # Job role catalogue
├── security/              # JWT filter chain + Spring Security
├── skill/                 # Skill catalogue + user skills
├── user/                  # User profile management
└── util/                  # PdfParserUtil, SkillExtractorUtil, ReadinessCalculatorUtil
```

---

## 👨‍💻 Author

**Yash Gupta**
B.Tech Computer Science Engineering · MSIT Delhi · CGPA 9.4

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0077B5?style=flat&logo=linkedin)](https://linkedin.com/in/yash-gupta-603f7630a)
[![GitHub](https://img.shields.io/badge/GitHub-Follow-181717?style=flat&logo=github)](https://github.com/codemasteryash)

---

<div align="center">
⭐ Star this repo if you found it useful!
</div>
