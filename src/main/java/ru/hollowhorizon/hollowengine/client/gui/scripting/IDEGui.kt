/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hollowengine.client.gui.scripting

import imgui.ImGui
import imgui.ImGuiWindowClass
import imgui.ImVec2
import imgui.extension.texteditor.TextEditor
import imgui.extension.texteditor.TextEditorLanguageDefinition
import imgui.flag.*
import imgui.type.ImBoolean
import imgui.type.ImInt
import imgui.type.ImString
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.ModList
import net.minecraftforge.registries.ForgeRegistries
import org.lwjgl.glfw.GLFW
import ru.hollowhorizon.hc.client.handlers.TickHandler
import ru.hollowhorizon.hc.client.imgui.FontAwesomeIcons
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.toTexture
import ru.hollowhorizon.hollowengine.storyarchitect.Companion.MODID
import ru.hollowhorizon.hollowengine.client.gui.height
import ru.hollowhorizon.hollowengine.client.gui.width
import ru.hollowhorizon.hollowengine.client.translate
import ru.hollowhorizon.hollowengine.client.utils.roundTo
import ru.hollowhorizon.hollowengine.common.capabilities.StoriesCapability
import ru.hollowhorizon.hollowengine.common.files.DirectoryManager.toReadablePath
import java.io.File
import kotlin.math.min

object IDEGui {
    val files = HashSet<FileData>()
    var currentFile = ""
    var currentPath = ""
    var selectedPath = ""
    val editor = TextEditor().apply {
        setLanguageDefinition(KOTLIN_LANG)

        tabSize = 4
        text = """
        """.trimIndent()
    }
    var tree = Tree("codeEditor.$MODID.loading".translate, "null")
    var updateTime = 0
    val input = ImString()
    var inputText = ""
    var inputAction = -1
    var shouldClose = false

    fun draw() {
        if (TickHandler.currentTicks - updateTime > 100) {
            updateTime = TickHandler.currentTicks
            RequestTreePacket().send()
        }

        ImGui.setNextWindowPos(0f, 0f)
        ImGui.setNextWindowSize(width, height)
        val shouldDrawWindowContents = ImGui.begin(
            "CodeEditorSpace",
            ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoTitleBar
        )
        val dockspaceID = ImGui.getID("MyWindow_DockSpace")
        val workspaceWindowClass = ImGuiWindowClass()
        workspaceWindowClass.setClassId(dockspaceID)
        workspaceWindowClass.dockingAllowUnclassed = false

        if (imgui.internal.ImGui.dockBuilderGetNode(dockspaceID).ptr == 0L) {
            imgui.internal.ImGui.dockBuilderAddNode(
                dockspaceID, imgui.internal.flag.ImGuiDockNodeFlags.DockSpace or
                        imgui.internal.flag.ImGuiDockNodeFlags.NoWindowMenuButton or
                        imgui.internal.flag.ImGuiDockNodeFlags.NoCloseButton
            )
            val region = ImGui.getContentRegionAvail()
            imgui.internal.ImGui.dockBuilderSetNodeSize(dockspaceID, region.x, region.y)

            val leftDockID = ImInt(0)
            val rightDockID = ImInt(0)
            imgui.internal.ImGui.dockBuilderSplitNode(dockspaceID, ImGuiDir.Left, 0.4f, leftDockID, rightDockID);

            val pLeftNode = imgui.internal.ImGui.dockBuilderGetNode(leftDockID.get())
            val pRightNode = imgui.internal.ImGui.dockBuilderGetNode(rightDockID.get())
            pLeftNode.localFlags = pLeftNode.localFlags or imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoDockingSplitMe or imgui.internal.flag.ImGuiDockNodeFlags.NoDockingOverMe or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar
            pRightNode.localFlags = pRightNode.localFlags or imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoDockingSplitMe or imgui.internal.flag.ImGuiDockNodeFlags.NoDockingOverMe or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar

            // Dock windows
            imgui.internal.ImGui.dockBuilderDockWindow("File Tree", leftDockID.get())
            imgui.internal.ImGui.dockBuilderDockWindow("Code Editor", rightDockID.get())

            imgui.internal.ImGui.dockBuilderFinish(dockspaceID)
        }

        val dockFlags = if (shouldDrawWindowContents) ImGuiDockNodeFlags.None
        else ImGuiDockNodeFlags.KeepAliveOnly
        val region = ImGui.getContentRegionAvail()
        ImGui.dockSpace(dockspaceID, region.x, region.y, dockFlags, workspaceWindowClass)
        ImGui.end()

        val windowClass = ImGuiWindowClass()
        windowClass.dockNodeFlagsOverrideSet = imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar

        ImGui.setNextWindowClass(windowClass)

        ImGui.begin(
            "File Tree",
            ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoTitleBar or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar
        )
        drawTree(tree)
        ImGui.end()

        ImGui.begin(
            "Code Editor",
            ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoTitleBar
        )

        if (currentPath.endsWith(".kts") && files.isNotEmpty()) {
            val engine = ModList.get().getModContainerById("storyarchitect").get().modInfo
            val compiler = ModList.get().getModContainerById("kotlinscript").get().modInfo
            ImGui.text("Minecraft ${Minecraft.getInstance().game.version.name} | ${engine.displayName} ${engine.version} | ${compiler.displayName} ${compiler.version}")
            ImGui.sameLine()
            ImGui.setCursorPosX(ImGui.getWindowWidth() - 50f)
            val stories = Minecraft.getInstance().level!![StoriesCapability::class].stories
            if(!stories.contains(currentPath)) {
                if (ImGui.imageButton("storyarchitect:textures/gui/play.png".rl.toTexture().id, 32f, 32f)) {
                    RunScriptPacket(currentPath).send()
                }
                if (ImGui.isItemHovered()) ImGui.setTooltip("codeEditor.$MODID.script.run".translate)
            } else {
                if (ImGui.imageButton("storyarchitect:textures/gui/stop.png".rl.toTexture().id, 32f, 32f)) {
                    StopScriptPacket(currentPath).send()
                }
                if (ImGui.isItemHovered()) ImGui.setTooltip("codeEditor.$MODID.script.stop".translate)
            }
        }

        ImGui.beginTabBar("##Files")
        files.removeIf { file ->
            val lastOpen = file.open.get()
            if (ImGui.beginTabItem(file.name, file.open, ImGuiTabItemFlags.None)) {
                file.draw()
                ImGui.endTabItem()
            }
            lastOpen && !file.open.get()
        }
        ImGui.endTabBar()
        ImGui.end()

        drawModalInput()

        if (shouldClose) {
            ImGui.setMouseCursor(0)
            GLFW.glfwSetCursor(
                Minecraft.getInstance().window.window,
                GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
            )
            shouldClose = false
        }
    }

    fun complete(c: Char) {
        if (ImGui.getIO().getKeysDown(ImGui.getIO().getKeyMap(ImGuiKey.Delete))) return
        if (ImGui.getIO().getKeysDown(ImGui.getIO().getKeyMap(ImGuiKey.Backspace))) return

        val chars = setOf('(', '{', '[', '"')

        val completeChars = arrayOf(')', '}', ']', '"')

        if (c in chars) {
            editor.insertText(completeChars[chars.indexOf(c)].toString())
            editor.setCursorPosition(editor.cursorPositionLine, editor.cursorPositionColumn - 1)
        }
    }

    fun drawTree(tree: Tree) {
        val flags =
            if (tree.drawArrow) ImGuiTreeNodeFlags.SpanFullWidth else ImGuiTreeNodeFlags.NoTreePushOnOpen or ImGuiTreeNodeFlags.Leaf or ImGuiTreeNodeFlags.SpanFullWidth

        drawFolderPopup(tree.path)
        drawFilePopup(tree.path)
        var hovered = false
        var ignore = false
        if (ImGui.treeNodeEx(icon(tree.drawArrow, tree.value.substringAfterLast(".")) + " " + tree.value, flags)) {
            hovered = ImGui.isItemHovered()
            tree.children.forEach { drawTree(it) }

            ignore = true
            if (tree.drawArrow) ImGui.treePop()
        }
        hovered = hovered || (ImGui.isItemHovered() && !ignore)
        if (hovered && ImGui.isMouseClicked(1)) {
            selectedPath = tree.path
            if (tree.drawArrow) ImGui.openPopup("FolderTreePopup##" + tree.path)
            else ImGui.openPopup("FileTreePopup##" + tree.path)
        }
        if ((tree.path.startsWith("assets") || tree.path.startsWith("data")) && !tree.drawArrow && ImGui.beginDragDropSource()) {
            ImGui.setDragDropPayload("TREE", tree.path, ImGuiCond.Once)
            ImGui.pushItemWidth(350f)
            ImGui.text(tree.path.substringAfter('/').replaceFirst('/', ':'))
            files.find { it.path == tree.path && it is ImageData }?.let { data ->
                val image = (data as ImageData).image

                val imageWidth = image.pixels?.width?.toFloat() ?: 1f
                val imageHeight = image.pixels?.height?.toFloat() ?: 1f
                val size = ImVec2(350f, 350f)

                val scale = min(size.x / imageWidth, size.y / imageHeight)
                val width = imageWidth * scale
                val height = imageHeight * scale

                ImGui.image(image.id, width, height)
            }
            ImGui.popItemWidth()
            ImGui.endDragDropSource()
        }

        if (ImGui.isItemActivated() && ImGui.isMouseDoubleClicked(0) && !tree.drawArrow) {
            RequestFilePacket(tree.path).send()
        }
    }

    private fun icon(isFolder: Boolean, ext: String): String {
        return if (isFolder) FontAwesomeIcons.Folder
        else when (ext) {
            "kts" -> FontAwesomeIcons.FileCode
            "json", "txt", "mcfunction", "md" -> FontAwesomeIcons.FileAlt
            "jar", "zip" -> FontAwesomeIcons.FileArchive
            "png", "jpg", "jpeg" -> FontAwesomeIcons.FileImage
            "mp3", "wav", "ogg" -> FontAwesomeIcons.FileAudio
            else -> FontAwesomeIcons.File
        }
    }

    fun drawScriptPopup() {
        val player = Minecraft.getInstance().player ?: return

        if (ImGui.beginPopup("ScriptPopup")) {
            if (ImGui.menuItem(FontAwesomeIcons.Globe + " " + "codeEditor.$MODID.insert.pos".translate)) {
                val loc = player.position()
                val text = "pos(${loc.x.roundTo(2)}, ${loc.y.roundTo(2)}, ${loc.z.roundTo(2)})"
                insertAtCursor(text)
                ImGui.closeCurrentPopup()
            }
            if (ImGui.menuItem(FontAwesomeIcons.Eye + " " + "codeEditor.$MODID.insert.look".translate)) {
                val loc = player.pick(100.0, 0.0f, true).location
                val text = "pos(${loc.x.roundTo(2)}, ${loc.y.roundTo(2)}, ${loc.z.roundTo(2)})"
                insertAtCursor(text)
                ImGui.closeCurrentPopup()
            }
            if (ImGui.menuItem(FontAwesomeIcons.HandPaper + " " + "codeEditor.$MODID.insert.itemInHand".translate)) {
                val item = player.mainHandItem
                val location = "\"" + ForgeRegistries.ITEMS.getKey(item.item).toString() + "\""
                val count = item.count
                val nbt = item.tag
                val text = when {
                    nbt == null && count > 1 -> "item($location, $count)"
                    nbt == null && count == 1 -> "item($location)"
                    else -> {
                        "item($location, $count, \"${
                            nbt.toString()
                                .replace("\"", "\\\"")
                        }\")"
                    }
                }
                insertAtCursor(text)
                ImGui.closeCurrentPopup()
            }
            if (ImGui.menuItem(FontAwesomeIcons.Toolbox + " " + "codeEditor.$MODID.insert.itemFromInv".translate)) {
                insertAtCursor("codeEditor.$MODID.inNewVersion".translate)
                ImGui.closeCurrentPopup()
            }
            ImGui.endPopup()
        }
    }

    fun insertAtCursor(text: String) {
        if (editor.hasSelection()) {
            editor.text = editor.text.substringBeforeLast("\n").replace(editor.selectedText, text)
            editor.setSelectionStart(0, 0)
            editor.setSelectionEnd(0, 0)
        } else editor.insertText(text)
    }

    fun drawFolderPopup(folder: String) {
        if (ImGui.beginPopup("FileTreePopup##$folder")) {
            if (ImGui.menuItem(FontAwesomeIcons.Pen + " " + "codeEditor.$MODID.rename".translate)) {
                inputAction = 0
                inputText = "codeEditor.$MODID.rename.new".translate + ":"
                ImGui.closeCurrentPopup()
            }
            if (ImGui.menuItem(FontAwesomeIcons.TrashAlt + " " + "codeEditor.$MODID.delete".translate)) {
                inputAction = 1
                inputText = "codeEditor.$MODID.delete.warning".translate + "\n" + "codeEditor.$MODID.delete.warning.script".translate + "?"
                ImGui.closeCurrentPopup()
            }
            ImGui.endPopup()
        }
    }

    fun drawFilePopup(file: String) {
        if (ImGui.beginPopup("FolderTreePopup##$file")) {
            if (ImGui.menuItem(FontAwesomeIcons.Folder + " " + "codeEditor.$MODID.create".translate + " " + "codeEditor.$MODID.create.folder".translate)) {
                inputAction = 2
                inputText = "codeEditor.hollowengine.enter".translate + " " + "codeEditor.hollowengine.enter.directory".translate + ":"
                ImGui.closeCurrentPopup()
            }

            if (ImGui.menuItem(FontAwesomeIcons.FileCode + " " + "codeEditor.$MODID.create".translate + " " + "codeEditor.$MODID.create.story".translate)) {
                inputAction = 3
                inputText = "codeEditor.hollowengine.enter".translate + " " + "codeEditor.hollowengine.enter.script".translate + ":"
                ImGui.closeCurrentPopup()
            }

            if (ImGui.menuItem(FontAwesomeIcons.FileCode + " " + "codeEditor.$MODID.create".translate + " " + "codeEditor.$MODID.create.content".translate)) {
                inputAction = 4
                inputText = "codeEditor.hollowengine.enter".translate + " " + "codeEditor.hollowengine.enter.script".translate + ":"
                ImGui.closeCurrentPopup()
            }
            if (ImGui.menuItem(FontAwesomeIcons.FileCode + " " + "codeEditor.$MODID.create".translate + " " + "codeEditor.$MODID.create.mod".translate)) {
                inputAction = 5
                inputText = "codeEditor.hollowengine.enter".translate + " " + "codeEditor.hollowengine.enter.script".translate + ":"
                ImGui.closeCurrentPopup()
            }

            if (ImGui.menuItem(FontAwesomeIcons.TrashAlt + " " + "codeEditor.$MODID.delete.dir".translate)) {
                inputAction = 6
                inputText = "codeEditor.$MODID.delete.warning".translate + "\n" + "codeEditor.$MODID.delete.warning.dir".translate + "?"
                ImGui.closeCurrentPopup()
            }
            ImGui.endPopup()
        }
    }

    fun drawModalInput() {
        val center = ImGui.getMainViewport().center
        ImGui.setNextWindowPos(center.x, center.y, ImGuiCond.Appearing, 0.5f, 0.5f);

        if (inputAction != -1) {
            ImGui.openPopup("Input")
        }

        if (ImGui.beginPopupModal(
                "Input", ImBoolean(true), ImGuiWindowFlags.AlwaysAutoResize or
                        ImGuiWindowFlags.NoTitleBar
            )
        ) {
            ImGui.text(inputText)
            ImGui.separator()

            if (inputAction == 1 || inputAction == 6) {
                if (ImGui.button("codeEditor.$MODID.yes".translate, 120f, 0f)) {
                    inputAction = -1
                    files.removeIf { it.path.startsWith(selectedPath) }
                    if (selectedPath.isNotEmpty()) DeleteFilePacket(selectedPath).send()
                    ImGui.closeCurrentPopup()
                    input.set("")
                }
                ImGui.sameLine()
                if (ImGui.button("codeEditor.$MODID.no".translate, 120f, 0f)) {
                    inputAction = -1
                    ImGui.closeCurrentPopup()
                    input.set("")
                }
            } else {
                ImGui.inputText("##Filename", input)

                if (ImGui.button("OK", 120f, 0f)) {
                    val input = input.get()

                    when (inputAction) {
                        0 -> {
                            RenameFilePacket(selectedPath, input).send()
                            files.removeIf { it.path == selectedPath }
                        }

                        2 -> CreateFilePacket("$selectedPath/$input").send()
                        3 -> CreateFilePacket("$selectedPath/$input.se.kts").send()
                        4 -> CreateFilePacket("$selectedPath/$input.content.kts").send()
                        5 -> CreateFilePacket("$selectedPath/$input.mod.kts").send()
                    }

                    inputAction = -1
                    ImGui.closeCurrentPopup()
                    this.input.set("")
                }
                ImGui.sameLine()
                if (ImGui.button("codeEditor.$MODID.no".translate, 120f, 0f)) {
                    inputAction = -1
                    ImGui.closeCurrentPopup()
                    input.set("")
                }
            }
            ImGui.endPopup()
        }
    }

    fun tree(file: File): Tree {
        val tree = Tree(file.name, file.toReadablePath())
        tree.drawArrow = file.isDirectory
        file.listFiles()?.sortedBy { if (it.isDirectory) 0 else 1 }?.forEach { tree.children.add(tree(it)) }
        return tree
    }

    fun onClose() {
        files.forEach { it.save() }
    }
}

@Serializable
class Tree(val value: String, val path: String) {
    var drawArrow = true
    val children: MutableList<Tree> = ArrayList()
}

val KOTLIN_LANG = TextEditorLanguageDefinition.c().apply {
    setPreprocChar('@')
    setKeywords(
        arrayOf(
            "break", "continue", "switch", "case", "try",
            "catch", "delete", "do", "while", "else", "finally", "if",
            "else", "for", "is", "as", "in", "instanceof",
            "new", "throw", "typeof", "with", "yield", "when", "return",
            "by", "constructor", "delegate", "dynamic", "field", "get", "set", "init", "value",
            "where", "actual", "annotation", "companion", "field", "external", "infix", "inline", "inner", "internal",
            "open", "operator", "out", "override", "suspend", "vararg",
            "abstract", "extends", "final", "implements", "interface", "super", "throws",
            "data", "class", "fun", "var", "val", "import", "Java", "JSON"
        )
    )

    setName("KotlinScript")

    setSingleLineComment("//")
    setCommentStart("/*")
    setCommentEnd("*/")

    setAutoIdentation(true)
}

