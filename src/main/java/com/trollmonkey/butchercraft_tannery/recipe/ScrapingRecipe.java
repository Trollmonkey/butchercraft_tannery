package com.trollmonkey.butchercraft_tannery.recipe;

import com.trollmonkey.butchercraft_tannery.registry.ModRecipeSerializers;
import com.trollmonkey.butchercraft_tannery.registry.ModRecipeTypes;
import com.trollmonkey.butchercraft_tannery.recipe.input.ScrapingInput;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ScrapingRecipe implements Recipe<ScrapingInput> {

    private final Ingredient input;
    private final Ingredient tool;
    private final ItemStack result;
    private final int toolDamage;

    public ScrapingRecipe(Ingredient input, Ingredient tool, ItemStack result, int toolDamage) {
        this.input = input;
        this.tool = tool;
        this.result = result;
        this.toolDamage = toolDamage;
    }

    public Ingredient input() { return input; }
    public Ingredient tool() { return tool; }
    public ItemStack result() { return result; }
    public int toolDamage() { return toolDamage; }

    @Override
    public boolean matches(ScrapingInput inv, Level level) {
        if (level.isClientSide) return false; // optional, but keeps log spam down
        return input.test(inv.target()) && tool.test(inv.tool());
    }

    @Override
    public ItemStack assemble(ScrapingInput inv, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true; // not a crafting grid recipe
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SCRAPING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.SCRAPING.get();
    }

    @Override
    public boolean isSpecial() {
        return true; // prevents recipe book clutter
    }
}
