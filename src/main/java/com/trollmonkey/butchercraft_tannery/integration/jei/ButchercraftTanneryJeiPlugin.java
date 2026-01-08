package com.trollmonkey.butchercraft_tannery.integration.jei;

import com.trollmonkey.butchercraft_tannery.ButchercraftTannery;
import com.trollmonkey.butchercraft_tannery.recipe.ScrapingRecipe;
import com.trollmonkey.butchercraft_tannery.recipe.TanningRecipe;
import com.trollmonkey.butchercraft_tannery.registry.ModRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class ButchercraftTanneryJeiPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_UID =
            ResourceLocation.fromNamespaceAndPath(ButchercraftTannery.MODID, "jei_plugin");

    public static final RecipeType<ScrapingRecipe> SCRAPING =
            RecipeType.create(ButchercraftTannery.MODID, "scraping", ScrapingRecipe.class);

    public static final RecipeType<TanningRecipe> TANNING =
            RecipeType.create(ButchercraftTannery.MODID, "tanning", TanningRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        ButchercraftTannery.LOGGER.info("[JEI] registerCategories called");
        var gui = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(
                new ScrapingCategory(gui),
                new TanningCategory(gui)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var mc = Minecraft.getInstance();

        // Prefer world recipe manager, fallback to connection recipe manager
        var rm = (mc.level != null)
                ? mc.level.getRecipeManager()
                : (mc.getConnection() != null ? mc.getConnection().getRecipeManager() : null);

        if (rm == null) {
            ButchercraftTannery.LOGGER.warn("[JEI] No RecipeManager available yet; skipping recipe registration");
            return;
        }

        var scraping = rm.getAllRecipesFor(ModRecipeTypes.scraping())
                .stream().map(h -> h.value()).toList();

        var tanning = rm.getAllRecipesFor(ModRecipeTypes.tanning())
                .stream().map(h -> h.value()).toList();

        ButchercraftTannery.LOGGER.info("[JEI] Loaded recipes: scraping={}, tanning={}", scraping.size(), tanning.size());

        registration.addRecipes(SCRAPING, scraping);
        registration.addRecipes(TANNING, tanning);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        ItemStack rack = new ItemStack(ButchercraftTannery.TANNING_RACK_ITEM.get());

        registration.addRecipeCatalyst(rack, SCRAPING);
        registration.addRecipeCatalyst(rack, TANNING);
    }
}