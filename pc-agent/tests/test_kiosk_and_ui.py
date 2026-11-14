import unittest
from unittest.mock import patch
import datetime as dt

from agent.ui import GamingCafeClientApp
import agent.kiosk_windows as kw


class KioskHookTests(unittest.TestCase):
    def test_hook_lifecycle(self):
        # Verify disable doesn't error when not active
        self.assertFalse(kw.disable_keyboard_hook())
        self.assertFalse(kw.is_hook_active())


class ClientUiLogicTests(unittest.TestCase):
    def test_remaining_calculation(self):
        # 3600 seconds remaining when expiry is in 1 hour
        future = (dt.datetime.now(dt.timezone.utc) + dt.timedelta(seconds=3600)).isoformat()
        session = {
            "status": "ACTIVE",
            "expiresAt": future,
            "remainingSeconds": 3600,
        }
        remaining = GamingCafeClientApp._remaining(session)
        self.assertTrue(3590 <= remaining <= 3600)

    def test_format_seconds(self):
        self.assertEqual("01:00:00", GamingCafeClientApp._format_seconds(3600))
        self.assertEqual("00:05:30", GamingCafeClientApp._format_seconds(330))
        self.assertEqual("00:00:00", GamingCafeClientApp._format_seconds(0))


if __name__ == "__main__":
    unittest.main()
