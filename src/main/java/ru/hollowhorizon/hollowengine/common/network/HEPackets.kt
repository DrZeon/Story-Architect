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

package ru.hollowhorizon.hollowengine.common.network

import kotlinx.serialization.Serializable
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.network.NetworkDirection
import ru.hollowhorizon.hc.client.models.gltf.manager.GltfManager
import ru.hollowhorizon.hc.client.utils.mc
import ru.hollowhorizon.hc.client.utils.mcTranslate
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hollowengine.common.events.ServerKeyPressedEvent
import ru.hollowhorizon.hollowengine.common.util.Keybind

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class CopyTextPacket(val text: String) : HollowPacketV3<CopyTextPacket> {
    override fun handle(player: Player, data: CopyTextPacket) {
        player.sendSystemMessage(Component.translatable("hollowengine.commands.copy", Component.literal(data.text).apply {
            style = Style.EMPTY
                .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, "hollowengine.tooltips.copy".mcTranslate))
                .withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, data.text))
        }))
        mc.keyboardHandler.clipboard = data.text
    }

}

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class ShowModelInfoPacket(val model: String) : HollowPacketV3<ShowModelInfoPacket> {
    override fun handle(player: Player, data: ShowModelInfoPacket) {
        val location = data.model.rl

        GltfManager.getOrCreate(location).let { model ->
            player.sendSystemMessage(
                Component.translatable(
                    "hollowengine.commands.model_animations",
                    data.model.substringAfterLast('/')
                )
            )

            model.animationPlayer.nameToAnimationMap.keys.forEach { anim ->
                player.sendSystemMessage(Component.literal("- ").append(Component.literal(anim).apply {
                    style = Style.EMPTY
                        .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, "hollowengine.tooltips.copy".mcTranslate))
                        .withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, anim))
                }))
            }

            player.sendSystemMessage(
                Component.translatable(
                    "hollowengine.commands.model_textures",
                    data.model.substringAfterLast('/')
                )
            )

            model.modelTree.materials.map { it.texture.path.removeSuffix(".png") }.forEach { anim ->
                player.sendSystemMessage(Component.literal("- ").append(Component.literal(anim).apply {
                    style = Style.EMPTY
                        .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, "hollowengine.tooltips.copy".mcTranslate))
                        .withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, anim))
                }))
            }
        }
    }

}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class KeybindPacket(private val key: Keybind) : HollowPacketV3<KeybindPacket> {
    override fun handle(player: Player, data: KeybindPacket) {
        MinecraftForge.EVENT_BUS.post(ServerKeyPressedEvent(player, data.key))
    }
}