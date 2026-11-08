import json
import os
import platform
import tempfile
from pathlib import Path
from typing import Any


def status_path() -> Path:
    if platform.system() == "Windows":
        root = Path(os.environ.get("PROGRAMDATA", Path.home())) / "CafeOS"
    else:
        root = Path.home() / ".cafeos"
    try:
        root.mkdir(parents=True, exist_ok=True)
    except OSError:
        root = Path.home() / ".cafeos"
        root.mkdir(parents=True, exist_ok=True)
    return root / "session-status.json"


def write_status(status: dict[str, Any] | None) -> None:
    path = status_path()
    fd, temporary_name = tempfile.mkstemp(prefix="session-status-", suffix=".tmp", dir=path.parent)
    temporary = Path(temporary_name)
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as handle:
            json.dump(status, handle, separators=(",", ":"))
            handle.flush()
            os.fsync(handle.fileno())
        temporary.replace(path)
    finally:
        temporary.unlink(missing_ok=True)


def read_status() -> dict[str, Any] | None:
    try:
        value = json.loads(status_path().read_text(encoding="utf-8"))
        return value if isinstance(value, dict) else None
    except (FileNotFoundError, json.JSONDecodeError, OSError):
        return None
