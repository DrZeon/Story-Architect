package ru.hollowhorizon.hollowengine.client.gui.testGui

import net.minecraftforge.api.distmarker.Dist
import ru.hollowhorizon.hollowengine.client.gui.testGui.ScreenGui
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.InputEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = "storyarchitect", value = [Dist.CLIENT])
object KeyInputHandler {
    @SubscribeEvent
    fun onKeyInput(event: InputEvent.Key) {
        if (KeyBindings.OPEN_GUI_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(ScreenGui())
        }
    }
}