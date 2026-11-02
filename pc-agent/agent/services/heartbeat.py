from typing import Any
import psutil

from agent.communication.api_client import ApiClient
from agent.services.payloads import HeartbeatPayload, RegistrationPayload
from agent.services.pc_info import collect_pc_info


class HeartbeatService:
    def __init__(self, client: ApiClient, expected_pc_id: str | None = None) -> None:
        self._client = client
        self._expected_pc_id = expected_pc_id

    async def register(self) -> dict[str, Any]:
        payload = RegistrationPayload(**collect_pc_info())
        result = await self._client.post("/api/agent/register", payload.model_dump(by_alias=True))
        if self._expected_pc_id and result.get("pcId") != self._expected_pc_id:
            raise RuntimeError("Agent token is bound to a different PC than CAFEOS_PC_ID")
        return result

    async def send(self) -> dict[str, Any]:
        """Submit a heartbeat through the configured transport."""
        validated = HeartbeatPayload(
            cpu_percent=psutil.cpu_percent(interval=0.1),
            memory_percent=psutil.virtual_memory().percent,
        )
        return await self._client.post("/api/agent/heartbeat", validated.model_dump(by_alias=True))
