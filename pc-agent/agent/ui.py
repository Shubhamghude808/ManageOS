"""Interactive Windows session display for the logged-in PC user."""
import datetime as dt
import tkinter as tk
from typing import Any

from agent.services.session_status import read_status


class SessionWindow:
    def __init__(self) -> None:
        self.root = tk.Tk()
        self.root.title("CafeOS Session")
        self.root.configure(bg="#10141c")
        self.root.geometry("900x520")
        self.root.minsize(640, 360)
        self.root.attributes("-topmost", True)
        self.root.protocol("WM_DELETE_WINDOW", self.root.iconify)

        self.status = tk.StringVar(value="WAITING FOR SESSION")
        self.title = tk.StringVar(value="CafeOS")
        self.plan = tk.StringVar(value="No active session")
        self.countdown = tk.StringVar(value="--:--:--")
        self.detail = tk.StringVar(value="The PC is ready. Your session will appear here when started.")

        tk.Label(self.root, textvariable=self.title, fg="#f2f5f8", bg="#10141c", font=("Segoe UI", 26, "bold")).pack(pady=(38, 4))
        tk.Label(self.root, textvariable=self.status, fg="#38d39f", bg="#10141c", font=("Segoe UI", 12, "bold")).pack()
        tk.Label(self.root, textvariable=self.plan, fg="#aab4c2", bg="#10141c", font=("Segoe UI", 14)).pack(pady=(22, 2))
        tk.Label(self.root, textvariable=self.countdown, fg="#ffffff", bg="#10141c", font=("Consolas", 72, "bold")).pack(pady=12)
        tk.Label(self.root, textvariable=self.detail, fg="#aab4c2", bg="#10141c", font=("Segoe UI", 12), wraplength=760).pack()

        self.root.after(1000, self.refresh)

    def refresh(self) -> None:
        session = read_status()
        if not session:
            self.status.set("NO ACTIVE SESSION")
            self.plan.set("PC available")
            self.countdown.set("--:--:--")
            self.detail.set("The PC is ready. Your session will appear here when started.")
        else:
            status = str(session.get("status", "UNKNOWN"))
            self.status.set(status.replace("_", " "))
            self.plan.set(f"{session.get('pricingPlanName', 'Gaming session')} · {session.get('currency', '')} {session.get('hourlyRate', '')}/hour")
            remaining = self._remaining(session)
            self.countdown.set(self._format_seconds(remaining))
            if status == "PAUSED":
                self.detail.set("Session paused. Please contact the cafe desk to resume or extend your session.")
            elif status == "ACTIVE":
                self.detail.set(f"Started {self._format_time(session.get('startedAt'))} · Expires {self._format_time(session.get('expiresAt'))}")
            else:
                self.detail.set("Session is no longer active.")
        self.root.after(1000, self.refresh)

    @staticmethod
    def _remaining(session: dict[str, Any]) -> int:
        remaining = int(session.get("remainingSeconds") or 0)
        if session.get("status") != "ACTIVE" or not session.get("expiresAt"):
            return remaining
        try:
            expiry = dt.datetime.fromisoformat(str(session["expiresAt"]).replace("Z", "+00:00"))
            return max(0, int((expiry - dt.datetime.now(dt.timezone.utc)).total_seconds()))
        except ValueError:
            return remaining

    @staticmethod
    def _format_seconds(value: int) -> str:
        hours, remainder = divmod(max(0, value), 3600)
        minutes, seconds = divmod(remainder, 60)
        return f"{hours:02d}:{minutes:02d}:{seconds:02d}"

    @staticmethod
    def _format_time(value: Any) -> str:
        if not value:
            return "--"
        try:
            return dt.datetime.fromisoformat(str(value).replace("Z", "+00:00")).astimezone().strftime("%I:%M %p")
        except ValueError:
            return "--"


def main() -> None:
    SessionWindow().root.mainloop()


if __name__ == "__main__":
    main()
