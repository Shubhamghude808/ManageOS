from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class AgentConfig(BaseSettings):
    model_config = SettingsConfigDict(env_prefix="CAFEOS_", env_file=".env", extra="ignore")

    server_url: str = "http://localhost:8080"
    pc_id: str | None = None
    branch_id: str | None = None
    agent_token: str | None = Field(default=None, repr=False)
    log_level: str = "INFO"
    heartbeat_interval_seconds: int = Field(default=15, ge=5, le=300)
    session_poll_interval_seconds: int = Field(default=5, ge=1, le=60)
    request_timeout_seconds: float = Field(default=10.0, gt=1, le=120)
    verify_tls: bool = True
    agent_version: str = "0.2.0"
    log_file: str | None = None
    allow_plaintext_token_fallback: bool = False
