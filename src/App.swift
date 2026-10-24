// SPDX-License-Identifier: GPL-3.0-only
import Cocoa

final class AppDelegate: NSObject, NSApplicationDelegate {
    var window: NSWindow!
    let pathField = NSTextField(labelWithString: "请选择 Cubism Editor.app 或安装目录")
    let status = NSTextField(labelWithString: "就绪 · 先检查版本，不自动修改文件")
    let output = NSTextView()
    var selected: URL?
    var busy = false
    var matched = false
    var is1603 = false
    var patchButton: NSButton!
    var restoreButton: NSButton!
    var chooseButton: NSButton!
    var inspectButton: NSButton!
    func applicationDidFinishLaunching(_ note: Notification) {
        NSApp.setActivationPolicy(.regular)
        window = NSWindow(contentRect: NSRect(x:0,y:0,width:800,height:560), styleMask:[.titled,.closable,.miniaturizable,.resizable], backing:.buffered, defer:false)
        window.title="LoveLive2D Mac · 上游兼容移植版";window.minSize=NSSize(width:680,height:460)
        let root=NSStackView();root.orientation = .vertical;root.alignment = .leading;root.spacing=14;root.edgeInsets=NSEdgeInsets(top:24,left:24,bottom:24,right:24)
        root.translatesAutoresizingMaskIntoConstraints=false;window.contentView!.addSubview(root)
        NSLayoutConstraint.activate([root.leadingAnchor.constraint(equalTo:window.contentView!.leadingAnchor),root.trailingAnchor.constraint(equalTo:window.contentView!.trailingAnchor),root.topAnchor.constraint(equalTo:window.contentView!.topAnchor),root.bottomAnchor.constraint(equalTo:window.contentView!.bottomAnchor)])
        let heading=NSTextField(labelWithString:"LoveLive2D · macOS");heading.font = .systemFont(ofSize:24,weight:.bold);root.addArrangedSubview(heading)
        let intro=NSTextField(wrappingLabelWithString:"支持选择 .app、版本核对、原件备份和恢复。当前实现覆盖上游 rlm1501 文件结构；及已核验摘要的 Cubism 5.3.04 / rlm1603 候选适配。");intro.textColor = .secondaryLabelColor;root.addArrangedSubview(intro)
        pathField.lineBreakMode = .byTruncatingMiddle;pathField.font = .monospacedSystemFont(ofSize:12,weight:.regular);root.addArrangedSubview(pathField)
        let row=NSStackView();row.orientation = .horizontal;row.spacing=10
        chooseButton=NSButton(title:"选择 Cubism…",target:self,action:#selector(choose));inspectButton=NSButton(title:"检查版本",target:self,action:#selector(inspect));patchButton=NSButton(title:"备份并应用",target:self,action:#selector(patch));restoreButton=NSButton(title:"恢复原始文件",target:self,action:#selector(restore))
        for b in [chooseButton,inspectButton,patchButton,restoreButton]{row.addArrangedSubview(b!)};root.addArrangedSubview(row)
        status.font = .systemFont(ofSize:13,weight:.medium);root.addArrangedSubview(status)
        let scroll=NSScrollView();scroll.hasVerticalScroller=true;scroll.borderType = .bezelBorder
        output.isEditable=false;output.isSelectable=true;output.font = .monospacedSystemFont(ofSize:12,weight:.regular);output.autoresizingMask = [.width];output.textContainer!.widthTracksTextView=true;scroll.documentView=output
        root.addArrangedSubview(scroll);scroll.widthAnchor.constraint(equalTo:root.widthAnchor,constant:-48).isActive=true;scroll.heightAnchor.constraint(greaterThanOrEqualToConstant:210).isActive=true
        let footer=NSTextField(wrappingLabelWithString:"文件更新成功 ≠ 功能状态已验证。不会操作你的模型工程；应用前请先保存并退出 Cubism。GPL-3.0 · 源码与许可随 ZIP 提供。");footer.textColor = .secondaryLabelColor;footer.font = .systemFont(ofSize:11);root.addArrangedSubview(footer)
        let menu=NSMenu();let item=NSMenuItem();menu.addItem(item);let sub=NSMenu();sub.addItem(withTitle:"退出",action:#selector(NSApplication.terminate(_:)),keyEquivalent:"q");item.submenu=sub;NSApp.mainMenu=menu
        update();window.center();window.makeKeyAndOrderFront(nil);NSApp.activate(ignoringOtherApps:true)
        if let arg=CommandLine.arguments.firstIndex(of:"--inspect"),arg+1<CommandLine.arguments.count{selected=URL(fileURLWithPath:CommandLine.arguments[arg+1]);pathField.stringValue=selected!.path;run("inspect")}
    }
    func update(){chooseButton.isEnabled = !busy;inspectButton.isEnabled = !busy && selected != nil;patchButton.isEnabled = !busy && matched;restoreButton.isEnabled = !busy && selected != nil}
    @objc func choose(){let p=NSOpenPanel();p.canChooseDirectories=true;p.canChooseFiles=true;p.treatsFilePackagesAsDirectories=false;p.allowsMultipleSelection=false;p.prompt="选择";if p.runModal() == .OK,let url=p.url{selected=url;pathField.stringValue=url.path;matched=false;run("inspect")}}
    @objc func inspect(){run("inspect")}
    @objc func patch(){confirm("备份并应用上游适配？",detail:"仅在已匹配的版本目录执行。保留原件和 SHA256，已有备份时停止。",action:"patch")}
    @objc func restore(){confirm("恢复原始文件？",detail:"核验原始备份和当前文件后恢复，保留备份记录。",action:"restore")}
    func confirm(_ title:String,detail:String,action:String){let a=NSAlert();a.messageText=title;a.informativeText=detail;a.addButton(withTitle:"继续");a.addButton(withTitle:"取消");if a.runModal() == .alertFirstButtonReturn{run(action)}}
    func findJava(_ url:URL)->URL? {
        let candidates=[url.appendingPathComponent("../jre/Contents/Home/bin/java"),url.appendingPathComponent("Contents/runtime/Contents/Home/bin/java"),url.appendingPathComponent("Contents/PlugIns/jre.bundle/Contents/Home/bin/java"),url.appendingPathComponent("jre/Contents/Home/bin/java")]
        for c in candidates{let p=c.standardizedFileURL;if FileManager.default.isExecutableFile(atPath:p.path){return p}}
        let p=Process();p.executableURL=URL(fileURLWithPath:"/usr/libexec/java_home");p.arguments=["-v","17+"];let pipe=Pipe();p.standardOutput=pipe;p.standardError=FileHandle.nullDevice
        do{try p.run();let data=pipe.fileHandleForReading.readDataToEndOfFile();p.waitUntilExit();if p.terminationStatus==0,let home=String(data:data,encoding:.utf8)?.trimmingCharacters(in:.whitespacesAndNewlines){return URL(fileURLWithPath:home).appendingPathComponent("bin/java")}}catch{}
        return nil
    }
    func run(_ action:String){guard !busy,let url=selected else{return};busy=true;matched=false;update();status.stringValue="正在\(action == "inspect" ? "检查" : "处理")…"
        DispatchQueue.global(qos:.userInitiated).async {
            var text="",ok=false
            do{
                guard let java=self.findJava(url) else{throw NSError(domain:"MacTool",code:1,userInfo:[NSLocalizedDescriptionKey:"未找到 Cubism 自带 Java 或 Java 17。请选完整安装目录。"])}
                let res=Bundle.main.resourceURL!,p=Process(),pipe=Pipe();p.executableURL=java
                p.arguments=["-cp",res.appendingPathComponent("MacTool.jar").path,"MacTool",action,url.path]
                if action == "patch"{p.arguments!.append(res.appendingPathComponent(self.is1603 ? "rlm1603-payload.jar" : "rlm1501.jar").path)}
                p.standardOutput=pipe;p.standardError=pipe;try p.run();let data=pipe.fileHandleForReading.readDataToEndOfFile();p.waitUntilExit();text=String(decoding:data,as:UTF8.self);ok=p.terminationStatus==0
            }catch{text=error.localizedDescription}
            DispatchQueue.main.async{self.busy=false;self.output.string=text;if action == "inspect" { self.is1603=text.contains("RLM：rlm1603.jar") };self.matched=ok && action == "inspect" && text.contains("\nMATCHED\n");self.status.stringValue = text.contains("UNSUPPORTED") ? "版本不匹配 · 原文件未修改" : (ok ? "检查／文件操作完成，请查看详情" : "操作停止 · 请查看详情");self.update()}
        }
    }
    func applicationShouldTerminateAfterLastWindowClosed(_ app:NSApplication)->Bool{return true}
}
let app=NSApplication.shared
let delegate=AppDelegate();app.delegate=delegate;app.run()
