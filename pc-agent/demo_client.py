"""Interactive Demo for CafeOS Gaming Client & User Profile Experience.

Demonstrates the 3-state gaming cafe cycle:
1. Station Available (Hardened Kiosk Lock Screen)
2. Cashier Activation -> Screen Unlocks to In-Game Gamer HUD (Profile & Live Timer)
3. Step-Away Lock -> 4-digit PIN Unlock
4. Session Expiry -> Relocks Station

Usage:
  python demo_client.py
"""
import datetime as dt
import os
import subprocess
import sys
import threading
import time
from pathlib import Path

# Add pc-agent to sys.path
sys.path.insert(0, str(Path(__file__).resolve().parent))

from agent.services.session_status import write_status


def simulate_cashier_flow() -> None:
    """Simulates cashier starting, extending, and ending a session from dashboard."""
    print("\n[Demo] 1. Initial State: PC is IDLE and LOCKED.")
    print("       (Kiosk displays station branding, pricing rates, and blocks desktop access)")
    write_status(None)
    time.sleep(6)

    print("\n[Demo] 2. Cashier starts session on Admin Dashboard for 'Alex' (VIP Gaming, 1 hr)...")
    expiry = (dt.datetime.now(dt.timezone.utc) + dt.timedelta(seconds=3600)).isoformat()
    session_data = {
        "status": "ACTIVE",
        "customerName": "Alex (Member)",
        "pcName": "PC-01",
        "pricingPlanName": "VIP Gaming Lounge",
        "hourlyRate": "100.00",
        "currency": "INR",
        "remainingSeconds": 3600,
        "expiresAt": expiry,
        "startedAt": dt.datetime.now(dt.timezone.utc).isoformat(),
    }
    write_status(session_data)
    print("       -> Kiosk automatically unlocks! Keyboard hook is released.")
    print("       -> Gamer HUD appears in top-right corner with player profile and live timer.")
    print("       -> Try dragging the HUD, toggling pill mode (—), or clicking 'Step Away'!")
    time.sleep(15)

    print("\n[Demo] 3. Simulating low-time warning (under 5 minutes remaining)...")
    low_expiry = (dt.datetime.now(dt.timezone.utc) + dt.timedelta(seconds=295)).isoformat()
    session_data["remainingSeconds"] = 295
    session_data["expiresAt"] = low_expiry
    write_status(session_data)
    print("       -> HUD timer turns orange/red and plays warning chime.")
    time.sleep(10)

    print("\n[Demo] 4. Session ends / expires...")
    write_status(None)
    print("       -> HUD closes. Fullscreen Kiosk Lock Screen re-engages.")
    print("       -> Press Ctrl+Shift+F12 (PIN 7777) to close maintenance kiosk when done.\n")


def main() -> None:
    print("=" * 65)
    print("      CafeOS - Gaming Cafe PC Client & User Profile Demo")
    print("=" * 65)
    print("Emergency Staff Unlock: Press Ctrl+Shift+F12 and enter PIN 7777")
    print("Starting client UI in 2 seconds...")
    time.sleep(2)

    # Start the automated cashier simulation thread
    sim_thread = threading.Thread(target=simulate_cashier_flow, daemon=True)
    sim_thread.start()

    # Launch the real client UI
    from agent.ui import main as run_ui
    run_ui()


if __name__ == "__main__":
    main()
