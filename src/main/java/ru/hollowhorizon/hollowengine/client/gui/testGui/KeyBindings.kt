package ru.hollowhorizon.hollowengine.client.gui.testGui

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.client.settings.KeyModifier
import ru.hollowhorizon.hollowengine.common.util.Keybind

object KeyBindings {
    val OPEN_GUI_KEY = KeyMapping(
        "key.hollowengine.open_gui",  // Уникальный ID
        KeyConflictContext.IN_GAME,   // Контекст (только в игре)
        KeyModifier.NONE,             // Без модификаторов (Shift/Ctrl/Alt)
        InputConstants.Type.KEYSYM,   // Тип ввода (клавиатура)
        Keybind.G.code,               // Код клавиши из enum
        "key.categories.hollowengine" // Категория в настройках
    )
}