"""Generate missing inventory entry points and modern names from original bundled data.

Existing geometry and textures are never overwritten. Type routing is handled by WeaponModels.
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
NS = 'rafradek_tf2_weapons'
ASSETS = ROOT / 'src/main/resources/assets' / NS


def resolve_catalog():
    raw = json.loads((ASSETS / 'weapons/weapons.json').read_text(encoding='utf-8'))
    resolved = {}
    def resolve(name, visiting=()):
        if name in resolved:
            return resolved[name]
        if name in visiting:
            raise ValueError(f'Inheritance cycle: {visiting}, {name}')
        source = raw[name]
        result = dict(resolve(source['Based on'], visiting + (name,))) if 'Based on' in source else {}
        attributes = dict(result.get('Attributes', {}))
        attributes.update(source.get('Attributes', {}))
        result.update(source)
        if attributes:
            result['Attributes'] = attributes
        resolved[name] = result
        return result
    for name in sorted(raw):
        resolve(name)
    return resolved


def generate():
    catalog = resolve_catalog()
    carriers = sorted({data['Class'] for data in catalog.values()} - catalog.keys())
    models = ASSETS / 'models/item'
    for name in sorted(set(catalog) | set(carriers)):
        path = models / (name + '.json')
        if path.exists():
            continue
        render = catalog.get(name, {}).get('Render', NS + ':weapon_unavailable')
        namespace, model = render.split(':')
        path.write_text(json.dumps({'parent': f'{namespace}:item/{model}'}, indent=2) + '\n', encoding='utf-8')
    language_path = ASSETS / 'lang/en_us.json'
    language = json.loads(language_path.read_text(encoding='utf-8'))
    original = {}
    for line in (ASSETS / 'lang/en_US.lang').read_text(encoding='utf-8-sig').splitlines():
        if '=' in line and not line.startswith('#'):
            key, value = line.split('=', 1)
            original[key] = value
    for name, data in catalog.items():
        language[f'item.{NS}.{name}'] = original.get('weapon.' + name, data.get('Name', name))
    for key, value in {
        'invalid_type': 'Missing or unknown weapon Type',
        'reserve': 'Reserve: %s/%s',
        'partial': 'Combat available; original mechanics partially ported',
        'pending': 'Weapon mechanics pending migration',
        'provisional_model': 'Provisional model: original representation unavailable',
        'ammo_supply': 'Use with the weapon in the other hand to replenish its ammo',
    }.items():
        language[f'item.{NS}.weapon.{key}'] = value
    language_path.write_text(json.dumps(language, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
    # Keep numeric legacy NBT Attributes IDs without importing the old API layer.
    attribute_source = ROOT / 'src/main/java/rafradek/tf2weapons/common/TF2Attribute.java'
    entries = re.findall(r'new TF2Attribute\((\d+),\s*"([^"]+)",\s*"([^"]+)",\s*Type\.(\w+)', attribute_source.read_text(encoding='utf-8'))
    attributes = {identifier: {'name': name, 'effect': effect, 'type': kind} for identifier, name, effect, kind in entries}
    metadata = ASSETS / 'weapon_metadata'
    metadata.mkdir(exist_ok=True)
    (metadata / 'attribute_ids.json').write_text(json.dumps(attributes, indent=2) + '\n', encoding='utf-8')
    print(f'{len(catalog)} catalog IDs, {len(carriers)} legacy carriers; missing inventory entry points generated.')


if __name__ == '__main__':
    generate()
