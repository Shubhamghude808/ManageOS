# CafeOS Windows Agent

Python 3.12 agent for Windows 10/11. It registers the PC, sends CPU/memory heartbeats, retries network errors with bounded exponential backoff, and can run as a foreground process, Windows Service, or optional user-session tray process.

The interactive PC-user session display runs separately with `python -m agent.ui`. It shows the current session status and server-authoritative countdown from the snapshot produced by the background agent. See `../docs/windows-agent.md` for setup and deployment details.

Create a local `.env` from `.env.example`, set the server URL, and then run:

```powershell
py -3.12 -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python -m agent.main
```

Configuration uses `CAFEOS_SERVER_URL`, optional `CAFEOS_BRANCH_ID`, `CAFEOS_PC_ID`, `CAFEOS_AGENT_TOKEN`, `CAFEOS_HEARTBEAT_INTERVAL_SECONDS`, and `CAFEOS_LOG_LEVEL`. On first launch the agent creates or claims its PC automatically; when the server has one branch, it is added there automatically, and `CAFEOS_BRANCH_ID` is required when there are multiple branches. The agent generates a persistent machine ID and stores it with the returned token in `%LOCALAPPDATA%\CafeOS\agent-state.json`. Never commit credentials. Use a trusted LAN only and see `../docs/windows-agent.md` for limits and next steps.
