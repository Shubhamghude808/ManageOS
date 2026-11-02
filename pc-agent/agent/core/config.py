from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class AgentConfig(BaseSettings):
    model_config = SettingsConfigDict(env_prefix="CAFEOS_", env_file=".env", extra="ignore")

    server_url: str = "http://localhost:8080"
    pc_id: str | None = None
    agent_token: str | None = Field(default=None, repr=False)
    log_level: str = "INFO"
    heartbeat_interval_seconds: int = Field(default=15, ge=5, le=300)
