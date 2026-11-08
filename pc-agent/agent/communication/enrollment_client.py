from typing import Any

import httpx


class EnrollmentClient:
    def __init__(self, server_url: str, timeout_seconds: float = 10.0, verify_tls: bool = True) -> None:
        self._client = httpx.AsyncClient(
            base_url=server_url.rstrip("/"),
            timeout=httpx.Timeout(timeout_seconds),
            verify=verify_tls,
        )

    async def enroll(self, payload: dict[str, Any]) -> dict[str, Any]:
        response = await self._client.post("/api/agent/enroll", json=payload)
        response.raise_for_status()
        return response.json()["data"]

    async def close(self) -> None:
        await self._client.aclose()
