import json, os, glob

src = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'src', 'main', 'resources', 'assets', 'ftgumod', 'lang')

def strip_value(v):
    # 1.12 .lang values may carry § style escapes; keep as-is. Strip trailing spaces.
    return v.strip()

for f in glob.glob(os.path.join(src, '*.lang')):
    base = os.path.basename(f).replace('.lang', '')
    # en_US -> en_us, zh_CN -> zh_cn, en_GB -> en_gb ...
    code = base.lower()
    data = {}
    with open(f, 'r', encoding='utf-8-sig') as fh:
        for line in fh:
            line = line.rstrip('\r\n')
            if not line or line.startswith('#') or line.startswith('//'):
                continue
            if '=' in line:
                k, v = line.split('=', 1)
                data[k] = strip_value(v)
    out = os.path.join(src, code + '.json')
    with open(out, 'w', encoding='utf-8') as fh:
        json.dump(data, fh, ensure_ascii=False, indent=2)
        fh.write('\n')
    os.remove(f)
    print('converted %s -> %s (%d keys)' % (base, code, len(data)))
