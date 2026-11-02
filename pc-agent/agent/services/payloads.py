from pydantic import BaseModel, ConfigDict, Field


def to_camel(name: str) -> str:
    head, *tail = name.split("_")
    return head + "".join(part.title() for part in tail)


class AgentPayload(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class RegistrationPayload(AgentPayload):
    hostname: str = Field(min_length=1, max_length=255)
    os: str = Field(min_length=1, max_length=100)
    ip_address: str = Field(min_length=1, max_length=64)
    agent_version: str = Field(default="0.1.0", max_length=40)


class HeartbeatPayload(AgentPayload):
    agent_status: str = "ONLINE"
    agent_version: str = "0.1.0"
    cpu_percent: float = Field(ge=0, le=100)
    memory_percent: float = Field(ge=0, le=100)
