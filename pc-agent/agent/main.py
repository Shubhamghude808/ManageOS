import asyncio
import logging

import httpx

from agent.communication.enrollment_client import EnrollmentClient
from agent.communication.http_client import HttpApiClient
from agent.core.config import AgentConfig
from agent.core.credentials import clear_token, load_token, save_token
from agent.core.logging_config import configure_logging
from agent.core.state import load_state, machine_id, save_state
from agent.services.pc_info import collect_pc_info
from agent.services.heartbeat import HeartbeatService

logger = logging.getLogger("cafeos.agent")


async def run(config: AgentConfig, stop_event: asyncio.Event | None = None) -> None:
    stop_event = stop_event or asyncio.Event()
    state = load_state()
    current_machine_id = machine_id(state)
    pc_id = config.pc_id or state.get("pc_id")
    backoff_seconds = 1
    while True:
        if stop_event.is_set():
            return
        token = config.agent_token or load_token()
        if not token and config.allow_plaintext_token_fallback:
            token = state.get("agent_token")
        try:
            if not token:
                enrollment = EnrollmentClient(config.server_url, config.request_timeout_seconds, config.verify_tls)
                try:
                    host_info = collect_pc_info()
                    payload = {
                        "machineId": current_machine_id,
                        "hostname": host_info["hostname"],
                        "os": host_info["os"],
                        "ipAddress": host_info["ip_address"],
                        "agentVersion": config.agent_version,
                    }
                    if config.branch_id:
                        payload["branchId"] = config.branch_id
                    result = await enrollment.enroll(payload)
                    token = result["token"]
                    pc_id = result["pcId"]
                    state.update({"pc_id": pc_id})
                    save_token(token, config.allow_plaintext_token_fallback, state)
                    save_state(state)
                    logger.info("Agent enrolled automatically as PC %s", pc_id)
                finally:
                    await enrollment.close()

            config.agent_token = token
            client = HttpApiClient(config)
            heartbeat = HeartbeatService(client, expected_pc_id=pc_id, agent_version=config.agent_version)
            try:
                await heartbeat.register()
                logger.info("Agent registered with CafeOS server")
                while not stop_event.is_set():
                    await heartbeat.send()
                    backoff_seconds = 1
                    try:
                        await asyncio.wait_for(stop_event.wait(), timeout=config.heartbeat_interval_seconds)
                    except asyncio.TimeoutError:
                        pass
            finally:
                await client.close()
        except httpx.HTTPStatusError as error:
            if error.response.status_code in (401, 403):
                logger.warning("Agent credential rejected; clearing it and retrying enrollment")
                clear_token()
                state.pop("agent_token", None)
                save_state(state)
                config.agent_token = None
            else:
                logger.warning("CafeOS returned HTTP %s: %s; retrying",
                               error.response.status_code, error.response.text[:500])
        except (httpx.RequestError, RuntimeError, ValueError) as error:
            logger.warning("CafeOS enrollment/connection failed (%s); retrying", type(error).__name__)
        try:
            await asyncio.wait_for(stop_event.wait(), timeout=backoff_seconds)
        except asyncio.TimeoutError:
            pass
        backoff_seconds = min(backoff_seconds * 2, 60)


def main() -> None:
    config = AgentConfig()
    configure_logging(config.log_level, config.log_file)
    try:
        asyncio.run(run(config))
    except KeyboardInterrupt:
        logger.info("Agent stopped")


if __name__ == "__main__":
    main()
