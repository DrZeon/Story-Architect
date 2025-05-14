package ru.hollowhorizon.hollowengine.client.gui.widget

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.resources.ResourceLocation

class TexturedButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    texX: Int,
    texY: Int,
    hoverOffset: Int,
    resource: ResourceLocation,
    textureWidth: Int,
    textureHeight: Int,
    private val buttonText: String,
    private val textColor: Int = 0xFFFFFF,
    onPress: Button.OnPress
) : ImageButton(x, y, width, height, texX, texY, hoverOffset, resource, textureWidth, textureHeight, onPress) {

    private val font: Font get() = Minecraft.getInstance().font

    override fun renderButton(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.renderButton(poseStack, mouseX, mouseY, partialTicks)

        drawCenteredString(
            poseStack,
            font,
            buttonText,
            x + width / 2,
            y + (height - 8) / 2,
            textColor
        )
    }
}