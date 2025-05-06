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

package ru.hollowhorizon.hollowengine

import net.minecraft.network.chat.Component
import net.minecraft.server.packs.metadata.pack.PackMetadataSection
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AddPackFindersEvent
import net.minecraftforge.event.AddReloadListenerEvent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent
import net.minecraftforge.fml.loading.FMLEnvironment
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.common.registry.RegistryLoader
import ru.hollowhorizon.hollowengine.client.ClientEvents
import ru.hollowhorizon.hollowengine.client.ClientEvents.initKeys
import ru.hollowhorizon.hollowengine.client.camera.CameraHandler
import ru.hollowhorizon.hollowengine.client.camera.ScreenShakeHandler
import ru.hollowhorizon.hollowengine.client.shaders.ModShaders
import ru.hollowhorizon.hollowengine.common.commands.HECommands
import ru.hollowhorizon.hollowengine.common.commands.HEStoryCommands
import ru.hollowhorizon.hollowengine.common.compat.ftbquests.FTBQuestsSupport
import ru.hollowhorizon.hollowengine.common.data.HollowStoryPack
import ru.hollowhorizon.hollowengine.common.events.StoryEngineSetup
import ru.hollowhorizon.hollowengine.common.files.DirectoryManager
import ru.hollowhorizon.hollowengine.common.files.DirectoryManager.getModScripts
import ru.hollowhorizon.hollowengine.common.network.NetworkHandler
import ru.hollowhorizon.hollowengine.common.recipes.RecipeReloadListener
import ru.hollowhorizon.hollowengine.common.registry.NodesRegistry
import ru.hollowhorizon.hollowengine.common.registry.PinsRegistry
import ru.hollowhorizon.hollowengine.common.registry.worldgen.structures.ModStructurePieces
import ru.hollowhorizon.hollowengine.common.registry.worldgen.structures.ModStructureSets
import ru.hollowhorizon.hollowengine.common.registry.worldgen.structures.ModStructures
import ru.hollowhorizon.hollowengine.common.scripting.mod.runModScript

@Mod(storyarchitect.MODID)
class storyarchitect {
    init {
        MOD_BUS = thedarkcolour.kotlinforforge.forge.MOD_BUS
        getModScripts().forEach(::runModScript)

        if (ModList.get().isLoaded("ftbquests")) FTBQuestsSupport

        val forgeBus = MinecraftForge.EVENT_BUS
        HollowCore.LOGGER.info("HollowEngine mod loading...")
        forgeBus.addListener(::registerCommands)
        forgeBus.addListener(this::addReloadListenerEvent)
        MOD_BUS.addListener(::setup)
        MOD_BUS.addListener(::onLoadingComplete)
        forgeBus.addListener(NodesRegistry::onReload)
        forgeBus.addListener(PinsRegistry::onReload)
        if (FMLEnvironment.dist.isClient) {
            initKeys()
            forgeBus.register(ClientEvents)
            forgeBus.register(CameraHandler)
            forgeBus.register(ScreenShakeHandler)
            MOD_BUS.addListener(::clientInit)
            MOD_BUS.register(ModShaders)
        }

        StoryEngineSetup.init()


        MOD_BUS.addListener(this::registerPacks)

        ModStructures.addStructure("rustic_temple") {
        }

        ModStructures.STRUCTURES.register(MOD_BUS)
        ModStructures.STRUCTURE_TYPES.register(MOD_BUS)
        ModStructureSets.STRUCTURE_SETS.register(MOD_BUS)
        ModStructurePieces.STRUCTURE_PIECES.register(MOD_BUS)
        RegistryLoader.registerAll()
        //ModDimensions

        isLoading = false
    }

    fun registerPacks(event: AddPackFindersEvent) {
        event.addRepositorySource { adder, creator ->
            adder.accept(
                creator.create(
                    HollowStoryPack.name, HollowStoryPack.name.mcText, true, { HollowStoryPack },
                    PackMetadataSection(Component.translatable("fml.resources.modresources"), 9),
                    Pack.Position.TOP, PackSource.BUILT_IN, false
                )
            )
        }
    }

    private fun addReloadListenerEvent(event: AddReloadListenerEvent) {
        event.addListener(RecipeReloadListener)
        RecipeReloadListener.resources = event.serverResources
    }

    @OnlyIn(Dist.CLIENT)
    private fun clientInit(event: FMLClientSetupEvent) {

    }

    private fun setup(event: FMLCommonSetupEvent) {
        DirectoryManager.init()
        NetworkHandler.register()
    }

    private fun onLoadingComplete(event: FMLLoadCompleteEvent) {}

    private fun registerCommands(event: RegisterCommandsEvent) {
        HECommands.register(event.dispatcher)

        HEStoryCommands.register(event.dispatcher)
    }

    companion object {
        const val MODID = "storyarchitect"
        lateinit var MOD_BUS: IEventBus
        var isLoading = true
    }
}