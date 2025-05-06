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

package ru.hollowhorizon.hollowengine.common.registry

import net.minecraftforge.fml.ModList
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hollowengine.common.npcs.nodes.ScriptNode
import ru.hollowhorizon.hollowengine.common.npcs.nodes.base.*
import ru.hollowhorizon.hollowengine.common.npcs.nodes.dialogues.ChoiceNode
import ru.hollowhorizon.hollowengine.common.npcs.nodes.dialogues.CloseDialogueNode
import ru.hollowhorizon.hollowengine.common.npcs.nodes.dialogues.DialogueSayNode
import ru.hollowhorizon.hollowengine.common.npcs.nodes.dialogues.OpenDialogueNode
import ru.hollowhorizon.hollowengine.common.npcs.nodes.npcs.*
import ru.hollowhorizon.hollowengine.common.npcs.nodes.server.players.NearestPlayerNode
import ru.hollowhorizon.hollowengine.common.npcs.nodes.server.players.PlayerByNickNode
import ru.hollowhorizon.hollowengine.common.npcs.nodes.server.players.PlayerInfoNode
import ru.hollowhorizon.hollowengine.common.npcs.nodes.server.players.StageCheckerNode
import ru.hollowhorizon.hollowengine.common.npcs.nodes.server.MessageNode

object NodesRegistry : EngineRegistry<ScriptNode>() {
    override fun init() {
        register("storyarchitect:general/start".rl, ::StartNode)
        register("storyarchitect:general/end".rl, ::EndNode)
        register("storyarchitect:general/condition".rl, ::IfNode)

        register("storyarchitect:npcs/move_to".rl, ::MoveToNode)
        register("storyarchitect:npcs/look_at".rl, ::LookAtNode)
        register("storyarchitect:npcs/say".rl, ::SayNode)
        register("storyarchitect:npcs/animation_start".rl, ::NpcStartAnimationNode)
        register("storyarchitect:npcs/animation_stop".rl, ::NpcStopAnimationNode)
        register("storyarchitect:npcs/interact".rl, ::NpcInteractNode)
        register("storyarchitect:npcs/throw_item".rl, ::NpcGiveItemNode)
        register("storyarchitect:npcs/suspend".rl, ::NpcSuspendScriptNode)

        register("storyarchitect:dialogues/start".rl, ::OpenDialogueNode)
        register("storyarchitect:dialogues/end".rl, ::CloseDialogueNode)
        register("storyarchitect:dialogues/say".rl, ::DialogueSayNode)
        register("storyarchitect:dialogues/choice".rl, ::ChoiceNode)

        register("storyarchitect:waiters/wait".rl, ::WaitNode)

        register("storyarchitect:server/message".rl, ::MessageNode)
        register("storyarchitect:server/command".rl, ::CommandNode)
        register("storyarchitect:server/player_by_nick".rl, ::PlayerByNickNode)
        register("storyarchitect:server/npc_by_uuid".rl, ::GetNpcNode)
        register("storyarchitect:server/nearest_player".rl, ::NearestPlayerNode)
        register("storyarchitect:server/player_info".rl, ::PlayerInfoNode)
        register("storyarchitect:server/npc_info".rl, ::NpcInfoNode)
        if (ModList.get().isLoaded("storyarchitect:server/player_stages")) register("hollowengine:server/stage_checker".rl, ::StageCheckerNode)
    }
}