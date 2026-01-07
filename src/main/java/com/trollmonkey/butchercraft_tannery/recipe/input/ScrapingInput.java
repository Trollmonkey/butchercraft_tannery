package com.trollmonkey.butchercraft_tannery.recipe.input;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.MethodsReturnNonnullByDefault;

public record ScrapingInput(ItemStack target, ItemStack tool) implements RecipeInput {

    @Override
    @MethodsReturnNonnullByDefault
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case 0 -> target;
            case 1 -> tool;
            default -> throw new IndexOutOfBoundsException("Slot: " + slot);
        };
    }

    @Override
    public int size() {
        return 2;
    }

    public static ScrapingInput of(ItemStack target, ItemStack tool) {
        return new ScrapingInput(target, tool);
    }
}