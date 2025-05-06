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

package ru.hollowhorizon.hollowengine.common.scripting.content

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeType
import net.minecraftforge.common.crafting.conditions.ICondition
import ru.hollowhorizon.hc.client.utils.isPhysicalClient
import ru.hollowhorizon.hc.client.utils.mcTranslate
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hollowengine.client.ClientEvents
import ru.hollowhorizon.hollowengine.common.recipes.RecipeHelper
import ru.hollowhorizon.hollowengine.common.recipes.RecipeReloadListener

open class ContentScriptBase(
    val recipes: MutableMap<RecipeType<*>, MutableMap<ResourceLocation, Recipe<*>>>,
    val byName: MutableMap<ResourceLocation, Recipe<*>>
) {

    init {
        RecipeHelper.currentScript = this
    }

    fun ItemStack.tooltip(text: String): ItemStack {
        if (isPhysicalClient) ClientEvents.addTooltip(this.item, text.mcTranslate)
        return this
    }

    fun removeById(location: String) {
        recipes.values.forEach { recipe ->
            recipe.remove(location.rl)
        }
        byName.remove(location.rl)
    }

    fun removeByOutput(output: ItemStack, type: RecipeType<*>, checkTag: Boolean = false) {
        val ids = arrayListOf<ResourceLocation>()
        recipes[type]?.forEach { recipe ->
            val value = recipe.value
            if ((value.resultItem.item == output.item) && (!checkTag || value.resultItem.tag == output.tag) && (value.type == type)) {
                ids += recipe.key
            }
        }
        recipes.values.forEach { recipe ->
            ids.forEach {
                recipe.remove(it)
            }
        }
        ids.forEach(byName::remove)
    }

    fun addFromJson(name: String, data: String) = addFromJson(name, JsonParser.parseString(data).asJsonObject)

    fun addFromJson(name: String, data: JsonObject) {
        removeById(name)
        val recipe = RecipeManager.fromJson(
            name.rl,
            data,
            RecipeReloadListener.resources?.conditionContext ?: ICondition.IContext.EMPTY
        )
        addRecipe(recipe)
    }

    fun addRecipe(recipe: Recipe<*>) {
        recipes.computeIfAbsent(recipe.type) { hashMapOf() }[recipe.id] = recipe
        byName[recipe.id] = recipe
    }
}