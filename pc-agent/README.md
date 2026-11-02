# CafeOS Windows Agent (pilot process)

Python 3.12 foreground process for Windows 10/11. It registers the provisioned PC, sends CPU/memory heartbeats every 15 seconds, and retries network errors with bounded exponential backoff. It does not install a Windows service or tray UI yet.

Create a local `.env` from `.env.example`, fill it with the one-time token config from CafeOS, then run:

```powershell
py -3.12 -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python -m agent.main
```

Configuration uses `CAFEOS_SERVER_URL`, `CAFEOS_PC_ID`, `CAFEOS_AGENT_TOKEN`, `CAFEOS_HEARTBEAT_INTERVAL_SECONDS`, and `CAFEOS_LOG_LEVEL`. Never commit credentials. The token is plaintext in this pilot `.env`; use a trusted LAN only and see `../docs/windows-agent.md` for limits and next steps.
