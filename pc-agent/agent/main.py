import asyncio
import logging

import httpx

from agent.communication.http_client import HttpApiClient
from agent.core.config import AgentConfig
from agent.core.logging_config import configure_logging
from agent.services.heartbeat import HeartbeatService

logger = logging.getLogger("cafeos.agent")


async def run(config: AgentConfig) -> None:
    if not config.pc_id:
        raise ValueError("CAFEOS_PC_ID is required")
    if not config.agent_token:
        raise ValueError("CAFEOS_AGENT_TOKEN is required")

    client = HttpApiClient(config)
    heartbeat = HeartbeatService(client, expected_pc_id=config.pc_id)
    backoff_seconds = 1
    try:
        while True:
            try:
                await heartbeat.register()
                logger.info("Agent registered with CafeOS server")
                while True:
                    await heartbeat.send()
                    backoff_seconds = 1
                    await asyncio.sleep(config.heartbeat_interval_seconds)
            except httpx.HTTPStatusError as error:
                if error.response.status_code in (401, 403):
                    logger.error("Agent credentials rejected; issue a replacement token")
                    return
                logger.warning("CafeOS returned HTTP %s; retrying", error.response.status_code)
            except httpx.RequestError as error:
                logger.warning("CafeOS is unreachable (%s); retrying", type(error).__name__)
            await asyncio.sleep(backoff_seconds)
            backoff_seconds = min(backoff_seconds * 2, 60)
    finally:
        await client.close()


def main() -> None:
    config = AgentConfig()
    configure_logging(config.log_level)
    try:
        asyncio.run(run(config))
    except KeyboardInterrupt:
        logger.info("Agent stopped")


if __name__ == "__main__":
    main()
