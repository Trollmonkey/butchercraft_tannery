package com.trollmonkey.butchercraft_tannery.integration.emi;

import com.trollmonkey.butchercraft_tannery.ButchercraftTannery;
import com.trollmonkey.butchercraft_tannery.registry.ModRecipeTypes;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

@EmiEntrypoint
public class ButchercraftTanneryEmiPlugin implements EmiPlugin {

    public static final EmiRecipeCategory SCRAPING_CATEGORY =
            new EmiRecipeCategory(
                    ResourceLocation.fromNamespaceAndPath(ButchercraftTannery.MODID, "scraping"),
                    EmiStack.of(ButchercraftTannery.TANNING_RACK_ITEM.get())
            );

    public static final EmiRecipeCategory TANNING_CATEGORY =
            new EmiRecipeCategory(
                    ResourceLocation.fromNamespaceAndPath(ButchercraftTannery.MODID, "tanning"),
                    EmiStack.of(ButchercraftTannery.TANNING_RACK_ITEM.get())
            );

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(SCRAPING_CATEGORY);
        registry.addCategory(TANNING_CATEGORY);

        // Workstation (catalyst)
        var rack = EmiStack.of(ButchercraftTannery.TANNING_RACK_ITEM.get());
        registry.addWorkstation(SCRAPING_CATEGORY, rack);
        registry.addWorkstation(TANNING_CATEGORY, rack);

        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var rm = level.getRecipeManager();

        for (var holder : rm.getAllRecipesFor(ModRecipeTypes.scraping())) {
            registry.addRecipe(new EmiScrapingRecipe(holder.value()));
        }

        for (var holder : rm.getAllRecipesFor(ModRecipeTypes.tanning())) {
            registry.addRecipe(new EmiTanningRecipe(holder.value()));
        }
    }
}