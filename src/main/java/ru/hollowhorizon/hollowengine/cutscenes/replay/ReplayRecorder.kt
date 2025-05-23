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

package ru.hollowhorizon.hollowengine.cutscenes.replay

import kotlinx.serialization.Serializable
import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.TickEvent.ServerTickEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.registries.ForgeRegistries
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationState
import ru.hollowhorizon.hc.client.models.gltf.animations.PlayMode
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimationLayer
import ru.hollowhorizon.hc.client.models.gltf.manager.LayerMode
import ru.hollowhorizon.hc.client.utils.nbt.save
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hollowengine.common.files.DirectoryManager

class ReplayRecorder(val player: Player) {
    val replay = Replay()
    var isRecording = false
    var animationFrame: RecordingContainer? = null
    val brokenBlocks: ArrayList<ReplayBlock> = ArrayList()
    val placedBlocks: ArrayList<ReplayBlock> = ArrayList()
    val usedBlocks: ArrayList<ReplayBlock> = ArrayList()
    var name = "replay"

    fun startRecording(name: String) {
        replay.clear()
        isRecording = true
        MinecraftForge.EVENT_BUS.register(this)
        this.name = name
    }

    fun stopRecording() {
        isRecording = false
        MinecraftForge.EVENT_BUS.unregister(this)
        val nbt = Replay.toNBT(replay)

        val file = DirectoryManager.HOLLOW_ENGINE.resolve("replays/$name.nbt")
        if (!file.exists()) {
            if (!file.parentFile.exists()) file.parentFile.mkdirs()
            file.createNewFile()
        }
        val outStream = file.outputStream()
        nbt.save(outStream)
    }

    fun recordTick() {
        replay.addPointFromPlayer(this, player, animationFrame)
        animationFrame = null
        brokenBlocks.clear()
        placedBlocks.clear()
        usedBlocks.clear()
    }

    @SubscribeEvent
    fun onPlayerTick(event: ServerTickEvent) {
        if (event.phase == TickEvent.Phase.END && isRecording) {
            recordTick()
        }
    }

    @SubscribeEvent
    fun onBlockPlaced(event: BlockEvent.BreakEvent) {
        if (isRecording) {
            brokenBlocks.add(ReplayBlock(event.pos, ForgeRegistries.BLOCKS.getKey(event.state.block)?.toString() ?: "minecraft:dirt"))
        }
    }

    @SubscribeEvent
    fun onBlockBroken(event: BlockEvent.EntityPlaceEvent) {
        if (isRecording) {
            placedBlocks.add(ReplayBlock(event.pos, ForgeRegistries.BLOCKS.getKey(event.state.block)?.toString() ?: "minecraft:dirt"))
        }
    }

    @SubscribeEvent
    fun onBlockUse(event: PlayerInteractEvent.RightClickBlock) {
        if (isRecording) {
            usedBlocks.add(ReplayBlock(event.pos, ForgeRegistries.ITEMS.getKey(event.itemStack.item)?.toString() ?: "minecraft:stick"))
        }
    }

    companion object {
        val recorders: HashMap<Player, ReplayRecorder> = HashMap()

        fun getRecorder(player: Player): ReplayRecorder {
            if (!recorders.containsKey(player)) {
                recorders[player] = ReplayRecorder(player)
            }
            return recorders[player]!!
        }
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class ToggleRecordingPacket(private val fileName: String) : HollowPacketV3<ToggleRecordingPacket> {
    override fun handle(player: Player, data: ToggleRecordingPacket) {
        if (!player.hasPermissions(2)) return

        val recorder = ReplayRecorder.getRecorder(player)

        if (fileName == "") recorder.stopRecording()
        else recorder.startRecording(fileName)
    }

}

@Serializable
data class RecordingContainer(
    val animation: String,
    val layerMode: LayerMode,
    val playMode: PlayMode,
    val speed: Float
)

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class PauseRecordingPacket(private val resume: Boolean, val animation: RecordingContainer?) : HollowPacketV3<PauseRecordingPacket> {
    override fun handle(player: Player, data: PauseRecordingPacket) {
        if (!player.hasPermissions(2)) return

        val recorder = ReplayRecorder.getRecorder(player)
        recorder.isRecording = resume
        recorder.animationFrame = animation
    }
}