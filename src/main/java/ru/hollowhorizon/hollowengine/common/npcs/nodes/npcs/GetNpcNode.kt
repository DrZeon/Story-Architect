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

package ru.hollowhorizon.hollowengine.common.npcs.nodes.npcs

import imgui.type.ImInt
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import ru.hollowhorizon.hc.client.utils.isLogicalServer
import ru.hollowhorizon.hollowengine.storyarchitect.Companion.MODID
import ru.hollowhorizon.hollowengine.client.gui.comboNode
import ru.hollowhorizon.hollowengine.client.translate
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity
import ru.hollowhorizon.hollowengine.common.npcs.CURRENT_GRAPH
import ru.hollowhorizon.hollowengine.common.npcs.ScriptGraph
import ru.hollowhorizon.hollowengine.common.npcs.nodes.ScriptNode
import ru.hollowhorizon.hollowengine.common.npcs.nodes.inPin
import ru.hollowhorizon.hollowengine.common.npcs.nodes.outPin
import ru.hollowhorizon.hollowengine.common.npcs.nodes.pins.FloatPin
import ru.hollowhorizon.hollowengine.common.npcs.nodes.pins.NpcPin
import ru.hollowhorizon.hollowengine.common.npcs.nodes.pins.StringPin
import ru.hollowhorizon.hollowengine.common.npcs.nodes.pins.Vec3Pin
import java.util.*

class GetNpcNode : ScriptNode() {
    var uuid = UUID.randomUUID()
    val npcPin = outPin<NPCEntity, NpcPin>().apply {
        updater = { Minecraft.getInstance().level!!.entitiesForRendering().first { it.uuid == uuid } as NPCEntity }
    }

    override fun draw(graph: ScriptGraph) {
        val npcs = Minecraft.getInstance().level?.entitiesForRendering()?.filterIsInstance<NPCEntity>() ?: return

        val index = npcs.indexOfFirst { it.uuid == uuid }
        val id = ImInt(if (index != -1) index else 0)

        comboNode("pins.$MODID.npc".translate, npcs.map { it.displayName.string }.toTypedArray(), id)
        uuid = npcs[id.get()].uuid
    }

    override fun serialize(tag: CompoundTag) {
        super.serialize(tag)
        tag.putUUID("npc", uuid)
    }

    override fun deserialize(tag: CompoundTag) {
        super.deserialize(tag)
        uuid = tag.getUUID("npc")
        if (isLogicalServer) {
            val level = CURRENT_GRAPH.npc.level as ServerLevel
            npcPin.updater = { level.getEntity(uuid) as NPCEntity }
        }
    }
}

class NpcInfoNode : ScriptNode() {
    val npc by inPin<NPCEntity, NpcPin>()
    val position by outPin<Vec3, Vec3Pin>().apply {
        name = "pins.$MODID.vec3".translate
        updater = { npc.position() }
    }
    val positionHead by outPin<Vec3, Vec3Pin>().apply {
        name = "pins.$MODID.eye".translate
        updater = { npc.eyePosition }
    }
    val name by outPin<String, StringPin>().apply {
        name = "pins.$MODID.name"
        updater = { npc.name.string }
    }
    val health by outPin<Float, FloatPin>().apply {
        name = "pins.$MODID.health".translate
        updater = { npc.health }
    }
}