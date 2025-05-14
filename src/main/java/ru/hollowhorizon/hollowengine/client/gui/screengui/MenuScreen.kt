package ru.hollowhorizon.hollowengine.client.gui.screengui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hollowengine.client.gui.widget.TexturedButton
import ru.hollowhorizon.hollowengine.storyarchitect

class MenuScreen : StoryScreen(Component.literal("Story Architect")) {
    override val backgroundTexture = ResourceLocation(storyarchitect.MODID, "textures/gui/background.png")
    override val backgroundScale = 0.8f

    private val buttonTexture = ResourceLocation(storyarchitect.MODID, "textures/gui/dialogues/choice_button.png")

    override fun init() {
        super.init()
        val bgArea = getBackgroundArea()

        this.addRenderableWidget(
            TexturedButton(
                width / 2 - 100, bgArea.y + 50,
                200, 20,
                0, 0, 20,
                buttonTexture,
                200, 40,
                "Персонажи"
            ) { _ ->
                minecraft?.setScreen(CharactersScreen())
            }
        )

        this.addRenderableWidget(
            TexturedButton(
                width / 2 - 100, bgArea.y + 75,
                200, 20,
                0, 0, 20,
                buttonTexture,
                200, 40,
                "Игроки"
            ) { _ ->
                minecraft?.setScreen(PlayersScreen())
            }
        )

        this.addRenderableWidget(
            TexturedButton(
                width / 2 - 100, bgArea.y + 100,
                200, 20,
                0, 0, 20,
                buttonTexture,
                200, 40,
                "Настройки"
            ) { _ ->
                minecraft?.setScreen(SettingsScreen())
            }
        )
    }

    override fun renderScreenContent(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val bgArea = getBackgroundArea()
        drawCenteredString(poseStack, font, "Главное меню", width / 2, bgArea.y + 30, 0xFFFFFF)
    }
}