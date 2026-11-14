"""Gaming Cafe PC Agent Client & User Profile Shell.

Provides:
1. Fullscreen Hardened Kiosk Lock Screen when station is idle/available.
2. Unlocked desktop with in-game Gamer HUD & profile overlay during active sessions.
3. Temporary Step-Away lock mode for gamer breaks.
4. Low-level Windows keyboard navigation key suppression while locked.
"""
import datetime as dt
import logging
import os
import platform
import tkinter as tk
from tkinter import messagebox
from typing import Any

try:
    from agent.core.config import AgentConfig
    _cfg = AgentConfig()
    DEFAULT_HARDENED = _cfg.kiosk_hardened
    DEFAULT_ADMIN_PIN = _cfg.admin_pin
except Exception:
    DEFAULT_HARDENED = os.environ.get("CAFEOS_KIOSK_HARDENED", "true").lower() in ("true", "1", "yes")
    DEFAULT_ADMIN_PIN = os.environ.get("CAFEOS_ADMIN_PIN", "7777")

from agent.core.state import load_state
from agent.kiosk_windows import disable_keyboard_hook, enable_keyboard_hook
from agent.services.pc_info import collect_pc_info
from agent.services.session_status import read_status

logger = logging.getLogger("cafeos.ui")

try:
    import winsound
except ImportError:
    winsound = None


class GamingCafeClientApp:
    def __init__(self) -> None:
        self.kiosk_hardened = DEFAULT_HARDENED
        self.admin_pin = DEFAULT_ADMIN_PIN
        self.state = "LOCKED"  # LOCKED | ACTIVE | TEMP_LOCKED
        self.local_state = load_state()
        self.host_info = collect_pc_info()

        self.root = tk.Tk()
        self.root.title("CafeOS Gaming Client")
        self.root.configure(bg="#0b0e14")

        # Session tracking variables
        self.current_session: dict[str, Any] | None = None
        self.temp_lock_pin = "1234"
        self.is_pill_mode = False
        self.alerted_10m = False
        self.alerted_5m = False
        self.alerted_1m = False

        # Drag tracking for floating HUD
        self._drag_start_x = 0
        self._drag_start_y = 0

        # StringVars for UI data binding
        self.var_station_name = tk.StringVar(value=self.local_state.get("pc_id", "Station"))
        self.var_player_name = tk.StringVar(value="Guest Player")
        self.var_plan_name = tk.StringVar(value="Standard Gaming")
        self.var_rate_display = tk.StringVar(value="₹100.00 / hr")
        self.var_countdown = tk.StringVar(value="--:--:--")
        self.var_status_badge = tk.StringVar(value="LOCKED")
        self.var_status_detail = tk.StringVar(value="Station ready for booking at counter.")

        # Bind emergency admin unlock (Ctrl + Shift + F12)
        self.root.bind("<Control-Shift-F12>", self._prompt_admin_unlock)

        # Setup initial view
        self._setup_kiosk_view()
        self._apply_locked_mode()

        # Start periodic polling loop (every 500ms)
        self.root.after(500, self.refresh)

    # -------------------------------------------------------------------------
    # Mode Transitions
    # -------------------------------------------------------------------------
    def _apply_locked_mode(self) -> None:
        """Station is idle. Fullscreen kiosk lock screen engaged."""
        self.state = "LOCKED"
        self.root.overrideredirect(False)
        self.root.attributes("-fullscreen", True)
        self.root.attributes("-topmost", True)
        self.root.protocol("WM_DELETE_WINDOW", lambda: None)  # Disable Alt+F4 / window close

        if self.kiosk_hardened and platform.system() == "Windows":
            enable_keyboard_hook()

        self.hud_frame.pack_forget()
        self.pill_frame.pack_forget()
        self.temp_lock_frame.pack_forget()
        self.kiosk_frame.pack(fill="both", expand=True)

        self.var_status_badge.set("STATION AVAILABLE · LOCKED")
        self.var_status_detail.set("Please request this station at the front desk to begin your session.")
        self.var_countdown.set("00:00:00")

    def _apply_active_mode(self, session: dict[str, Any]) -> None:
        """Session is active. Desktop unlocked, Gamer HUD floating overlay engaged."""
        self.state = "ACTIVE"
        # Release low-level keyboard hook so gamer has full control of Windows
        disable_keyboard_hook()

        self.kiosk_frame.pack_forget()
        self.temp_lock_frame.pack_forget()

        # Switch to borderless floating window in top-right corner
        self.root.attributes("-fullscreen", False)
        self.root.attributes("-topmost", True)
        self.root.overrideredirect(True)

        screen_w = self.root.winfo_screenwidth()
        if self.is_pill_mode:
            self.root.geometry(f"280x48+{screen_w - 300}+16")
            self.hud_frame.pack_forget()
            self.pill_frame.pack(fill="both", expand=True)
        else:
            self.root.geometry(f"440x190+{screen_w - 460}+24")
            self.pill_frame.pack_forget()
            self.hud_frame.pack(fill="both", expand=True)

        # Reset alert flags for new session
        self.alerted_10m = False
        self.alerted_5m = False
        self.alerted_1m = False

    def _apply_temp_lock_mode(self) -> None:
        """Gamer stepped away for break. Screen locked with PIN to resume."""
        self.state = "TEMP_LOCKED"
        self.root.overrideredirect(False)
        self.root.attributes("-fullscreen", True)
        self.root.attributes("-topmost", True)

        if self.kiosk_hardened and platform.system() == "Windows":
            enable_keyboard_hook()

        self.kiosk_frame.pack_forget()
        self.hud_frame.pack_forget()
        self.pill_frame.pack_forget()
        self.temp_lock_frame.pack(fill="both", expand=True)
        self.temp_pin_entry.delete(0, tk.END)
        self.temp_pin_entry.focus_set()

    # -------------------------------------------------------------------------
    # View Builders
    # -------------------------------------------------------------------------
    def _setup_kiosk_view(self) -> None:
        # 1. Kiosk Fullscreen Frame
        self.kiosk_frame = tk.Frame(self.root, bg="#0b0e14")

        # Top Bar
        header_bar = tk.Frame(self.kiosk_frame, bg="#0b0e14")
        header_bar.pack(fill="x", padx=48, pady=(40, 20))
        tk.Label(header_bar, text="CAFEOS ESPORTS & GAMING", fg="#38d39f", bg="#0b0e14", font=("Segoe UI", 16, "bold")).pack(side="left")
        pc_badge = f"{self.host_info.get('hostname', 'PC')} · {self.host_info.get('ip_address', '127.0.0.1')}"
        tk.Label(header_bar, text=pc_badge, fg="#5a677d", bg="#0b0e14", font=("Consolas", 12)).pack(side="right")

        # Center Card
        center_card = tk.Frame(self.kiosk_frame, bg="#141824", bd=1, relief="solid", padx=40, pady=36)
        center_card.pack(expand=True)

        tk.Label(center_card, text="🎮 STATION AVAILABLE", fg="#00ffc8", bg="#141824", font=("Segoe UI", 28, "bold")).pack(pady=(0, 10))
        tk.Label(center_card, textvariable=self.var_status_detail, fg="#aab4c2", bg="#141824", font=("Segoe UI", 13), wraplength=520).pack(pady=(0, 24))

        # Rates Table in Kiosk
        rates_frame = tk.Frame(center_card, bg="#1b2132", padx=24, pady=16)
        rates_frame.pack(fill="x", pady=10)
        tk.Label(rates_frame, text="Hourly Gaming Rate", fg="#7a889b", bg="#1b2132", font=("Segoe UI", 11)).pack(anchor="w")
        tk.Label(rates_frame, textvariable=self.var_rate_display, fg="#ffffff", bg="#1b2132", font=("Segoe UI", 20, "bold")).pack(anchor="w")

        # Unlock Instruction
        unlock_info = tk.Frame(center_card, bg="#141824", pady=16)
        unlock_info.pack()
        tk.Label(unlock_info, text="⚡ Once activated by staff, your screen will unlock automatically.", fg="#38d39f", bg="#141824", font=("Segoe UI", 11)).pack()

        # Admin hint
        tk.Label(self.kiosk_frame, text="Staff Maintenance: Press Ctrl+Shift+F12", fg="#2a3344", bg="#0b0e14", font=("Segoe UI", 9)).pack(side="bottom", pady=16)

        # 2. In-Game Gamer HUD Overlay (Floating)
        self.hud_frame = tk.Frame(self.root, bg="#10141f", bd=1, relief="solid")

        # Draggable Header
        drag_bar = tk.Frame(self.hud_frame, bg="#182030", height=28, cursor="fleur")
        drag_bar.pack(fill="x")
        drag_bar.bind("<Button-1>", self._start_drag)
        drag_bar.bind("<B1-Motion>", self._do_drag)

        drag_label = tk.Label(drag_bar, text=":: CafeOS HUD", fg="#38d39f", bg="#182030", font=("Segoe UI", 9, "bold"))
        drag_label.pack(side="left", padx=8)
        drag_label.bind("<Button-1>", self._start_drag)
        drag_label.bind("<B1-Motion>", self._do_drag)

        pill_toggle_btn = tk.Button(drag_bar, text="—", fg="#aab4c2", bg="#182030", bd=0, activebackground="#252e3e",
                                    activeforeground="#ffffff", font=("Segoe UI", 10, "bold"), command=self._toggle_pill_mode)
        pill_toggle_btn.pack(side="right", padx=6)

        # HUD Content
        hud_content = tk.Frame(self.hud_frame, bg="#10141f", padx=14, pady=10)
        hud_content.pack(fill="both", expand=True)

        profile_row = tk.Frame(hud_content, bg="#10141f")
        profile_row.pack(fill="x")
        tk.Label(profile_row, textvariable=self.var_player_name, fg="#00ffc8", bg="#10141f", font=("Segoe UI", 13, "bold")).pack(side="left")
        tk.Label(profile_row, textvariable=self.var_plan_name, fg="#7a889b", bg="#10141f", font=("Segoe UI", 10)).pack(side="right")

        # Timer Display
        timer_row = tk.Frame(hud_content, bg="#10141f")
        timer_row.pack(fill="x", pady=(2, 4))
        self.lbl_countdown = tk.Label(timer_row, textvariable=self.var_countdown, fg="#ffffff", bg="#10141f", font=("Consolas", 32, "bold"))
        self.lbl_countdown.pack(side="left")

        # Action Buttons
        btn_row = tk.Frame(hud_content, bg="#10141f")
        btn_row.pack(fill="x", pady=(6, 0))

        step_away_btn = tk.Button(btn_row, text="🔒 Step Away", fg="#ffffff", bg="#20293d", activebackground="#2d3a54",
                                  bd=0, padx=10, pady=4, font=("Segoe UI", 9, "bold"), command=self._on_click_step_away)
        step_away_btn.pack(side="left")

        # 3. Compact Pill Mode (Minimized HUD for hardcore gaming)
        self.pill_frame = tk.Frame(self.root, bg="#141824", bd=1, relief="solid", cursor="fleur")
        self.pill_frame.bind("<Button-1>", self._start_drag)
        self.pill_frame.bind("<B1-Motion>", self._do_drag)

        pill_user = tk.Label(self.pill_frame, textvariable=self.var_player_name, fg="#00ffc8", bg="#141824", font=("Segoe UI", 10, "bold"))
        pill_user.pack(side="left", padx=(10, 6), pady=6)
        pill_user.bind("<Button-1>", self._start_drag)
        pill_user.bind("<B1-Motion>", self._do_drag)

        pill_time = tk.Label(self.pill_frame, textvariable=self.var_countdown, fg="#ffffff", bg="#141824", font=("Consolas", 14, "bold"))
        pill_time.pack(side="left", padx=4)
        pill_time.bind("<Button-1>", self._start_drag)
        pill_time.bind("<B1-Motion>", self._do_drag)

        expand_btn = tk.Button(self.pill_frame, text="🗖", fg="#7a889b", bg="#141824", bd=0, activebackground="#252e3e",
                               activeforeground="#ffffff", font=("Segoe UI", 9), command=self._toggle_pill_mode)
        expand_btn.pack(side="right", padx=(4, 8))

        # 4. Temp Lock Screen (Step-Away)
        self.temp_lock_frame = tk.Frame(self.root, bg="#080b10")

        temp_box = tk.Frame(self.temp_lock_frame, bg="#131824", bd=1, relief="solid", padx=48, pady=40)
        temp_box.pack(expand=True)

        tk.Label(temp_box, text="🔒 STATION STEPPED AWAY", fg="#ffb84d", bg="#131824", font=("Segoe UI", 26, "bold")).pack(pady=(0, 6))
        tk.Label(temp_box, textvariable=self.var_player_name, fg="#00ffc8", bg="#131824", font=("Segoe UI", 14, "bold")).pack(pady=(0, 16))
        tk.Label(temp_box, text="Your gaming time is still running. Enter your 4-digit PIN to resume playing:",
                 fg="#aab4c2", bg="#131824", font=("Segoe UI", 11)).pack(pady=(0, 16))

        tk.Label(temp_box, textvariable=self.var_countdown, fg="#ffffff", bg="#131824", font=("Consolas", 42, "bold")).pack(pady=(0, 20))

        pin_input_row = tk.Frame(temp_box, bg="#131824")
        pin_input_row.pack()

        self.temp_pin_entry = tk.Entry(pin_input_row, font=("Consolas", 18, "bold"), show="*", justify="center", width=8,
                                       bg="#1c2333", fg="#ffffff", insertbackground="#38d39f", bd=1)
        self.temp_pin_entry.pack(side="left", padx=(0, 10))
        self.temp_pin_entry.bind("<Return>", lambda e: self._verify_temp_pin())

        unlock_btn = tk.Button(pin_input_row, text="Unlock PC", bg="#38d39f", fg="#0b0e14", font=("Segoe UI", 11, "bold"),
                               bd=0, padx=14, pady=3, command=self._verify_temp_pin)
        unlock_btn.pack(side="left")

    # -------------------------------------------------------------------------
    # Drag and Floating HUD controls
    # -------------------------------------------------------------------------
    def _start_drag(self, event: tk.Event) -> None:
        self._drag_start_x = event.x
        self._drag_start_y = event.y

    def _do_drag(self, event: tk.Event) -> None:
        x = self.root.winfo_x() + (event.x - self._drag_start_x)
        y = self.root.winfo_y() + (event.y - self._drag_start_y)
        self.root.geometry(f"+{x}+{y}")

    def _toggle_pill_mode(self) -> None:
        self.is_pill_mode = not self.is_pill_mode
        screen_w = self.root.winfo_screenwidth()
        if self.is_pill_mode:
            self.hud_frame.pack_forget()
            self.pill_frame.pack(fill="both", expand=True)
            self.root.geometry(f"280x48+{screen_w - 300}+16")
        else:
            self.pill_frame.pack_forget()
            self.hud_frame.pack(fill="both", expand=True)
            self.root.geometry(f"440x190+{screen_w - 460}+24")

    # -------------------------------------------------------------------------
    # Step-Away (Temp Lock) Actions
    # -------------------------------------------------------------------------
    def _on_click_step_away(self) -> None:
        # Ask gamer for a quick 4-digit PIN for unlocking
        dialog = tk.Toplevel(self.root)
        dialog.title("Set Step-Away PIN")
        dialog.configure(bg="#141824")
        dialog.geometry("320x160")
        dialog.resizable(False, False)
        dialog.attributes("-topmost", True)

        tk.Label(dialog, text="Set 4-digit PIN to lock your screen:", fg="#f2f5f8", bg="#141824", font=("Segoe UI", 11, "bold")).pack(pady=(16, 8))
        pin_box = tk.Entry(dialog, font=("Consolas", 14), justify="center", width=10, show="*")
        pin_box.pack(pady=4)
        pin_box.insert(0, "1234")
        pin_box.focus_set()

        def confirm() -> None:
            pin = pin_box.get().strip() or "1234"
            self.temp_lock_pin = pin
            dialog.destroy()
            self._apply_temp_lock_mode()

        tk.Button(dialog, text="Lock Station", bg="#ffb84d", fg="#0b0e14", font=("Segoe UI", 10, "bold"),
                  bd=0, padx=12, pady=4, command=confirm).pack(pady=12)

    def _verify_temp_pin(self) -> None:
        entered = self.temp_pin_entry.get().strip()
        if entered == self.temp_lock_pin or entered == self.admin_pin:
            if self.current_session and self.current_session.get("status") == "ACTIVE":
                self._apply_active_mode(self.current_session)
            else:
                self._apply_locked_mode()
        else:
            messagebox.showerror("Invalid PIN", "Incorrect unlock PIN. Please try again or ask counter staff.")
            self.temp_pin_entry.delete(0, tk.END)

    # -------------------------------------------------------------------------
    # Staff / Admin Emergency Bypass
    # -------------------------------------------------------------------------
    def _prompt_admin_unlock(self, event: tk.Event | None = None) -> None:
        dialog = tk.Toplevel(self.root)
        dialog.title("Staff Maintenance Unlock")
        dialog.configure(bg="#141824")
        dialog.geometry("340x170")
        dialog.resizable(False, False)
        dialog.attributes("-topmost", True)

        tk.Label(dialog, text="Enter Staff / Admin PIN:", fg="#f2f5f8", bg="#141824", font=("Segoe UI", 11, "bold")).pack(pady=(18, 8))
        entry = tk.Entry(dialog, font=("Consolas", 14), justify="center", width=10, show="*")
        entry.pack(pady=4)
        entry.focus_set()

        def check_admin_pin() -> None:
            if entry.get().strip() == self.admin_pin:
                disable_keyboard_hook()
                self.root.attributes("-fullscreen", False)
                self.root.iconify()
                dialog.destroy()
                messagebox.showinfo("Staff Unlock", "Kiosk lock minimized for maintenance.")
            else:
                messagebox.showerror("Access Denied", "Incorrect Admin PIN.")
                entry.delete(0, tk.END)

        entry.bind("<Return>", lambda e: check_admin_pin())
        tk.Button(dialog, text="Unlock Maintenance", bg="#38d39f", fg="#0b0e14", font=("Segoe UI", 10, "bold"),
                  bd=0, padx=12, pady=4, command=check_admin_pin).pack(pady=12)

    # -------------------------------------------------------------------------
    # Core Refresh Loop
    # -------------------------------------------------------------------------
    def refresh(self) -> None:
        session = read_status()
        self.current_session = session

        if not session or session.get("status") not in ("ACTIVE", "PAUSED"):
            # No open session
            if self.state != "LOCKED":
                self._apply_locked_mode()
        else:
            # Open session detected
            status = str(session.get("status", "ACTIVE"))
            customer_name = session.get("customerName") or "Guest Player"
            plan_name = session.get("pricingPlanName") or "Gaming Session"
            rate = f"{session.get('currency', '₹')} {session.get('hourlyRate', '0.00')} / hr"
            remaining = self._remaining(session)

            self.var_player_name.set(f"👤 {customer_name}")
            self.var_plan_name.set(f"{plan_name} · {rate}")
            self.var_rate_display.set(rate)
            self.var_countdown.set(self._format_seconds(remaining))

            # Trigger warnings at <10m, <5m, <1m
            self._check_time_warnings(remaining)

            # State transition if coming from LOCKED
            if self.state == "LOCKED":
                self._apply_active_mode(session)
            elif self.state == "ACTIVE":
                # Color code timer low warnings
                if remaining < 300:
                    self.lbl_countdown.configure(fg="#ff5555")
                elif remaining < 600:
                    self.lbl_countdown.configure(fg="#ffb84d")
                else:
                    self.lbl_countdown.configure(fg="#ffffff")

        self.root.after(500, self.refresh)

    def _check_time_warnings(self, remaining: int) -> None:
        if remaining <= 60 and not self.alerted_1m:
            self.alerted_1m = True
            if winsound:
                winsound.MessageBeep(winsound.MB_ICONEXCLAMATION)
        elif remaining <= 300 and not self.alerted_5m:
            self.alerted_5m = True
            if winsound:
                winsound.MessageBeep(winsound.MB_ICONEXCLAMATION)
        elif remaining <= 600 and not self.alerted_10m:
            self.alerted_10m = True
            if winsound:
                winsound.MessageBeep(winsound.MB_ICONASTERISK)

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


def main() -> None:
    app = GamingCafeClientApp()
    app.root.mainloop()


if __name__ == "__main__":
    main()
