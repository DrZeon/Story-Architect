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

package ru.hollowhorizon.hollowengine.client.screen

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import it.unimi.dsi.fastutil.ints.IntLists
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.vehicle.Minecart
import ru.hollowhorizon.hc.api.AutoScaled
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hc.client.screens.widget.HollowWidget
import ru.hollowhorizon.hc.client.screens.widget.layout.PlacementType
import ru.hollowhorizon.hc.client.screens.widget.layout.box
import ru.hollowhorizon.hc.client.utils.drawScaled
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.mcTranslate
import ru.hollowhorizon.hc.common.ui.Alignment
import ru.hollowhorizon.hc.common.ui.Anchor
import ru.hollowhorizon.hollowengine.storyarchitect
import ru.hollowhorizon.hollowengine.common.capabilities.PlayerStoryCapability

class ProgressManagerScreen : HollowScreen("Progress Manager".mcText), AutoScaled {
    override fun init() {
        super.init()

        val messages = Minecraft.getInstance().player?.get(PlayerStoryCapability::class)?.quests ?: return

        box {
            size = 100.pc x 100.pc

            elements {
                box {
                    size = 100.pc x 15.pc
                    align = Alignment.TOP_CENTER
                    renderer = { stack, x, y, w, h ->
                        bind(storyarchitect.MODID, "gui/event_list/event_list.png")
                        blit(stack, x, y + 5, 0f, 0f, w, h, w, h)

                        font.drawScaled(
                            stack, Anchor.CENTER,
                            "storyarchitect.progress_manager.event_list".mcTranslate,
                            x + w / 2,
                            y + h / 2 + 1 + 5,
                            0xFFFFFF,
                            2f
                        )
                    }
                }
                box {
                    size = 100.pc x 80.pc
                    align = Alignment.BOTTOM_CENTER
                    alignElements = Alignment.TOP_LEFT
                    spacing = 0.px x 5.pc
                    placementType = PlacementType.VERTICAL

                    renderer = { stack, x, y, w, h ->
                        if (messages.isEmpty()) font.drawScaled(
                            stack, Anchor.CENTER,
                            "storyarchitect.progress_manager.no_tasks".mcTranslate,
                            x + w / 2,
                            y + h / 2 ,
                            0xFFFFFF,
                            1.0f
                        )
                    }

                    elements {

                        messages.forEach { task ->
                            +TaskWidget(task, 100.pc.w().value, 15.pc.h().value)
                        }

                    }
                }
            }
        }
    }

    override fun render(pMatrixStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        renderBackground(pMatrixStack)
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks)
    }

    class TaskWidget(task: String, w: Int, h: Int) : HollowWidget(0, 0, w, h, task.mcText) {
        override fun init() {
            box {
                size = 100.pc x 100.pc

                renderer = { stack, x, y, w, h ->
                    RenderSystem.enableBlend()
                    bind(storyarchitect.MODID, "gui/event_list/event_list_value.png")
                    blit(stack, x, y, 0f, 0f, w, h, w, h)

                    val lines = font.split(message, w)
                    val textHeight = (lines.size * font.lineHeight) / 2
                    lines.forEachIndexed { index, text ->
                        font.drawShadow(
                            stack,
                            text,
                            x.toFloat() + 6.pc.w().value,
                            (y + height / 2 - textHeight + index * font.lineHeight).toFloat(),
                            0xFFFFFF
                        )
                    }
                }
            }
        }
    }
}