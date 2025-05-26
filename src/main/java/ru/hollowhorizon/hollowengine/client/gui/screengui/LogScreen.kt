package ru.hollowhorizon.hollowengine.client.gui.screengui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import ru.hollowhorizon.hollowengine.storyarchitect

class LogScreen(private val nextScreen: StoryScreen) : StoryScreen(Component.literal("Fake Logs")) {
    override val backgroundTexture = ResourceLocation(storyarchitect.MODID, "textures/gui/background.png")
    override val backgroundScale = 0.8f

    // Список фейковых логов
    private val logs = listOf(
        "[INFO] Инициализация системы...",
        "[DEBUG] Подключение к серверу...",
        "[ERROR] Ошибка проверки данных!",
        "[WARN] Память заполнена на 95%",
        "[SUCCESS] Все модули загружены"
    )

    // Время отображения каждого лога (в тиках)
    private val LOG_DURATION_TICKS = 5

    // Текущее количество тиков
    private var currentTick = 0

    // Индекс последнего отображённого лога
    private var displayedLogsCount = 0


    override fun tick() {
        super.tick()
        currentTick++

        if (displayedLogsCount < logs.size && currentTick % LOG_DURATION_TICKS == 0) {
            displayedLogsCount++
        }

        // Перейти к следующему экрану после всех логов
        if (displayedLogsCount >= logs.size && currentTick > LOG_DURATION_TICKS * logs.size) {
            minecraft?.setScreen(nextScreen)
        }
    }

    override fun renderScreenContent(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val bgArea = getBackgroundArea()
        drawCenteredString(poseStack, font, "Загрузка...", width / 2, bgArea.y + 30, 0xFFFFFF)

        for (i in 0 until displayedLogsCount) {
            val alpha = Mth.clamp((currentTick - i * LOG_DURATION_TICKS) / 10f, 0f, 1f)
            val color = (alpha * 255).toInt() shl 24 or 0xAA00FF.toInt() // Фиолетовый цвет с прозрачностью
            font.drawShadow(poseStack, logs[i], (width / 3 - 100).toFloat(), (bgArea.y + 50 + i * 12).toFloat(), color)
        }
    }
}