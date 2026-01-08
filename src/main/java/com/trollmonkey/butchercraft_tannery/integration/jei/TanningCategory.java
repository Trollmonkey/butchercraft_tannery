package com.trollmonkey.butchercraft_tannery.integration.jei;

import com.trollmonkey.butchercraft_tannery.ButchercraftTannery;
import com.trollmonkey.butchercraft_tannery.recipe.TanningRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TanningCategory implements IRecipeCategory<TanningRecipe> {

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ButchercraftTannery.MODID, "tanning");

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public TanningCategory(IGuiHelper gui) {
        // Increased height to fit text
        this.background = gui.createBlankDrawable(120, 94);
        this.icon = gui.createDrawableItemStack(new ItemStack(ButchercraftTannery.TANNING_RACK_ITEM.get()));
        this.title = Component.literal("Tannery: Tanning");
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<TanningRecipe> getRecipeType() {
        return ButchercraftTanneryJeiPlugin.TANNING;
    }

    @Override public Component getTitle() { return title; }
    @Override public IDrawable getIcon() { return icon; }

    // Keep this to avoid JEI background-null issues
    @SuppressWarnings("removal")
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void draw(TanningRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     net.minecraft.client.gui.GuiGraphics g,
                     double mouseX, double mouseY) {

        var mc = Minecraft.getInstance();
        var font = mc.font;

        double seconds = recipe.time() / 20.0;
        String timeStr = (Math.floor(seconds) == seconds)
                ? String.format("%.0fs", seconds)
                : String.format("%.1fs", seconds);

        String[] baseLines = new String[] {
                "Time: " + timeStr,
                "Needs: Sunshine",
                "Heat: Campfire beneath",
                "Warning: Foul weather may ruin hides"
        };

        int panelX = -2, panelY = 34, panelW = 124;
        int textX = panelX + 4, textY = panelY + 4;
        int maxTextWidth = panelW - 8;
        int lineHeight = 9;

        // Wrap into real strings (same logic as EMI)
        java.util.List<String> wrappedLines = new java.util.ArrayList<>();
        for (String line : baseLines) {
            wrappedLines.addAll(wrapLine(font, line, maxTextWidth));
        }

        int panelH = (wrappedLines.size() * lineHeight) + 8;

        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0x66000000);

        int y = textY;
        for (String wl : wrappedLines) {
            g.drawString(font, wl, textX, y, 0xFFFFFFFF, true);
            y += lineHeight;
        }
    }

    // Same wrapper as EMI
    private static java.util.List<String> wrapLine(net.minecraft.client.gui.Font font, String text, int maxWidth) {
        java.util.List<String> out = new java.util.ArrayList<>();
        String remaining = text;

        while (!remaining.isEmpty()) {
            String fit = font.plainSubstrByWidth(remaining, maxWidth);
            if (fit.isEmpty()) {
                out.add(remaining);
                break;
            }
            out.add(fit);
            remaining = remaining.substring(fit.length()).stripLeading();
        }

        return out;
    }


    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TanningRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 18, 10)
                .addIngredients(recipe.input());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 10)
                .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
    }
}