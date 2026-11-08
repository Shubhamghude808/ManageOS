"""Credential storage with Windows Credential Manager support when installed."""
import logging
from pathlib import Path

from agent.core.state import state_path

logger = logging.getLogger("cafeos.agent.credentials")
SERVICE_NAME = "CafeOSAgent"
TOKEN_KEY = "agent-token"


def _keyring():
    try:
        import keyring
        return keyring
    except ImportError:
        return None


def load_token() -> str | None:
    keyring = _keyring()
    if keyring:
        try:
            return keyring.get_password(SERVICE_NAME, TOKEN_KEY)
        except Exception:
            logger.warning("Windows Credential Manager unavailable; trying DPAPI storage")
    try:
        import win32crypt
        path = _dpapi_path()
        if path.exists():
            plaintext = win32crypt.CryptUnprotectData(path.read_bytes(), None, None, None, 0)[1]
            return plaintext.decode("utf-8")
    except Exception:
        logger.exception("Could not read DPAPI token storage")
    return None


def save_token(token: str, allow_plaintext_fallback: bool = False, state: dict | None = None) -> None:
    keyring = _keyring()
    if not keyring:
        try:
            _save_dpapi(token)
            return
        except Exception:
            if allow_plaintext_fallback and state is not None:
                state["agent_token"] = token
                return
            raise RuntimeError("No usable protected token store is available; enable CAFEOS_ALLOW_PLAINTEXT_TOKEN_FALLBACK for local development")
    try:
        keyring.set_password(SERVICE_NAME, TOKEN_KEY, token)
    except Exception:
        logger.warning("Windows Credential Manager unavailable; using DPAPI storage")
        try:
            _save_dpapi(token)
        except Exception:
            if allow_plaintext_fallback and state is not None:
                state["agent_token"] = token
                return
            raise RuntimeError("Windows Credential Manager and DPAPI are unavailable; enable CAFEOS_ALLOW_PLAINTEXT_TOKEN_FALLBACK for local development")


def clear_token() -> None:
    keyring = _keyring()
    if keyring:
        try:
            keyring.delete_password(SERVICE_NAME, TOKEN_KEY)
        except Exception:
            pass
    _dpapi_path().unlink(missing_ok=True)


def _dpapi_path() -> Path:
    return state_path().with_name("agent-token.dpapi")


def _save_dpapi(token: str) -> None:
    try:
        import win32crypt
    except ImportError as error:
        raise RuntimeError("Install pywin32 for protected Windows token storage") from error
    protected = win32crypt.CryptProtectData(token.encode("utf-8"), "CafeOS agent token", None, None, None, 0)[1]
    path = _dpapi_path()
    temporary = path.with_suffix(".tmp")
    temporary.write_bytes(protected)
    temporary.replace(path)
