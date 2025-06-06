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

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraftforge.registries.RegistryObject
import ru.hollowhorizon.hc.common.registry.HollowRegistry
import ru.hollowhorizon.hc.common.registry.ObjectConfig
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity
import ru.hollowhorizon.hollowengine.common.entities.SeatEntity

object ModEntities : HollowRegistry() {
    val NPC_ENTITY: RegistryObject<EntityType<NPCEntity>> by register(
        ObjectConfig(
            name = "npc_entity",
        )
    ) {
        EntityType.Builder.of(
            ::NPCEntity,
            MobCategory.CREATURE
        ).sized(0.6f, 1.8f).build("npc_entity")
    }

    val SEAT: RegistryObject<EntityType<SeatEntity>> by register(
        ObjectConfig(
            name = "seat_entity"
        )
    ) {
        EntityType.Builder.of({ _, l -> SeatEntity(l) }, MobCategory.CREATURE)
            .sized(0.0F, 0.0F)
            .setCustomClientFactory { _, l -> SeatEntity(l) }
            .build("seat_entity")
    }
}
