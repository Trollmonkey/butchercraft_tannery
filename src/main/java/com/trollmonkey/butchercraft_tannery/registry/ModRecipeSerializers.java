package com.trollmonkey.butchercraft_tannery.registry;

import com.trollmonkey.butchercraft_tannery.ButchercraftTannery;
import com.trollmonkey.butchercraft_tannery.recipe.ScrapingRecipeSerializer;
import com.trollmonkey.butchercraft_tannery.recipe.TanningRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
    private ModRecipeSerializers() {}

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ButchercraftTannery.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SCRAPING =
            RECIPE_SERIALIZERS.register("scraping", ScrapingRecipeSerializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> TANNING =
            RECIPE_SERIALIZERS.register("tanning", TanningRecipeSerializer::new);
}
