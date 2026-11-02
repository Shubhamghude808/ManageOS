import platform
import socket


def collect_pc_info() -> dict[str, str]:
    """Return non-secret host fields suitable for agent registration."""
    hostname = socket.gethostname()
    try:
        ip_address = socket.gethostbyname(hostname)
    except OSError:
        ip_address = "127.0.0.1"
    return {
        "hostname": hostname,
        "os": f"{platform.system()} {platform.release()}",
        "ip_address": ip_address,
    }
