import os
from pathlib import Path
import subprocess
import sys


ROOT = Path(__file__).resolve().parents[1]


def load_local_env(path: Path) -> dict[str, str]:
    if not path.is_file():
        return {}

    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue

        name, value = line.split("=", 1)
        if name.strip() == "DB_PASSWORD":
            value = value.strip()
            if len(value) >= 2 and value[0] == value[-1] and value[0] in "\"'":
                value = value[1:-1]
            return {"DB_PASSWORD": value}

    return {}


child_env = os.environ.copy()
for name, value in load_local_env(ROOT / ".env").items():
    child_env.setdefault(name, value)

if os.name == "nt":
    wrapper = ROOT / "spectrum-app" / "mvnw.cmd"
    command = [str(wrapper), "-f", "spectrum-app/pom.xml", *sys.argv[1:]]
    # Windows batch files must be launched through the command shell.
    result = subprocess.run(
        subprocess.list2cmdline(command), cwd=ROOT, shell=True, env=child_env
    )
else:
    wrapper = ROOT / "spectrum-app" / "mvnw"
    command = [str(wrapper), "-f", "spectrum-app/pom.xml", *sys.argv[1:]]
    result = subprocess.run(command, cwd=ROOT, env=child_env)

sys.exit(result.returncode)
