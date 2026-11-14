"""Windows low-level keyboard hook for hardened kiosk enforcement in gaming cafes.

Uses standard library ctypes to intercept and block system navigation keys
(Windows key, Alt+Tab, Alt+Esc, Ctrl+Esc, Alt+F4) while the PC is in locked
kiosk mode. Automatically bypassed during active gaming sessions.
"""
import atexit
import logging
import sys

logger = logging.getLogger("cafeos.kiosk")

_hook_id = None
_hook_proc = None

if sys.platform == "win32":
    import ctypes
    from ctypes import wintypes

    user32 = ctypes.windll.user32
    kernel32 = ctypes.windll.kernel32

    WH_KEYBOARD_LL = 13
    WM_KEYDOWN = 0x0100
    WM_KEYUP = 0x0101
    WM_SYSKEYDOWN = 0x0104
    WM_SYSKEYUP = 0x0105

    VK_TAB = 0x09
    VK_ESCAPE = 0x1B
    VK_SPACE = 0x20
    VK_F4 = 0x73
    VK_LWIN = 0x5B
    VK_RWIN = 0x5C
    VK_F12 = 0x7B

    class KBDLLHOOKSTRUCT(ctypes.Structure):
        _fields_ = [
            ("vkCode", wintypes.DWORD),
            ("scanCode", wintypes.DWORD),
            ("flags", wintypes.DWORD),
            ("time", wintypes.DWORD),
            ("dwExtraInfo", ctypes.c_ulong),
        ]

    HOOKPROC = ctypes.WINFUNCTYPE(ctypes.c_long, ctypes.c_int, wintypes.WPARAM, wintypes.LPARAM)

    def _low_level_keyboard_proc(nCode: int, wParam: int, lParam: int) -> int:
        if nCode < 0:
            return user32.CallNextHookEx(_hook_id, nCode, wParam, lParam)

        kb = KBDLLHOOKSTRUCT.from_address(lParam)
        vk = kb.vkCode
        alt_down = bool(kb.flags & 0x20)

        # Allow emergency escape shortcut Ctrl+Shift+F12 to reach Tkinter handlers
        if vk == VK_F12:
            return user32.CallNextHookEx(_hook_id, nCode, wParam, lParam)

        # 1. Suppress Windows Keys (Start menu)
        if vk in (VK_LWIN, VK_RWIN):
            return 1

        # 2. Suppress Alt+Tab, Alt+Esc, Alt+F4, Alt+Space
        if alt_down and vk in (VK_TAB, VK_ESCAPE, VK_F4, VK_SPACE):
            return 1

        # 3. Suppress Ctrl+Esc (Start menu shortcut)
        if vk == VK_ESCAPE:
            ctrl_down = bool(user32.GetAsyncKeyState(0x11) & 0x8000)
            if ctrl_down:
                return 1

        return user32.CallNextHookEx(_hook_id, nCode, wParam, lParam)


def enable_keyboard_hook() -> bool:
    """Install the low-level keyboard hook to block Windows navigation keys."""
    global _hook_id, _hook_proc
    if sys.platform != "win32":
        return False
    if _hook_id is not None:
        return True

    try:
        _hook_proc = HOOKPROC(_low_level_keyboard_proc)
        module_handle = kernel32.GetModuleHandleW(None)
        _hook_id = user32.SetWindowsHookExW(WH_KEYBOARD_LL, _hook_proc, module_handle, 0)
        if not _hook_id:
            logger.warning("Failed to install low-level keyboard hook (error code: %s)", kernel32.GetLastError())
            _hook_proc = None
            return False
        logger.info("Hardened kiosk keyboard hook installed successfully")
        return True
    except Exception as exc:
        logger.warning("Error installing keyboard hook: %s", exc)
        return False


def disable_keyboard_hook() -> bool:
    """Remove the low-level keyboard hook so the gamer has full system control."""
    global _hook_id, _hook_proc
    if sys.platform != "win32" or _hook_id is None:
        return False

    try:
        user32.UnhookWindowsHookEx(_hook_id)
        logger.info("Hardened kiosk keyboard hook removed")
        return True
    except Exception as exc:
        logger.warning("Error removing keyboard hook: %s", exc)
        return False
    finally:
        _hook_id = None
        _hook_proc = None


def is_hook_active() -> bool:
    return _hook_id is not None


# Ensure hook is released on exit to prevent leaving keyboard blocked
atexit.register(disable_keyboard_hook)
