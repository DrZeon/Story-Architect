package ru.hollowhorizon.hollowengine.client.gui.testGui

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import ru.hollowhorizon.hollowengine.storyarchitect;

abstract class StoryScreen(title: Component) : Screen(title) {
    protected var transitionProgress = 0f
    protected abstract val backgroundTexture: ResourceLocation
    protected abstract val backgroundScale: Float

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        renderBackground(poseStack)

        val bgWidth = (width * backgroundScale).toInt()
        val bgHeight = (height * backgroundScale).toInt()
        val bgX = (width - bgWidth) / 2
        val bgY = (height - bgHeight) / 2

        RenderSystem.setShaderTexture(0, backgroundTexture)
        blit(poseStack, bgX, bgY, bgWidth, bgHeight, 0f, 0f, bgWidth, bgHeight, bgWidth, bgHeight)

        this.transitionProgress = Mth.lerp(partialTicks * 5f, transitionProgress, 1f)

        poseStack.pushPose()
        poseStack.translate(0.0, (1 - transitionProgress) * height * 0.1, 0.0)

        renderScreenContent(poseStack, mouseX, mouseY, partialTicks)

        poseStack.popPose()
        super.render(poseStack, mouseX, mouseY, partialTicks)
    }

    protected fun getBackgroundArea(): BackgroundArea {
        val bgWidth = (width * backgroundScale).toInt()
        val bgHeight = (height * backgroundScale).toInt()
        val bgX = (width - bgWidth) / 2
        val bgY = (height - bgHeight) / 2
        return BackgroundArea(bgX, bgY, bgWidth, bgHeight)
    }

    abstract fun renderScreenContent(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float)

    data class BackgroundArea(val x: Int, val y: Int, val width: Int, val height: Int)
}


class MenuScreen : StoryScreen(Component.literal("Story Architect GUI")) {
    override val backgroundTexture = ResourceLocation(storyarchitect.MODID, "textures/gui/background.png")
    override val backgroundScale = 0.8f // 80% заполнения

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
                width / 2 - 100, bgArea.y + 90,
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


class CharactersScreen : StoryScreen(Component.literal("Персонажи")) {
    override val backgroundTexture = ResourceLocation(storyarchitect.MODID, "textures/gui/background.png")
    override val backgroundScale = 0.8f // 80% заполнения

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
        drawCenteredString(poseStack, font, "Персонажи", width / 2, bgArea.y + 30, 0x55FF55)
    }
}


class SettingsScreen : StoryScreen(Component.literal("Настройки")) {
    override val backgroundTexture = ResourceLocation(storyarchitect.MODID, "textures/gui/background.png")
    override val backgroundScale = 0.8f // 80% заполнения

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