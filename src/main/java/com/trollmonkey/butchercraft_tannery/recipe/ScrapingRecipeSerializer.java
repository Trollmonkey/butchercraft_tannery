package com.trollmonkey.butchercraft_tannery.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.trollmonkey.butchercraft_tannery.recipe.codec.StackResult;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

@MethodsReturnNonnullByDefault
public class ScrapingRecipeSerializer implements RecipeSerializer<ScrapingRecipe> {

    // JSON <-> Recipe
    public static final MapCodec<ScrapingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("input").forGetter(ScrapingRecipe::input),
            Ingredient.CODEC.fieldOf("tool").forGetter(ScrapingRecipe::tool),
            StackResult.CODEC.fieldOf("result").forGetter(r ->
                    new StackResult(r.result().getItem(), r.result().getCount())
            ),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("tool_damage", 1).forGetter(ScrapingRecipe::toolDamage)
    ).apply(inst, (in, tool, res, dmg) -> new ScrapingRecipe(in, tool, res.toStack(), dmg)));

    // Network sync (server -> client)
    public static final StreamCodec<RegistryFriendlyByteBuf, ScrapingRecipe> STREAM_CODEC =
            StreamCodec.of(ScrapingRecipeSerializer::toNetwork, ScrapingRecipeSerializer::fromNetwork);

    @Override
    public MapCodec<ScrapingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ScrapingRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static ScrapingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        Ingredient tool = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
        int dmg = buf.readVarInt();
        return new ScrapingRecipe(input, tool, result, dmg);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, ScrapingRecipe recipe) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input());
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.tool());
        ItemStack.STREAM_CODEC.encode(buf, recipe.result());
        buf.writeVarInt(recipe.toolDamage());
    }
}