"""Windows Service entry point.

PyWin32 is intentionally optional so the foreground agent remains runnable on
non-Windows development machines.
"""
import asyncio
import logging

from agent.core.config import AgentConfig
from agent.core.logging_config import configure_logging
from agent.main import run

logger = logging.getLogger("cafeos.agent.service")

try:
    import servicemanager
    import win32event
    import win32service
    import win32serviceutil
except ImportError:  # pragma: no cover - only available on Windows installs
    servicemanager = win32event = win32service = win32serviceutil = None


if win32serviceutil:
    class CafeOsAgentService(win32serviceutil.ServiceFramework):
        _svc_name_ = "CafeOSAgent"
        _svc_display_name_ = "CafeOS Gaming PC Agent"
        _svc_description_ = "Reports CafeOS gaming PC health and connectivity."

        def __init__(self, args):
            super().__init__(args)
            self.stop_handle = win32event.CreateEvent(None, 0, 0, None)
            self.stop_event = asyncio.Event()
            self.loop = None

        def SvcStop(self):
            self.ReportServiceStatus(win32service.SERVICE_STOP_PENDING)
            if self.loop:
                self.loop.call_soon_threadsafe(self.stop_event.set)
            win32event.SetEvent(self.stop_handle)

        def SvcDoRun(self):
            config = AgentConfig()
            configure_logging(config.log_level, config.log_file)
            async def serve():
                self.loop = asyncio.get_running_loop()
                await run(config, self.stop_event)
            asyncio.run(serve())


def main() -> None:
    if not win32serviceutil:
        raise RuntimeError("Install pywin32 to run the Windows Service entry point")
    win32serviceutil.HandleCommandLine(CafeOsAgentService)


if __name__ == "__main__":
    main()
