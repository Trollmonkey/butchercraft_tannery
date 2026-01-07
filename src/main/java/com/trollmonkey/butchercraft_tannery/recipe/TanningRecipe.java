package com.trollmonkey.butchercraft_tannery.recipe;

import com.trollmonkey.butchercraft_tannery.registry.ModRecipeSerializers;
import com.trollmonkey.butchercraft_tannery.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.MethodsReturnNonnullByDefault;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TanningRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final ItemStack result;
    private final int time;

    public TanningRecipe(Ingredient input, ItemStack result, int time) {
        this.input = input;
        this.result = result;
        this.time = time;
    }

    public Ingredient input() { return input; }
    public ItemStack result() { return result; }
    public int time() { return time; }

    @Override
    public boolean matches(SingleRecipeInput inv, Level level) {
        return input.test(inv.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput inv, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.TANNING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.tanning();
    }


    @Override
    public boolean isSpecial() {
        return true;
    }
}