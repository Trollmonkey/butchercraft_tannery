package com.trollmonkey.butchercraft_tannery.integration.emi;

import com.trollmonkey.butchercraft_tannery.recipe.TanningRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class EmiTanningRecipe implements EmiRecipe {

    private final TanningRecipe recipe;

    public EmiTanningRecipe(TanningRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return ButchercraftTanneryEmiPlugin.TANNING_CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return null; // optional
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(EmiIngredient.of(recipe.input()));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(recipe.getResultItem(Minecraft.getInstance().level.registryAccess())));
    }

    @Override
    public int getDisplayWidth() {
        return 140;
    }

    // Give enough vertical room for wrapped text
    @Override
    public int getDisplayHeight() {
        return 94;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiIngredient.of(recipe.input()), 18, 10);
        widgets.addSlot(getOutputs().get(0), 86, 10).recipeContext(this);

        var font = Minecraft.getInstance().font;

        double seconds = recipe.time() / 20.0;
        String timeStr = (Math.floor(seconds) == seconds)
                ? String.format("%.0fs", seconds)
                : String.format("%.1fs", seconds);

        String[] baseLines = new String[]{
                "Time: " + timeStr,
                "Needs: Sunshine",
                "Heat: Campfire beneath",
                "Warning: Foul weather may ruin hides"
        };

        int panelX = -1, panelY = 34, panelW = 140;
        int textX = panelX + 4, textY = panelY + 4;
        int maxTextWidth = panelW - 8;
        int lineHeight = 9;

        // Wrap all lines into real strings (no FormattedCharSequence -> no lambda garbage)
        List<String> wrappedLines = new ArrayList<>();
        for (String line : baseLines) {
            wrappedLines.addAll(wrapLine(font, line, maxTextWidth));
        }

        // Panel height based on wrapped lines, with padding
        int panelH = (wrappedLines.size() * lineHeight) + 8;

        widgets.addDrawable(panelX, panelY, panelW, panelH, (draw, mouseX, mouseY, delta) -> {
            draw.fill(0, 0, panelW, panelH, 0x66000000);
        });

        int y = textY;
        for (String wl : wrappedLines) {
            widgets.addText(Component.literal(wl), textX, y, 0xFFFFFFFF, true);
            y += lineHeight;
        }
    }

    /* Wrap a string into multiple lines that each fit maxWidth pixels.
     * Uses Font.plainSubstrByWidth to get the largest prefix that fits,
     * then continues with the remaining text. */
    private static List<String> wrapLine(Font font, String text, int maxWidth) {
        List<String> out = new ArrayList<>();
        String remaining = text;

        while (!remaining.isEmpty()) {
            String fit = font.plainSubstrByWidth(remaining, maxWidth);

            // Safety: if somehow nothing fits, avoid infinite loop
            if (fit.isEmpty()) {
                out.add(remaining);
                break;
            }

            out.add(fit);

            remaining = remaining.substring(fit.length()).stripLeading();
        }

        return out;
    }
}
