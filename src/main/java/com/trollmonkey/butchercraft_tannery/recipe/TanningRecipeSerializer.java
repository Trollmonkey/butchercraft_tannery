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
public class TanningRecipeSerializer implements RecipeSerializer<TanningRecipe> {

    public static final MapCodec<TanningRecipe> CODEC =
            RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Ingredient.CODEC.fieldOf("input").forGetter(TanningRecipe::input),
                    StackResult.CODEC.fieldOf("result")
                            .forGetter(r -> new StackResult(r.result().getItem(), r.result().getCount())),
                    ExtraCodecs.POSITIVE_INT.fieldOf("time").forGetter(TanningRecipe::time)
            ).apply(inst, (input, result, time) ->
                    new TanningRecipe(input, result.toStack(), time)
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, TanningRecipe> STREAM_CODEC =
            StreamCodec.of(TanningRecipeSerializer::toNetwork, TanningRecipeSerializer::fromNetwork);

    @Override
    public MapCodec<TanningRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, TanningRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static TanningRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
        int time = buf.readVarInt();
        return new TanningRecipe(input, result, time);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, TanningRecipe recipe) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input());
        ItemStack.STREAM_CODEC.encode(buf, recipe.result());
        buf.writeVarInt(recipe.time());
    }
}