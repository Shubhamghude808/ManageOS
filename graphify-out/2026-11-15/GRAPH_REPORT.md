# Graph Report - caf  (2026-11-15)

## Corpus Check
- 100 files · ~17,344 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 8 file(s) not represented in the graph (top: .css 3, .example 2, (none) 2)

## Summary
- 822 nodes · 1896 edges · 39 communities (24 shown, 15 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 115 edges (avg confidence: 0.87)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ab3c09ae`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Branch
- Backend Agent Services and Credentials
- Gaming Session Management
- PcService
- Agent Enrollment and Security Configuration
- System Documentation and Architecture Specs
- Spring Database and App Configuration
- run
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
- GamingCafeClientApp
- Frontend TypeScript Node Config
- demo_client.py
- Spring Boot Health and Context Tests
- PC Agent Windows DPAPI Credentials
- Spring Boot Application Bootstrap
- Frontend Build and Dev Scripts
- GamingPc
- Frontend TypeScript Root Config
- Backend Maven Artifact Descriptor
- GamingSession
- GamingPcRepository
- Customer
- PricingPlan

## God Nodes (most connected - your core abstractions)
1. `GamingPc` - 50 edges
2. `GamingSession` - 40 edges
3. `Branch` - 39 edges
4. `ApiResponse` - 33 edges
5. `SessionService` - 32 edges
6. `GamingPcRepository` - 29 edges
7. `run()` - 28 edges
8. `PricingPlan` - 24 edges
9. `Customer` - 23 edges
10. `AgentTokenService` - 22 edges

## Surprising Connections (you probably didn't know these)
- `Frontend HTML Entrypoint` --implements--> `React Admin Dashboard`  [INFERRED]
  frontend/index.html → docs/architecture.md
- `Windows Agent Dependencies Specification` --supports--> `Windows Service Background Mode`  [INFERRED]
  pc-agent/requirements-windows.txt → docs/windows-agent.md
- `Windows Agent Dependencies Specification` --supports--> `User-Session Tray Mode`  [INFERRED]
  pc-agent/requirements-windows.txt → docs/windows-agent.md
- `HttpApiClient` --uses--> `AgentConfig`  [INFERRED]
  pc-agent/agent/communication/http_client.py → pc-agent/agent/core/config.py
- `run()` --uses--> `AgentConfig`  [INFERRED]
  pc-agent/agent/main.py → pc-agent/agent/core/config.py

## Import Cycles
- None detected.

## Communities (39 total, 15 thin omitted)

### Community 1 - "Backend Agent Services and Credentials"
Cohesion: 0.06
Nodes (12): AgentCredentialController, BranchController, BranchDto, BranchRepository, BranchService, CafeRepository, CreateBranchRequest, ApiError (+4 more)

### Community 2 - "Gaming Session Management"
Cohesion: 0.06
Nodes (19): AgentController, HeartbeatBody, RegistrationBody, AgentAcknowledgement, AgentHeartbeatRequest, AgentRegistrationRequest, AgentService, PcHeartbeatRepository (+11 more)

### Community 3 - "PcService"
Cohesion: 0.14
Nodes (12): CreatePcBody, PcController, UpdatePcBody, CreatePcRequest, PcDto, PcService, UpdatePcRequest, PcStatus (+4 more)

### Community 4 - "Agent Enrollment and Security Configuration"
Cohesion: 0.08
Nodes (4): AgentTokenAuthenticationFilter, ApiRequestLoggingFilter, OpenApiConfig, SecurityConfig

### Community 5 - "System Documentation and Architecture Specs"
Cohesion: 0.07
Nodes (37): Agent Protocol API Routes, CafeOS API Specification, Authentication API Routes, PC Management API Routes, Session Management API Routes, Windows Agent Process, Spring Boot Modular Monolith, CafeOS Architecture Overview (+29 more)

### Community 6 - "Spring Database and App Configuration"
Cohesion: 0.06
Nodes (31): name, import, password, url, username, web, include, enabled (+23 more)

### Community 7 - "run"
Cohesion: 0.17
Nodes (6): EnrollmentClient, HttpApiClient, run(), serve(), start_agent(), serve()

### Community 8 - "Frontend Session Management View"
Cohesion: 0.14
Nodes (21): axios@^1.12.2, connect(), remainingLabel(), SessionManagement(), connect(), AgentToken, api, ApiEnvelope (+13 more)

### Community 9 - "PC Agent HTTP API Client"
Cohesion: 0.12
Nodes (7): ApiClient, HeartbeatService, AgentPayload, HeartbeatPayload, RegistrationPayload, collect_pc_info(), PayloadTests

### Community 10 - "PC Agent Machine State and Identity"
Cohesion: 0.17
Nodes (8): load_state(), machine_id(), save_state(), state_path(), read_status(), status_path(), write_status(), AgentStateTests

### Community 11 - "PC Agent Logging and Config"
Cohesion: 0.13
Nodes (5): AgentConfig, configure_logging(), main(), CafeOsAgentService, main()

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
Cohesion: 0.25
Nodes (6): CreatePricingPlanBody, PricingController, PricingPlanRepository, CreatePricingPlanRequest, PricingPlanDto, PricingService

### Community 17 - "Docker Compose and Postgres Service"
Cohesion: 0.12
Nodes (16): POSTGRES_DB, POSTGRES_PASSWORD, POSTGRES_USER, interval, retries, test, timeout, environment (+8 more)

### Community 18 - "GamingCafeClientApp"
Cohesion: 0.08
Nodes (10): disable_keyboard_hook(), enable_keyboard_hook(), is_hook_active(), KBDLLHOOKSTRUCT, _low_level_keyboard_proc(), GamingCafeClientApp, confirm(), check_admin_pin() (+2 more)

### Community 20 - "Frontend TypeScript Node Config"
Cohesion: 0.14
Nodes (13): compilerOptions, allowImportingTsExtensions, lib, module, moduleDetection, moduleResolution, noEmit, skipLibCheck (+5 more)

### Community 21 - "demo_client.py"
Cohesion: 0.33
Nodes (3): main(), main(), simulate_cashier_flow()

### Community 23 - "PC Agent Windows DPAPI Credentials"
Cohesion: 0.42
Nodes (6): clear_token(), _dpapi_path(), _keyring(), load_token(), _save_dpapi(), save_token()

### Community 25 - "Frontend Build and Dev Scripts"
Cohesion: 0.50
Nodes (4): scripts, build, check, dev

### Community 34 - "GamingSession"
Cohesion: 0.10
Nodes (6): GamingSession, SessionStatus, ACTIVE, CANCELLED, COMPLETED, PAUSED

### Community 35 - "GamingPcRepository"
Cohesion: 0.07
Nodes (8): AgentEnrollmentController, EnrollmentRequest, EnrollmentResponse, AgentLivenessService, AgentTokenService, IssuedAgentToken, GamingPcRepository, AgentTokenServiceTest

## Knowledge Gaps
- **15 isolated node(s):** `AVAILABLE`, `MAINTENANCE`, `OCCUPIED`, `OFFLINE`, `com.cafeos:cafeos-backend` (+10 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 246 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **15 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `machine_id()` connect `PC Agent Machine State and Identity` to `Backend Agent Services and Credentials`, `PC Agent Logging and Config`, `run`?**
  _High betweenness centrality (0.070) - this node is a cross-community bridge._
- **What connects `AVAILABLE`, `MAINTENANCE`, `OCCUPIED` to the rest of the system?**
  _15 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Backend Agent Services and Credentials` be split into smaller, more focused modules?**
  _Cohesion score 0.0581140350877193 - nodes in this community are weakly interconnected._
- **Why does `GamingPc` connect `GamingPc` to `Branch`, `Backend Agent Services and Credentials`, `Gaming Session Management`, `GamingPcRepository`, `PcService`, `GamingSession`, `PricingPlan`, `jakarta.persistence.PrePersist`, `uuid`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **Should `Gaming Session Management` be split into smaller, more focused modules?**
  _Cohesion score 0.06296656929568321 - nodes in this community are weakly interconnected._
- **Why does `GamingSession` connect `GamingSession` to `Branch`, `Gaming Session Management`, `Customer`, `PricingPlan`, `jakarta.persistence.PrePersist`, `uuid`, `GamingPc`?**
  _High betweenness centrality (0.047) - this node is a cross-community bridge._
- **Should `PcService` be split into smaller, more focused modules?**
  _Cohesion score 0.14245014245014245 - nodes in this community are weakly interconnected._