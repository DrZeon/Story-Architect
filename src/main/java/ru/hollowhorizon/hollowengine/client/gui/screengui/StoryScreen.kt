package ru.hollowhorizon.hollowengine.client.gui.screengui

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth

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