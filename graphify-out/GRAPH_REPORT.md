# Graph Report - caf  (2026-11-15)

## Corpus Check
- Corpus is ~14,617 words - fits in a single context window. You may not need a graph.

## Summary
- 762 nodes · 1748 edges · 34 communities (23 shown, 11 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 111 edges (avg confidence: 0.87)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Java Unit Tests and Assertions
- Backend Agent Services and Credentials
- Gaming Session Management
- Agent Liveness and Health Tracking
- Agent Enrollment and Security Configuration
- System Documentation and Architecture Specs
- Spring Database and App Configuration
- PC Agent Enrollment Client
- Frontend Session Management View
- PC Agent HTTP API Client
- PC Agent Machine State and Identity
- PC Agent Logging and Config
- Frontend Dependencies and Tailwind Setup
- Frontend React Application Layout
- Frontend TypeScript App Config
- Frontend UI Libraries and Router
- Pricing Plan Domain and Endpoints
- Docker Compose and Postgres Service
- Backend Agent Heartbeat Endpoints
- Global API Error Handling
- Frontend TypeScript Node Config
- PC Agent Windows Session UI
- Spring Boot Health and Context Tests
- PC Agent Windows DPAPI Credentials
- Spring Boot Application Bootstrap
- Frontend Build and Dev Scripts
- PC Agent Host Info Collection
- Frontend TypeScript Root Config
- Backend Maven Artifact Descriptor

## God Nodes (most connected - your core abstractions)
1. `GamingPc` - 50 edges
2. `GamingSession` - 37 edges
3. `Branch` - 35 edges
4. `ApiResponse` - 33 edges
5. `SessionService` - 31 edges
6. `GamingPcRepository` - 29 edges
7. `run()` - 28 edges
8. `PricingPlan` - 24 edges
9. `AgentTokenService` - 22 edges
10. `BranchRepository` - 21 edges

## Surprising Connections (you probably didn't know these)
- `Frontend HTML Entrypoint` --implements--> `React Admin Dashboard`  [INFERRED]
  frontend/index.html → docs/architecture.md
- `Windows Agent Dependencies Specification` --supports--> `Windows Service Background Mode`  [INFERRED]
  pc-agent/requirements-windows.txt → docs/windows-agent.md
- `Windows Agent Dependencies Specification` --supports--> `User-Session Tray Mode`  [INFERRED]
  pc-agent/requirements-windows.txt → docs/windows-agent.md
- `SessionManagement()` --indirect_call--> `getBranches()`  [INFERRED]
  frontend/src/features/SessionManagement.tsx → frontend/src/services/api.ts
- `CafeOS Overview` --references--> `CafeOS Architecture Overview`  [EXTRACTED]
  README.md → docs/architecture.md

## Import Cycles
- None detected.

## Communities (34 total, 11 thin omitted)

### Community 0 - "Java Unit Tests and Assertions"
Cohesion: 0.05
Nodes (6): PcHeartbeat, Branch, Cafe, GamingPc, PricingPlan, SessionBillingCalculatorTest

### Community 1 - "Backend Agent Services and Credentials"
Cohesion: 0.07
Nodes (10): AgentCredentialController, PcHeartbeatRepository, BranchController, BranchDto, BranchRepository, BranchService, CafeRepository, CreateBranchRequest (+2 more)

### Community 2 - "Gaming Session Management"
Cohesion: 0.06
Nodes (15): GamingSession, GamingSessionRepository, SessionBillingCalculator, ExtendSessionBody, SessionController, StartSessionBody, SessionExpiryService, SessionDto (+7 more)

### Community 3 - "Agent Liveness and Health Tracking"
Cohesion: 0.07
Nodes (16): AgentLivenessService, IssuedAgentToken, GamingPcRepository, CreatePcBody, PcController, UpdatePcBody, CreatePcRequest, PcDto (+8 more)

### Community 4 - "Agent Enrollment and Security Configuration"
Cohesion: 0.07
Nodes (8): AgentEnrollmentController, EnrollmentRequest, EnrollmentResponse, AgentTokenAuthenticationFilter, AgentTokenService, ApiRequestLoggingFilter, OpenApiConfig, SecurityConfig

### Community 5 - "System Documentation and Architecture Specs"
Cohesion: 0.07
Nodes (37): Agent Protocol API Routes, CafeOS API Specification, Authentication API Routes, PC Management API Routes, Session Management API Routes, Windows Agent Process, Spring Boot Modular Monolith, CafeOS Architecture Overview (+29 more)

### Community 6 - "Spring Database and App Configuration"
Cohesion: 0.06
Nodes (31): name, import, password, url, username, web, include, enabled (+23 more)

### Community 7 - "PC Agent Enrollment Client"
Cohesion: 0.11
Nodes (8): EnrollmentClient, HttpApiClient, AgentConfig, run(), CafeOsAgentService, serve(), start_agent(), serve()

### Community 8 - "Frontend Session Management View"
Cohesion: 0.14
Nodes (21): axios@^1.12.2, connect(), remainingLabel(), SessionManagement(), connect(), AgentToken, api, ApiEnvelope (+13 more)

### Community 9 - "PC Agent HTTP API Client"
Cohesion: 0.14
Nodes (6): ApiClient, HeartbeatService, AgentPayload, HeartbeatPayload, RegistrationPayload, PayloadTests

### Community 10 - "PC Agent Machine State and Identity"
Cohesion: 0.21
Nodes (8): load_state(), machine_id(), save_state(), state_path(), read_status(), status_path(), write_status(), AgentStateTests

### Community 11 - "PC Agent Logging and Config"
Cohesion: 0.16
Nodes (3): configure_logging(), main(), main()

### Community 12 - "Frontend Dependencies and Tailwind Setup"
Cohesion: 0.14
Nodes (19): devDependencies, tailwindcss@^4.1.16, @tailwindcss/vite@^4.1.16, @types/react@^19.1.16, @types/react-dom@^19.1.9, typescript@~5.9.3, vite@^7.1.12, @vitejs/plugin-react@^5.0.4 (+11 more)

### Community 13 - "Frontend React Application Layout"
Cohesion: 0.17
Nodes (15): App(), Dashboard(), nav, PcManagement(), Placeholder(), createBranch(), createPc(), deletePc() (+7 more)

### Community 14 - "Frontend TypeScript App Config"
Cohesion: 0.10
Nodes (19): compilerOptions, allowImportingTsExtensions, erasableSyntaxOnly, jsx, lib, module, moduleDetection, moduleResolution (+11 more)

### Community 15 - "Frontend UI Libraries and Router"
Cohesion: 0.13
Nodes (16): dependencies, lucide-react@^0.468.0, react@^19.1.1, react-dom@^19.1.1, react-hook-form@^7.62.0, react-router-dom@^7.9.4, @tanstack/react-query@^5.90.2, zod@^4.1.12 (+8 more)

### Community 16 - "Pricing Plan Domain and Endpoints"
Cohesion: 0.23
Nodes (6): CreatePricingPlanBody, PricingController, PricingPlanRepository, CreatePricingPlanRequest, PricingPlanDto, PricingService

### Community 17 - "Docker Compose and Postgres Service"
Cohesion: 0.12
Nodes (16): POSTGRES_DB, POSTGRES_PASSWORD, POSTGRES_USER, interval, retries, test, timeout, environment (+8 more)

### Community 18 - "Backend Agent Heartbeat Endpoints"
Cohesion: 0.31
Nodes (7): AgentController, HeartbeatBody, RegistrationBody, AgentAcknowledgement, AgentHeartbeatRequest, AgentRegistrationRequest, AgentService

### Community 19 - "Global API Error Handling"
Cohesion: 0.28
Nodes (3): ApiError, ApiResponse, GlobalExceptionHandler

### Community 20 - "Frontend TypeScript Node Config"
Cohesion: 0.14
Nodes (13): compilerOptions, allowImportingTsExtensions, lib, module, moduleDetection, moduleResolution, noEmit, skipLibCheck (+5 more)

### Community 23 - "PC Agent Windows DPAPI Credentials"
Cohesion: 0.42
Nodes (6): clear_token(), _dpapi_path(), _keyring(), load_token(), _save_dpapi(), save_token()

### Community 25 - "Frontend Build and Dev Scripts"
Cohesion: 0.50
Nodes (4): scripts, build, check, dev

## Knowledge Gaps
- **15 isolated node(s):** `com.cafeos:cafeos-backend`, `AVAILABLE`, `OCCUPIED`, `OFFLINE`, `MAINTENANCE` (+10 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 228 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **11 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `machine_id()` connect `PC Agent Machine State and Identity` to `Backend Agent Services and Credentials`, `PC Agent Logging and Config`, `PC Agent Enrollment Client`?**
  _High betweenness centrality (0.079) - this node is a cross-community bridge._
- **What connects `com.cafeos:cafeos-backend`, `AVAILABLE`, `OCCUPIED` to the rest of the system?**
  _15 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Java Unit Tests and Assertions` be split into smaller, more focused modules?**
  _Cohesion score 0.050140056022408966 - nodes in this community are weakly interconnected._
- **Why does `GamingPc` connect `Java Unit Tests and Assertions` to `Gaming Session Management`, `Backend Agent Services and Credentials`, `Backend Agent Heartbeat Endpoints`, `Agent Liveness and Health Tracking`?**
  _High betweenness centrality (0.062) - this node is a cross-community bridge._
- **Should `Backend Agent Services and Credentials` be split into smaller, more focused modules?**
  _Cohesion score 0.06553041434028799 - nodes in this community are weakly interconnected._
- **Why does `AgentTokenService` connect `Agent Enrollment and Security Configuration` to `Backend Agent Services and Credentials`, `Agent Liveness and Health Tracking`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._
- **Should `Gaming Session Management` be split into smaller, more focused modules?**
  _Cohesion score 0.061815336463223784 - nodes in this community are weakly interconnected._