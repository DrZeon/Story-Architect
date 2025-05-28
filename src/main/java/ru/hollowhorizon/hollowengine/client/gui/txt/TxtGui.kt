package ru.hollowhorizon.hollowengine.client.gui.txt

import com.mojang.blaze3d.vertex.PoseStack
import imgui.ImGui
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import ru.hollowhorizon.hc.client.imgui.ImguiHandler
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hollowengine.common.files.DirectoryManager

class TxtGui(private val titleString: String, private val filePath: String) : HollowScreen() {
    private var fileContent: String = ""

    override fun getTitle(): Component {
        return Component.literal(titleString)
    }

    override fun init() {
        super.init()
        val file = DirectoryManager.HOLLOW_ENGINE.resolve(filePath)

        fileContent = when {
            file.exists() && file.isFile -> {
                try {
                    file.readText()
                } catch (e: Exception) {
                    "Error reading file: ${e.message}"
                }
            }
            else -> "File not found: $filePath"
        }
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBackground(poseStack)
        ImguiHandler.drawFrame {
            val window = Minecraft.getInstance().window

            val windowWidth = window.width * 0.7f
            val windowHeight = window.height * 0.7f

            val posX = (window.width - windowWidth) / 2f
            val posY = (window.height - windowHeight) / 2f

            ImGui.setNextWindowPos(posX, posY)
            ImGui.setNextWindowSize(windowWidth, windowHeight)

            ImGui.begin(titleString)
            ImGui.textWrapped(fileContent)
            ImGui.spacing()
            ImGui.spacing()
            if (ImGui.button("Закрыть")) {
                Minecraft.getInstance().setScreen(null)
            }
            ImGui.end()
        }
    }

    override fun isPauseScreen() = false
}