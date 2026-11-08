import json
import os
import platform
import tempfile
import uuid
from pathlib import Path
from typing import Any


def state_path() -> Path:
    if platform.system() == "Windows":
        root = Path(os.environ.get("LOCALAPPDATA", Path.home())) / "CafeOS"
    else:
        root = Path.home() / ".cafeos"
    root.mkdir(parents=True, exist_ok=True)
    return root / "agent-state.json"


def load_state() -> dict[str, Any]:
    path = state_path()
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (FileNotFoundError, json.JSONDecodeError):
        return {}


def save_state(state: dict[str, Any]) -> None:
    path = state_path()
    fd, temporary_name = tempfile.mkstemp(prefix="agent-state-", suffix=".tmp", dir=path.parent)
    temporary = Path(temporary_name)
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as handle:
            json.dump(state, handle, separators=(",", ":"))
            handle.flush()
            os.fsync(handle.fileno())
        if platform.system() == "Windows":
            os.chmod(temporary, 0o600)
        temporary.replace(path)
    finally:
        temporary.unlink(missing_ok=True)


def machine_id(state: dict[str, Any]) -> str:
    value = state.get("machine_id")
    if value:
        return str(value)
    value = str(uuid.uuid4())
    state["machine_id"] = value
    save_state(state)
    return value
