from typing import Any

import httpx

from agent.core.config import AgentConfig


class HttpApiClient:
    def __init__(self, config: AgentConfig) -> None:
        if not config.agent_token:
            raise ValueError("An enrolled agent token is required")
        self._client = httpx.AsyncClient(
            base_url=config.server_url.rstrip("/"),
            headers={"Authorization": f"Bearer {config.agent_token}"},
            timeout=httpx.Timeout(5.0),
        )

    async def post(self, path: str, payload: dict[str, Any]) -> dict[str, Any]:
        response = await self._client.post(path, json=payload)
        response.raise_for_status()
        body = response.json()
        return body["data"]

    async def close(self) -> None:
        await self._client.aclose()
