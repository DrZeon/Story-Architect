package ru.hollowhorizon.hollowengine.client.gui.screengui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hollowengine.storyarchitect

class SettingsScreen : StoryScreen(Component.literal("Настройки")) {
    override val backgroundTexture = ResourceLocation(storyarchitect.MODID, "textures/gui/background.png")
    override val backgroundScale = 0.8f

    private val backButtonTexture = ResourceLocation(storyarchitect.MODID, "textures/gui/icons/recording.png")

    override fun init() {
        super.init()
        val bgArea = getBackgroundArea()

        this.addRenderableWidget(
            ImageButton(
                bgArea.x + 20, bgArea.y + 20,
                16, 16,
                0, 0, 16,
                backButtonTexture,
                16, 16
            ) { _ ->
                minecraft?.setScreen(MenuScreen())
            }
        )
    }

    override fun renderScreenContent(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val bgArea = getBackgroundArea()
        drawCenteredString(poseStack, font, "Настройки", width / 2, bgArea.y + 30, 0xFFAA00)
    }
}