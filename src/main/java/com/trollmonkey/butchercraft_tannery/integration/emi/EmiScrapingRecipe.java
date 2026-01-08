package com.trollmonkey.butchercraft_tannery.integration.emi;

import com.trollmonkey.butchercraft_tannery.recipe.ScrapingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class EmiScrapingRecipe implements EmiRecipe {

    private final ScrapingRecipe recipe;

    public EmiScrapingRecipe(ScrapingRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return ButchercraftTanneryEmiPlugin.SCRAPING_CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return null; // optional
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(
                EmiIngredient.of(recipe.input()),
                EmiIngredient.of(recipe.tool())
        );
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(recipe.getResultItem(Minecraft.getInstance().level.registryAccess())));
    }

    @Override
    public int getDisplayWidth() {
        return 120;
    }

    @Override
    public int getDisplayHeight() {
        return 40;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiIngredient.of(recipe.input()), 10, 10);
        widgets.addSlot(EmiIngredient.of(recipe.tool()), 46, 10);
        widgets.addSlot(getOutputs().get(0), 92, 10).recipeContext(this);
    }
}