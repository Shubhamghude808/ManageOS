from typing import Any, Protocol


class ApiClient(Protocol):
    """Authenticated device transport to the local CafeOS server."""

    async def post(self, path: str, payload: dict[str, Any]) -> dict[str, Any]: ...
    async def close(self) -> None: ...
