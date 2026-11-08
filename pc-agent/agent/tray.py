"""Optional logged-in-user tray entry point.

The tray is deliberately a status surface only. Session control and billing
remain in CafeOS and are never implemented as local authority here.
"""
import asyncio
import threading

from agent.core.config import AgentConfig
from agent.core.logging_config import configure_logging
from agent.main import run


def main() -> None:
    try:
        import pystray
        from PIL import Image, ImageDraw
    except ImportError as error:
        raise RuntimeError("Install pystray and Pillow to run the tray client") from error

    config = AgentConfig()
    configure_logging(config.log_level, config.log_file)
    stop_event = asyncio.Event()
    loop_holder = {"loop": None}

    def start_agent() -> None:
        async def serve():
            loop_holder["loop"] = asyncio.get_running_loop()
            await run(config, stop_event)
        asyncio.run(serve())

    threading.Thread(target=start_agent, name="CafeOS-Agent", daemon=True).start()
    image = Image.new("RGB", (64, 64), "#151922")
    ImageDraw.Draw(image).ellipse((12, 12, 52, 52), fill="#38d39f")
    def quit_agent(icon, item):
        if loop_holder["loop"]:
            loop_holder["loop"].call_soon_threadsafe(stop_event.set)
        icon.stop()
    menu = pystray.Menu(pystray.MenuItem("Quit", quit_agent))
    pystray.Icon("CafeOS", image, "CafeOS Agent", menu).run()


if __name__ == "__main__":
    main()
