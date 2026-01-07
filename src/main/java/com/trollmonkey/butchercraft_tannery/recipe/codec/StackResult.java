package com.trollmonkey.butchercraft_tannery.recipe.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public record StackResult(Item id, int count) {
    public static final Codec<StackResult> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(StackResult::id),
            Codec.INT.optionalFieldOf("count", 1).forGetter(StackResult::count)
    ).apply(inst, StackResult::new));

    public ItemStack toStack() {
        return new ItemStack(id, Math.max(1, count));
    }
}