# CafeOS Windows Agent

Python 3.12 agent for Windows 10/11. It registers the PC, sends CPU/memory heartbeats, retries network errors with bounded exponential backoff, and can run as a foreground process, Windows Service, or optional user-session tray process.

The interactive PC-user session client runs with `python -m agent.ui`. It acts as a full gaming cafe station client:
1. **Hardened Kiosk Lock Screen**: Fullscreen borderless window blocking Windows navigation keys (`WinKey`, `Alt+Tab`, `Alt+Esc`, `Ctrl+Esc`, `Alt+F4`) when station is idle.
2. **In-Game Gamer Profile & HUD**: Automatically unlocks Windows when cashier starts a session, displaying player profile (`👤 Alex`), live authoritative countdown (`HH:MM:SS`), and minimizable pill mode.
3. **Step-Away Mode**: Gamers can click `🔒 Step Away` to lock their PC with a 4-digit PIN while stepping out for food/restroom.
4. **Interactive Demo**: Run `python demo_client.py` to watch an automated end-to-end simulation of station locking, remote cashier unlock, Gamer HUD, low-time warning, and session expiration.
5. **Staff Emergency Bypass**: Press `Ctrl+Shift+F12` and enter PIN `7777` to minimize/close the kiosk during staff maintenance.

See `../docs/windows-agent.md` for setup and deployment details.

Create a local `.env` from `.env.example`, set the server URL, and then run:

```powershell
py -3.12 -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python -m agent.main
```

Configuration uses `CAFEOS_SERVER_URL`, optional `CAFEOS_BRANCH_ID`, `CAFEOS_PC_ID`, `CAFEOS_AGENT_TOKEN`, `CAFEOS_HEARTBEAT_INTERVAL_SECONDS`, and `CAFEOS_LOG_LEVEL`. On first launch the agent creates or claims its PC automatically; when the server has one branch, it is added there automatically, and `CAFEOS_BRANCH_ID` is required when there are multiple branches. The agent generates a persistent machine ID and stores it with the returned token in `%LOCALAPPDATA%\CafeOS\agent-state.json`. Never commit credentials. Use a trusted LAN only and see `../docs/windows-agent.md` for limits and next steps.
