"""
Checks that every block and item the mod registers has its files:
models, textures, blockstates, names in en_us.json and block loot tables.
GitHub runs this before building. Run it yourself with: python3 tools/check_assets.py
"""
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java"
RES = ROOT / "src/main/resources"
ASSETS = RES / "assets/glt"
DATA = RES / "data/glt"

errors = []


def err(msg):
    errors.append(msg)


def java_sources():
    return "\n".join(p.read_text() for p in JAVA.rglob("*.java"))


def registered(src, register_var):
    # e.g. ITEMS.register("name", ...), ITEMS.registerSimpleItem("name", ...), BLOCKS.register("name", ...)
    return set(re.findall(register_var + r'\.register\w*\(\s*"([a-z0-9_/]+)"', src))


def texture_exists(ref):
    if ":" in ref:
        ns, path = ref.split(":", 1)
    else:
        ns, path = "minecraft", ref
    if ns != "glt":
        return True
    return (ASSETS / "textures" / (path + ".png")).exists()


def model_exists(ref):
    ns, path = ref.split(":", 1) if ":" in ref else ("minecraft", ref)
    if ns != "glt":
        return True
    return (ASSETS / "models" / (path + ".json")).exists()


def main():
    src = java_sources()
    items = registered(src, "ITEMS") | set(re.findall(r'blockItem\(\s*"([a-z0-9_/]+)"', src))
    blocks = registered(src, "BLOCKS")
    lang = json.loads((ASSETS / "lang/en_us.json").read_text())

    for block in sorted(blocks):
        if not (ASSETS / f"blockstates/{block}.json").exists():
            err(f"block {block}: missing blockstates/{block}.json")
        if f"block.glt.{block}" not in lang:
            err(f"block {block}: missing name block.glt.{block} in en_us.json")
        if not (DATA / f"loot_table/blocks/{block}.json").exists():
            err(f"block {block}: missing loot_table/blocks/{block}.json (it would drop nothing)")

    for item in sorted(items):
        if not (ASSETS / f"models/item/{item}.json").exists():
            err(f"item {item}: missing models/item/{item}.json")
        if item not in blocks and f"item.glt.{item}" not in lang:
            err(f"item {item}: missing name item.glt.{item} in en_us.json")

    for state_file in (ASSETS / "blockstates").glob("*.json"):
        text = state_file.read_text()
        for ref in re.findall(r'"model"\s*:\s*"([^"]+)"', text):
            if not model_exists(ref):
                err(f"{state_file.name}: model {ref} not found")

    for model_file in (ASSETS / "models").rglob("*.json"):
        model = json.loads(model_file.read_text())
        parent = model.get("parent")
        if parent and not model_exists(parent) and not parent.startswith("builtin/"):
            err(f"{model_file.relative_to(ASSETS)}: parent {parent} not found")
        for key, ref in model.get("textures", {}).items():
            if not ref.startswith("#") and not texture_exists(ref):
                err(f"{model_file.relative_to(ASSETS)}: texture {ref} not found")

    # Every translation key used in Java exists.
    for key in sorted(set(re.findall(r'(?:translatable|KeyMapping)\(\s*"([a-z0-9_.]+)"', src))):
        if "glt" not in key or key.endswith("."):
            continue  # a vanilla key, or the rest is added in code like "role.glt." + name
        if key not in lang:
            err(f"translation {key} is used in the code but missing from en_us.json")

    # All JSON files parse.
    for json_file in RES.rglob("*.json"):
        try:
            json.loads(json_file.read_text())
        except json.JSONDecodeError as e:
            err(f"{json_file.relative_to(RES)}: invalid JSON ({e})")

    if errors:
        print("Asset check found problems:")
        for e in errors:
            print("  - " + e)
        sys.exit(1)
    print(f"Asset check OK: {len(blocks)} blocks, {len(items)} items.")


if __name__ == "__main__":
    main()
