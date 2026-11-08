# Build with: pyinstaller --clean CafeOSAgent.spec
from PyInstaller.utils.hooks import collect_submodules

hiddenimports = collect_submodules("agent")
a = Analysis(["agent/main.py"], hiddenimports=hiddenimports, pathex=["."], excludes=[])
pyz = PYZ(a.pure)
exe = EXE(pyz, a.scripts, a.binaries, a.datas, [], name="CafeOSAgent", console=True)
