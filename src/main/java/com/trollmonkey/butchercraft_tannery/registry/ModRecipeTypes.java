package com.trollmonkey.butchercraft_tannery.registry;

import com.trollmonkey.butchercraft_tannery.ButchercraftTannery;
import com.trollmonkey.butchercraft_tannery.recipe.ScrapingRecipe;
import com.trollmonkey.butchercraft_tannery.recipe.TanningRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeTypes {
    private ModRecipeTypes() {}

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, ButchercraftTannery.MODID);

    // Typed instances (IMPORTANT)
    public static final RecipeType<ScrapingRecipe> SCRAPING_TYPE = simple("scraping");
    public static final RecipeType<TanningRecipe>  TANNING_TYPE  = simple("tanning");

    // Registered holders
    public static final DeferredHolder<RecipeType<?>, RecipeType<?>> SCRAPING =
            RECIPE_TYPES.register("scraping", () -> SCRAPING_TYPE);

    public static final DeferredHolder<RecipeType<?>, RecipeType<?>> TANNING =
            RECIPE_TYPES.register("tanning", () -> TANNING_TYPE);

    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> RecipeType<T> simple(String id) {
        return new RecipeType<>() {
            @Override public String toString() {
                return ResourceLocation.fromNamespaceAndPath(ButchercraftTannery.MODID, id).toString();
            }
        };
    }

    // Convenience typed getters (use these everywhere)
    public static RecipeType<ScrapingRecipe> scraping() { return SCRAPING_TYPE; }
    public static RecipeType<TanningRecipe> tanning() { return TANNING_TYPE; }
}