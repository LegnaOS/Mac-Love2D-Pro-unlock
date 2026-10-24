#!/usr/bin/env python3
"""Package only the built tool and public documentation; never local test fixtures."""
from pathlib import Path
import hashlib, shutil, zipfile
root=Path(__file__).resolve().parent
app=root/'output/LoveLive2D-Mac/LoveLive2D Mac.app'
if not app.is_dir():raise SystemExit('Run bash build.sh first')
dist=root/'dist';dist.mkdir(exist_ok=True)
name='Mac-Love2D-Pro-unlock-v0.2.0-macos'
stage=dist/name
if stage.exists():shutil.rmtree(stage)
stage.mkdir()
shutil.copytree(app,stage/app.name)
for n in ('LICENSE','UPSTREAM.md','README.md','README.en.md','CHANGELOG.md'):
 shutil.copy2(root/n,stage/n)
archive=dist/(name+'.zip')
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
 for p in sorted(stage.rglob('*')):
  if p.is_file():z.write(p,Path(name)/p.relative_to(stage))
with zipfile.ZipFile(archive) as z:
 if z.testzip():raise SystemExit('ZIP CRC failure')
 if any(n.endswith(('Live2D_Cubism.jar','rlm1603.jar','.cmo3','.moc3')) for n in z.namelist()):
  raise SystemExit('Unexpected commercial file')
(dist/'SHA256SUMS.txt').write_text(hashlib.sha256(archive.read_bytes()).hexdigest()+'  '+archive.name+'\n')
print(archive)
