import os
from pathlib import Path
import subprocess
import sys


ROOT = Path(__file__).resolve().parents[1]
if os.name == "nt":
    wrapper = ROOT / "spectrum-app" / "mvnw.cmd"
    command = [str(wrapper), "-f", "spectrum-app/pom.xml", *sys.argv[1:]]
    # Windows batch files must be launched through the command shell.
    result = subprocess.run(subprocess.list2cmdline(command), cwd=ROOT, shell=True)
else:
    wrapper = ROOT / "spectrum-app" / "mvnw"
    command = [str(wrapper), "-f", "spectrum-app/pom.xml", *sys.argv[1:]]
    result = subprocess.run(command, cwd=ROOT)

sys.exit(result.returncode)
