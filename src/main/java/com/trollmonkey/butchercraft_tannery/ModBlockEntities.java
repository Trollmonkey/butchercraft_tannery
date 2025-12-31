package com.trollmonkey.butchercraft_tannery;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ButchercraftTannery.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TanningRackBlockEntity>> TANNING_RACK =
            BLOCK_ENTITIES.register(
                    "tanning_rack",
                    () -> {
                        /* Datafixer type: we don't use DFU, null is the correct value here. */
                        // noinspection ConstantConditions
                        return BlockEntityType.Builder
                                .of(TanningRackBlockEntity::new, ButchercraftTannery.TANNING_RACK.get())
                                .build(null);
                    }
            );
}