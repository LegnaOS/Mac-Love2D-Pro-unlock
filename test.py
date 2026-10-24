#!/usr/bin/env python3
"""Synthetic archive tests. Does not modify any installed editor."""
from pathlib import Path
import tempfile,zipfile,hashlib,subprocess,unittest,os,shutil
ROOT=Path(__file__).resolve().parent
RES=ROOT/'output/LoveLive2D-Mac/LoveLive2D Mac.app/Contents/Resources'
JAVA_HOME=os.environ.get('JAVA_HOME') or subprocess.check_output(['/usr/libexec/java_home','-v','17'],text=True).strip()
JAVA=str(Path(JAVA_HOME)/'bin/java')
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def jar(p,entries):
 with zipfile.ZipFile(p,'w',zipfile.ZIP_DEFLATED) as z:
  for n,b in entries.items():z.writestr(n,b)
class Test(unittest.TestCase):
 def setUp(self):
  self.tmp=tempfile.TemporaryDirectory(prefix='MacTool 大小写 ')
  self.root=Path(self.tmp.name);self.app=self.root/'Cubism Test.app';self.app.mkdir();self.res=self.root/'res';self.res.mkdir()
  jar(self.res/'rlm1501.jar',{'original.class':b'original'})
  h=digest(self.res/'rlm1501.jar').encode()
  jar(self.res/'Live2D_Cubism.jar',{'com/live2d/cubism/g.class':b'\xca\xfe\xba\xbe'+h+b'end','com/live2d/cubism/G.class':b'KEEP CASE','unrelated.txt':b'unchanged','META-INF/MANIFEST.MF':b'manifest','META-INF/X.RSA':b'signature'})
  self.before={p.name:digest(p) for p in self.res.iterdir()}
 def tearDown(self):self.tmp.cleanup()
 def run_tool(self,action,*args):return subprocess.run([JAVA,'-cp',str(RES/'MacTool.jar'),'MacTool',action,str(self.app),*map(str,args)],text=True,capture_output=True)
 def test_inspect_patch_restore(self):
  self.assertIn('MATCHED',self.run_tool('inspect').stdout)
  r=self.run_tool('patch',RES/'rlm1501.jar');self.assertEqual(r.returncode,0,r.stdout+r.stderr)
  with zipfile.ZipFile(self.res/'Live2D_Cubism.jar') as z:
   self.assertEqual(z.read('com/live2d/cubism/G.class'),b'KEEP CASE')
   self.assertEqual(z.read('unrelated.txt'),b'unchanged')
   self.assertIn(digest(RES/'rlm1501.jar').encode(),z.read('com/live2d/cubism/g.class'))
   self.assertNotIn('META-INF/X.RSA',z.namelist())
  b=self.res/'.lovelive2d-mac-backup'
  for name,h in self.before.items():self.assertEqual(digest(b/name),h)
  self.assertNotEqual(self.run_tool('patch',RES/'rlm1501.jar').returncode,0)
  self.assertEqual(self.run_tool('restore').returncode,0)
  self.assertEqual(self.run_tool('restore').returncode,0)
  for name,h in self.before.items():self.assertEqual(digest(self.res/name),h)
 def test_1603_no_mutation(self):
  (self.res/'rlm1501.jar').rename(self.res/'rlm1603.jar')
  before={p.name:digest(p) for p in self.res.iterdir()}
  self.assertIn('UNSUPPORTED',self.run_tool('inspect').stdout)
  self.assertNotEqual(self.run_tool('patch',RES/'rlm1501.jar').returncode,0)
  self.assertEqual({p.name:digest(p) for p in self.res.iterdir()},before)
 def test_missing_hash_no_mutation(self):
  jar(self.res/'Live2D_Cubism.jar',{'com/live2d/cubism/g.class':b'no match'})
  before=digest(self.res/'Live2D_Cubism.jar')
  self.assertIn('UNSUPPORTED',self.run_tool('inspect').stdout)
  self.assertNotEqual(self.run_tool('patch',RES/'rlm1501.jar').returncode,0)
  self.assertEqual(digest(self.res/'Live2D_Cubism.jar'),before)
 def test_tampered_backup_stops_restore(self):
  self.assertEqual(self.run_tool('patch',RES/'rlm1501.jar').returncode,0)
  (self.res/'.lovelive2d-mac-backup/rlm1501.jar').write_bytes(b'tampered')
  before=digest(self.res/'Live2D_Cubism.jar')
  self.assertNotEqual(self.run_tool('restore').returncode,0)
  self.assertEqual(digest(self.res/'Live2D_Cubism.jar'),before)
 def test_external_change_stops_restore(self):
  self.assertEqual(self.run_tool('patch',RES/'rlm1501.jar').returncode,0)
  (self.res/'rlm1501.jar').write_bytes(b'changed by someone else')
  self.assertNotEqual(self.run_tool('restore').returncode,0)
 def test_1603_profile_lifecycle(self):
  cp=str(RES/'rlm1603-payload.jar')+':'+str(RES/'rlm1501.jar')
  javac=str(Path(JAVA).with_name('javac'))
  subprocess.run([javac,'--release','11','-cp',cp,'-d',str(self.root),str(Path(__file__).parent/'test/Rlm1603Test.java')],check=True,capture_output=True)
  p=subprocess.run([JAVA,'-cp',str(self.root)+':'+cp,'Rlm1603Test'],capture_output=True,text=True)
  self.assertEqual(p.returncode,0,p.stdout+p.stderr)
 @unittest.skipUnless(os.environ.get('CUBISM_1603_RES'),'optional local commercial artifact test')
 def test_real_1603_roundtrip(self):
  src=Path(os.environ['CUBISM_1603_RES'])
  (self.res/'rlm1501.jar').unlink()
  names=['Live2D_Cubism.jar','rlm1603.jar']
  before={n:digest(src/n) for n in names}
  for n in names:shutil.copy2(src/n,self.res/n)
  self.assertIn('MATCHED',self.run_tool('inspect').stdout)
  p=self.run_tool('patch',RES/'rlm1603-payload.jar')
  self.assertEqual(p.returncode,0,p.stdout+p.stderr)
  targets={'Live2D_Cubism.jar':'com/live2d/cubism/h.class','rlm1603.jar':'com/reprisesoftware/rlm/RlmLicense.class'}
  for n,target in targets.items():
   with zipfile.ZipFile(src/n) as a,zipfile.ZipFile(self.res/n) as b:
    common=set(a.namelist())&set(b.namelist())
    self.assertEqual([x for x in common if a.read(x)!=b.read(x)],[target])
    self.assertFalse(set(b.namelist())-set(a.namelist()))
    self.assertTrue(all(x.startswith('META-INF/') for x in set(a.namelist())-set(b.namelist())))
  javap=str(Path(JAVA).with_name('javap'))
  def api(jar):return subprocess.check_output([javap,'-protected','-s','-classpath',str(jar),'com.reprisesoftware.rlm.RlmLicense'],text=True)
  self.assertEqual(api(src/'rlm1603.jar'),api(self.res/'rlm1603.jar'))
  self.assertEqual(self.run_tool('restore').returncode,0)
  self.assertEqual({n:digest(self.res/n) for n in names},before)
  self.assertEqual({n:digest(src/n) for n in names},before)
if __name__=='__main__':unittest.main(verbosity=2)
