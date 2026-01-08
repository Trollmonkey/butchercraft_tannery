package com.trollmonkey.butchercraft_tannery.integration.jei;

import com.trollmonkey.butchercraft_tannery.ButchercraftTannery;
import com.trollmonkey.butchercraft_tannery.recipe.ScrapingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ScrapingCategory implements IRecipeCategory<ScrapingRecipe> {

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ButchercraftTannery.MODID, "scraping");

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;
    @SuppressWarnings("removal")
    @Override
    public IDrawable getBackground() {
        return background;
    }
    public ScrapingCategory(IGuiHelper gui) {
        this.background = gui.createBlankDrawable(120, 40);
        this.icon = gui.createDrawableItemStack(new ItemStack(ButchercraftTannery.TANNING_RACK_ITEM.get()));
        this.title = Component.literal("Tannery: Scraping");
    }

    @Override public mezz.jei.api.recipe.RecipeType<ScrapingRecipe> getRecipeType() {
        return ButchercraftTanneryJeiPlugin.SCRAPING;
    }

    @Override public Component getTitle() { return title; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void draw(ScrapingRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     net.minecraft.client.gui.GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        background.draw(guiGraphics);
    }
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ScrapingRecipe recipe, IFocusGroup focuses) {
        // Input item (slot 0)
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 10)
                .addIngredients(recipe.input());

        // Tool (slot 1)
        builder.addSlot(RecipeIngredientRole.CATALYST, 46, 10)
                .addIngredients(recipe.tool());

        // Output
        builder.addSlot(RecipeIngredientRole.OUTPUT, 92, 10)
                .addItemStack(recipe.getResultItem(net.minecraft.client.Minecraft.getInstance().level.registryAccess()));
    }
}