import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from agent.core.state import load_state, machine_id, save_state
from agent.services.payloads import HeartbeatPayload, RegistrationPayload


class AgentStateTests(unittest.TestCase):
    def test_state_round_trip_is_json_and_machine_id_is_stable(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "agent-state.json"
            with patch("agent.core.state.state_path", return_value=path):
                state = load_state()
                first = machine_id(state)
                state["agent_token"] = "secret"
                save_state(state)
                loaded = load_state()
                self.assertEqual(first, machine_id(loaded))
                self.assertEqual("secret", loaded["agent_token"])
                self.assertEqual(first, json.loads(path.read_text())["machine_id"])


class PayloadTests(unittest.TestCase):
    def test_payloads_use_api_camel_case(self):
        registration = RegistrationPayload(hostname="pc-01", os="Windows 11", ip_address="192.168.1.20")
        heartbeat = HeartbeatPayload(cpu_percent=10, memory_percent=20)
        self.assertIn("ipAddress", registration.model_dump(by_alias=True))
        self.assertIn("cpuPercent", heartbeat.model_dump(by_alias=True))


if __name__ == "__main__":
    unittest.main()
