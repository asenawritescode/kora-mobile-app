#!/usr/bin/env python3
"""Verify generated Android Engine contracts against the pinned producer."""

from __future__ import annotations

import argparse
import hashlib
import re
import subprocess
from pathlib import Path


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--engine-root", type=Path, required=True)
    args = parser.parse_args()
    engine = args.engine_root.resolve()
    manifest = Path("app/src/main/java/com/kora/mobile/generated/EngineContractManifest.kt").read_text()
    revision = re.search(r'const val sourceRevision = "([a-f0-9]{40})"', manifest)
    schema_digest = re.search(r'const val schemaSHA256 = "([a-f0-9]{64})"', manifest)
    if not revision or not schema_digest:
        raise SystemExit("Android Engine contract manifest lacks its source revision or schema digest")
    actual_revision = subprocess.run(
        ["git", "rev-parse", "HEAD"], cwd=engine, check=True, capture_output=True, text=True
    ).stdout.strip()
    if actual_revision != revision.group(1):
        raise SystemExit(f"Engine source revision drift: {actual_revision} != {revision.group(1)}")
    android_schema = Path("app/src/main/assets/contracts/engine-v2.json")
    engine_schema = engine / "contract/schema/engine-v2.json"
    local_digest, engine_digest = sha256(android_schema), sha256(engine_schema)
    if local_digest != schema_digest.group(1) or local_digest != engine_digest:
        raise SystemExit(f"Android Engine contract schema drift: {local_digest} != {engine_digest}")
    print(f"Android Engine v2 contract verified at {actual_revision} ({local_digest})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
