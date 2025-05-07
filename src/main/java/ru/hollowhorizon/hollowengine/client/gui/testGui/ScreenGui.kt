package ru.hollowhorizon.hollowengine.client.gui.testGui

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth

class ScreenGui : Screen(Component.literal("Story Architect GUI")) {
    private enum class GuiState { MAIN, CHARACTERS, SETTINGS }
    private var currentState = GuiState.MAIN
    private var transitionProgress = 0f


    private val backgroundTexture = ResourceLocation("storyarchitect", "textures/gui/background.png")
    private val toggleButtonTexture = ResourceLocation("storyarchitect", "textures/gui/dialogues/choice_button.png")

    override fun init() {
        super.init()

        this.addRenderableWidget(
            ImageButton(
                width / 2 - 8, 150,
                16, 16,
                0, 0,
                16,
                toggleButtonTexture,
                16, 32
            ) { _ ->
                currentState = when(currentState) {
                    GuiState.MAIN -> GuiState.CHARACTERS
                    GuiState.CHARACTERS -> GuiState.SETTINGS
                    GuiState.SETTINGS -> GuiState.MAIN
                }
                transitionProgress = 0f
                rebuildWidgets()
            }
        )

        rebuildWidgets()
    }

    override fun rebuildWidgets() {
        val mainButton = this.children().filterIsInstance<ImageButton>().firstOrNull()
        clearWidgets()
        mainButton?.let { addWidget(it) }

        when(currentState) {
            GuiState.MAIN -> initMainScreen()
            GuiState.CHARACTERS -> initCharactersScreen()
            GuiState.SETTINGS -> initSettingsScreen()
        }
    }

    private fun initMainScreen() {
        val characterIcon = ResourceLocation("storyarchitect", "textures/gui/dialogues/choice_button.png")

        this.addRenderableWidget(
            TexturedButton(
                width / 2 - 100, 100,
                200, 20,
                0, 0, 20,
                characterIcon,
                200, 40,
                "Персонажи"
            ) { _ ->
                currentState = GuiState.CHARACTERS
                transitionProgress = 0f
                rebuildWidgets()
            }
        )
        this.addRenderableWidget(
            TexturedButton(
                width / 2 - 100, 140,
                200, 20,
                0, 0, 20,
                characterIcon,
                200, 40,
                "Настройки"
            ) { _ ->
                currentState = GuiState.SETTINGS
                transitionProgress = 0f
                rebuildWidgets()
            }
        )
    }

    private fun initCharactersScreen() {
        val Texture = ResourceLocation("storyarchitect", "textures/gui/icons/recording.png")

        this.addRenderableWidget(
            ImageButton(
                10, 10,
                16, 16,
                0, 0, 16,
                Texture,
                16, 16
            ) { _ ->
                currentState = GuiState.MAIN
                rebuildWidgets()
            }
        )
    }

    private fun initSettingsScreen() {
        val Texture = ResourceLocation("storyarchitect", "textures/gui/icons/recording.png")

        this.addRenderableWidget(
            ImageButton(
                10, 10,
                16, 16,
                0, 0, 16,
                Texture,
                16, 16
            ) { _ ->
                currentState = GuiState.MAIN
                rebuildWidgets()
            }
        )
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float) {

        renderBackground(poseStack)
        RenderSystem.setShaderTexture(0, backgroundTexture)
        blit(poseStack, 0, 0, width, height, 0f, 0f, width, height, width, height)

        this.transitionProgress = Mth.lerp(partialTicks * 5f, transitionProgress, 1f)

        poseStack.pushPose()
        poseStack.translate(0.0, (1 - transitionProgress) * height * 0.1, 0.0)

        when(currentState) {
            GuiState.MAIN -> renderMainScreen(poseStack)
            GuiState.CHARACTERS -> renderCharactersScreen(poseStack)
            GuiState.SETTINGS -> renderSettingsScreen(poseStack)
        }

        poseStack.popPose()
        super.render(poseStack, mouseX, mouseY, partialTicks)
    }

    private fun renderMainScreen(poseStack: PoseStack) {
        drawCenteredString(poseStack, font, "Главное меню", width / 2, 80, 0xFFFFFF)
    }

    private fun renderCharactersScreen(poseStack: PoseStack) {
        drawCenteredString(poseStack, font, "Персонажи", width / 2, 80, 0x55FF55)
    }

    private fun renderSettingsScreen(poseStack: PoseStack) {
        drawCenteredString(poseStack, font, "Настройки", width / 2, 80, 0xFFAA00)
    }
}