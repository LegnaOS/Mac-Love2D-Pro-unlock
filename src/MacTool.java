/* SPDX-License-Identifier: GPL-3.0-only
 * Mac transaction wrapper for LoveLive2D. See UPSTREAM.md. */
import java.nio.file.*;
import java.io.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;
import java.util.stream.*;

public final class MacTool {
    static final String MAIN="Live2D_Cubism.jar", RLM="rlm1501.jar";
    static final long MAX_ENTRY=64L*1024*1024, MAX_ARCHIVE=512L*1024*1024;
    static final String HASH_ENTRY="com/live2d/cubism/g.class";
    static final String MAIN_5304="c9bb3e9960bf49908a531b7010421c393a8dd5f9376745eb6fdb273f41a1cf5d";
    static final String RLM_1603="f755966ea45739ef3706e324fa001b83058c8c430a44f754c0412d80403b60cd";
    static String resource(Path dir) throws Exception {
        if(Files.isRegularFile(dir.resolve(RLM)))return RLM;
        if(Files.isRegularFile(dir.resolve("rlm1603.jar")))return "rlm1603.jar";
        throw new IOException("未发现受支持的RLM资源");
    }
    static String hashEntry(String rlm) { return rlm.equals(RLM)?HASH_ENTRY:"com/live2d/cubism/h.class"; }
    static void replaceLicenseClass(Path original,Path payload,Path dest)throws Exception {
        validate(payload);
        String target="com/reprisesoftware/rlm/RlmLicense.class";
        try(ZipFile patch=new ZipFile(payload.toFile());ZipFile src=new ZipFile(original.toFile());
            ZipOutputStream out=new ZipOutputStream(Files.newOutputStream(dest))) {
            byte[] changed=entry(patch,patch.getEntry(target));
            if(src.getEntry(target)==null)throw new IOException("原始RLM缺少目标类");
            for(ZipEntry e:Collections.list(src.entries())) {
                if(signature(e.getName()))continue;
                ZipEntry ne=new ZipEntry(e.getName());if(e.getTime()!=-1)ne.setTime(e.getTime());out.putNextEntry(ne);
                if(!e.isDirectory())out.write(e.getName().equals(target)?changed:entry(src,e));
                out.closeEntry();
            }
        }
        validate(dest);
    }
    static String sha(Path p) throws Exception {return hex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(p)));}
    static String hex(byte[] b){StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x&255));return s.toString();}
    static Path locate(Path selected) throws Exception {
        Path p=selected.toRealPath();
        List<Path> candidates=List.of(p.resolve("Contents/app/lib"),p.resolve("Contents/Resources/app/lib"),
            p.resolve("Contents/Resources/lib"),p.resolve("Contents/Resources/res"),p.resolve("app/lib"),p.resolve("res"),
            p.resolve("../res").normalize(),p);
        LinkedHashSet<Path> found=new LinkedHashSet<>();
        for(Path c:candidates)if(Files.isRegularFile(c.resolve(MAIN)))found.add(c.toRealPath());
        if(found.size()!=1)throw new IOException("应找到唯一 Cubism 资源目录，实际找到 "+found.size()+" 个");
        return found.iterator().next();
    }
    static byte[] entry(ZipFile z,ZipEntry e)throws Exception{
        if(e==null||e.isDirectory()||e.getSize()<0||e.getSize()>MAX_ENTRY)throw new IOException("JAR条目缺失或超出大小限制");
        try(InputStream in=z.getInputStream(e)){byte[] b=in.readNBytes((int)MAX_ENTRY+1);if(b.length>MAX_ENTRY)throw new IOException("条目过大");return b;}
    }
    static void validate(Path p)throws Exception{
        if(Files.size(p)>MAX_ARCHIVE)throw new IOException("JAR过大");
        try(ZipFile z=new ZipFile(p.toFile())){Set<String> names=new HashSet<>();long total=0;
            for(ZipEntry e:Collections.list(z.entries())){if(!names.add(e.getName()))throw new IOException("重复JAR条目");
                if(!e.isDirectory()){byte[] b=entry(z,e);CRC32 c=new CRC32();c.update(b);if(c.getValue()!=e.getCrc())throw new IOException("CRC不匹配");total+=b.length;if(total>MAX_ARCHIVE)throw new IOException("解压内容过大");}}
        }
    }
    static int count(byte[] hay,byte[] needle){int n=0;outer:for(int i=0;i<=hay.length-needle.length;i++){for(int j=0;j<needle.length;j++)if(hay[i+j]!=needle[j])continue outer;n++;}return n;}
    static byte[] replace(byte[] hay,byte[] old,byte[] next){byte[] out=hay.clone();outer:for(int i=0;i<=out.length-old.length;i++){for(int j=0;j<old.length;j++)if(out[i+j]!=old[j])continue outer;System.arraycopy(next,0,out,i,next.length);i+=old.length-1;}return out;}
    static byte[] hashBytes(String hash){return hash.getBytes(java.nio.charset.StandardCharsets.US_ASCII);}
    static boolean signature(String n){String u=n.toUpperCase(Locale.ROOT);return u.startsWith("META-INF/")&&(u.endsWith(".SF")||u.endsWith(".RSA")||u.endsWith(".DSA")||u.endsWith(".EC")||u.equals("META-INF/MANIFEST.MF"));}
    static void rebuild(Path source,Path dest,String old,String next,String targetEntry)throws Exception{
        try(ZipFile z=new ZipFile(source.toFile());ZipOutputStream out=new ZipOutputStream(Files.newOutputStream(dest))){
            for(ZipEntry e:Collections.list(z.entries())){
                if(signature(e.getName()))continue;
                ZipEntry ne=new ZipEntry(e.getName());if(e.getTime()!=-1)ne.setTime(e.getTime());out.putNextEntry(ne);
                if(!e.isDirectory()){byte[] b=entry(z,e);if(e.getName().equals(targetEntry)){
                    b=replace(b,hashBytes(old),hashBytes(next));b=replace(b,hashBytes(old.toUpperCase(Locale.ROOT)),hashBytes(next.toUpperCase(Locale.ROOT)));}
                    out.write(b);}
                out.closeEntry();
            }
        }
        validate(dest);
    }
    static String compatibility(Path dir)throws Exception {
        String rlm=resource(dir);
        validate(dir.resolve(MAIN));validate(dir.resolve(rlm));String old=sha(dir.resolve(rlm));
        if(rlm.equals("rlm1603.jar") && (!old.equals(RLM_1603)||!sha(dir.resolve(MAIN)).equals(MAIN_5304)))
            return "UNSUPPORTED\n1603仅适配已核验的5.3.04文件摘要；当前文件不匹配，保持原件。";
        try(ZipFile z=new ZipFile(dir.resolve(MAIN).toFile())){
            ZipEntry e=z.getEntry(hashEntry(rlm));if(e==null)return "UNSUPPORTED\n未找到上游版本对应的类；保持原文件。";
            byte[] b=entry(z,e);int hits=count(b,hashBytes(old))+count(b,hashBytes(old.toUpperCase(Locale.ROOT)));
            if(hits!=1)return "UNSUPPORTED\n原资源摘要引用不是唯一匹配，命中 "+hits+" 处；保持原文件。";
        }
        return "MATCHED\n文件结构与适配规则匹配；实际功能状态仍须启动验证。";
    }
    static String listRlm(Path dir)throws Exception{try(Stream<Path>s=Files.list(dir)){return s.filter(p->p.getFileName().toString().matches("rlm.*\\.jar")).map(p->p.getFileName().toString()).collect(Collectors.joining(", "));}}
    static void inspect(Path dir)throws Exception{
        System.out.println("目录："+dir+"\n主程序 SHA256："+sha(dir.resolve(MAIN))+"\nRLM："+listRlm(dir));
        System.out.println(compatibility(dir));
        Path b=dir.resolve(".lovelive2d-mac-backup");if(Files.isDirectory(b))System.out.println("备份存在："+b);
    }
    static void move(Path a,Path b)throws Exception{Files.move(a,b,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
    static Properties props(Path file)throws Exception{Properties p=new Properties();try(InputStream in=Files.newInputStream(file)){p.load(in);}return p;}
    static void save(Properties p,Path file)throws Exception{try(OutputStream out=Files.newOutputStream(file,StandardOpenOption.CREATE_NEW)){p.store(out,"LoveLive2D Mac transaction v1");}}
    static void runningGuard(Path dir)throws Exception{
        String root=dir.getParent().toString();boolean running=ProcessHandle.allProcesses().anyMatch(p->{
            if(p.pid()==ProcessHandle.current().pid())return false;String name=p.info().command().orElse("");String cmd=p.info().commandLine().orElse("");
            boolean editor=name.endsWith("/CubismEditor")||cmd.contains("com.live2d.cubism.CECubismEditorApp");
            return editor&&cmd.contains(root);});
        if(running)throw new IOException("请先在 Cubism 保存工程并退出，再执行文件操作。");
    }
    static void patch(Path dir,Path replacement)throws Exception {
        runningGuard(dir);
        if(!compatibility(dir).startsWith("MATCHED"))throw new IOException(compatibility(dir));
        String rlmName=resource(dir);
        Path main=dir.resolve(MAIN),rlm=dir.resolve(rlmName),backup=dir.resolve(".lovelive2d-mac-backup");
        if(Files.exists(backup))throw new IOException("已有备份，先检查或恢复；不会覆盖原始备份。");
        validate(replacement);
        try(ZipFile z=new ZipFile(replacement.toFile())){if(z.getEntry("com/reprisesoftware/rlm/RlmLicense.class")==null)throw new IOException("替换资源内容错误");}
        Path stage=Files.createTempDirectory(dir,".lovelive2d-stage-");
        boolean backupReady=false,started=false;
        try{
            String oldMain=sha(main),oldRlm=sha(rlm);
            if(rlmName.equals(RLM)) Files.copy(replacement,stage.resolve(rlmName));
            else replaceLicenseClass(rlm,replacement,stage.resolve(rlmName));
            rebuild(main,stage.resolve(MAIN),oldRlm,sha(stage.resolve(rlmName)),hashEntry(rlmName));
            if(!sha(main).equals(oldMain)||!sha(rlm).equals(oldRlm))throw new IOException("处理期间原文件发生变化");
            Files.createDirectory(backup);Files.copy(main,backup.resolve(MAIN));Files.copy(rlm,backup.resolve(rlmName));
            Properties p=new Properties();p.setProperty("resource.name",rlmName);p.setProperty("original.main",oldMain);p.setProperty("original.rlm",oldRlm);
            p.setProperty("patched.main",sha(stage.resolve(MAIN)));p.setProperty("patched.rlm",sha(stage.resolve(rlmName)));save(p,backup.resolve("transaction.properties"));
            if(!sha(backup.resolve(MAIN)).equals(oldMain)||!sha(backup.resolve(rlmName)).equals(oldRlm))throw new IOException("备份校验失败");backupReady=true;
            Files.setPosixFilePermissions(stage.resolve(MAIN),Files.getPosixFilePermissions(main));Files.setPosixFilePermissions(stage.resolve(rlmName),Files.getPosixFilePermissions(rlm));
            started=true;move(stage.resolve(rlmName),rlm);move(stage.resolve(MAIN),main);
            if(!sha(main).equals(p.getProperty("patched.main"))||!sha(rlm).equals(p.getProperty("patched.rlm")))throw new IOException("写后校验失败");
            System.out.println("PATCHED\n文件更新及SHA256校验完成。备份："+backup+"\n下一步手工启动Cubism验证实际功能；此结果不是PRO状态验证。");
        }catch(Exception e){
            if(started&&backupReady){Files.copy(backup.resolve(MAIN),main,StandardCopyOption.REPLACE_EXISTING);Files.copy(backup.resolve(rlmName),rlm,StandardCopyOption.REPLACE_EXISTING);}
            throw e;
        }finally{try(Stream<Path>s=Files.walk(stage)){for(Path p:s.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))Files.deleteIfExists(p);}}
    }
    static void restore(Path dir)throws Exception{
        runningGuard(dir);Path backup=dir.resolve(".lovelive2d-mac-backup");Properties p=props(backup.resolve("transaction.properties"));
        String rlmName=p.getProperty("resource.name",RLM);
        if(!List.of(RLM,"rlm1603.jar").contains(rlmName))throw new IOException("备份资源名无效");
        for(String file:List.of(MAIN,rlmName)){String key=file.equals(MAIN)?"main":"rlm";
            if(!sha(backup.resolve(file)).equals(p.getProperty("original."+key)))throw new IOException("原始备份摘要不匹配");
            String now=sha(dir.resolve(file));if(!now.equals(p.getProperty("original."+key))&&!now.equals(p.getProperty("patched."+key)))throw new IOException("当前文件有外部修改，保留现场与备份");}
        for(String file:List.of(MAIN,rlmName)){Path temp=Files.createTempFile(dir,".lovelive2d-restore-",".tmp");try{Files.copy(backup.resolve(file),temp,StandardCopyOption.REPLACE_EXISTING);move(temp,dir.resolve(file));}finally{Files.deleteIfExists(temp);}}
        for(String file:List.of(MAIN,rlmName))if(!sha(dir.resolve(file)).equals(sha(backup.resolve(file))))throw new IOException("恢复校验失败");
        System.out.println("RESTORED\n原始文件已恢复并逐文件SHA256校验。备份保留。工程文件未改变。");
    }
    public static void main(String[] args){try{
        if(args.length<2)throw new IllegalArgumentException("inspect|patch|restore <Cubism.app or install folder> [replacement.jar]");
        Path dir=locate(Paths.get(args[1]));
        switch(args[0]){case "inspect":inspect(dir);break;case "patch":if(args.length!=3)throw new IllegalArgumentException("replacement.jar required");patch(dir,Paths.get(args[2]));break;case "restore":restore(dir);break;default:throw new IllegalArgumentException("Unknown action");}
    }catch(Exception e){System.err.println("ERROR: "+e.getMessage());System.exit(2);}}
}
