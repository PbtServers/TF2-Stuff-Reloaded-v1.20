"""Inventory bundled weapon data and resource references; no game execution."""
import json
import argparse
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets'
NAMESPACE = 'rafradek_tf2_weapons'


def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def resource(identifier, folder, suffix):
    namespace, _, name = identifier.partition(':')
    if not name:
        namespace, name = 'minecraft', namespace
    return ASSETS / namespace / folder / (name + suffix)


def audit():
    catalogs = {p.stem: read(p) for p in sorted((ASSETS / NAMESPACE / 'weapons').glob('*.json'))}
    weapons = catalogs['weapons']
    missing_models = []
    for name, data in weapons.items():
        render = data.get('Render')
        if render:
            namespace, model = render.split(':', 1)
            if not resource(f'{namespace}:item/{model}', 'models', '.json').is_file():
                missing_models.append({'weapon': name, 'render': render})
    invalid_json = []
    missing_references = []
    for path in sorted(ASSETS.rglob('*.json')):
        try:
            data = read(path)
        except (ValueError, UnicodeError) as error:
            invalid_json.append({'file': str(path.relative_to(ROOT)), 'error': str(error)})
            continue
        if 'models' not in path.parts or not isinstance(data, dict):
            continue
        references = [(data.get('parent'), 'models', '.json')]
        references += [(v, 'textures', '.png') for v in data.get('textures', {}).values()]
        for identifier, folder, suffix in references:
            if not isinstance(identifier, str) or ':' not in identifier or identifier.startswith(('#', 'minecraft:')):
                continue
            target = resource(identifier, folder, suffix)
            if not target.is_file():
                missing_references.append({'file': str(path.relative_to(ROOT)), 'reference': identifier})
    return {
        'catalog_entries': {name: len(data) for name, data in catalogs.items()},
        'weapon_classes': dict(sorted(Counter(v.get('Class', '<inherited>') for v in weapons.values()).items())),
        'missing_direct_weapon_models': missing_models,
        'invalid_asset_json': invalid_json,
        'missing_local_model_references': missing_references,
        'scope': 'Static inventory only; does not prove runtime loading, inherited model resolution or gameplay.',
    }


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path)
    parser.add_argument('--check', action='store_true', help='Fail on invalid JSON or missing local model references')
    args = parser.parse_args()
    report = audit()
    output = json.dumps(report, indent=2, ensure_ascii=False) + '\n'
    if args.output:
        args.output.write_text(output, encoding='utf-8')
    else:
        print(output, end='')
    if args.check and any(report[key] for key in ('invalid_asset_json', 'missing_direct_weapon_models', 'missing_local_model_references')):
        raise SystemExit(1)
