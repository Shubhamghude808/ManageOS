import json
import os
import platform
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
    temporary = path.with_suffix(".tmp")
    temporary.write_text(json.dumps(state), encoding="utf-8")
    temporary.replace(path)


def machine_id(state: dict[str, Any]) -> str:
    value = state.get("machine_id")
    if value:
        return str(value)
    value = str(uuid.uuid4())
    state["machine_id"] = value
    save_state(state)
    return value
