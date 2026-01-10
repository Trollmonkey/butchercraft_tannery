package com.trollmonkey.butchercraft_tannery;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue TANNING_TIME_MULTIPLIER;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("tanning");

        TANNING_TIME_MULTIPLIER = BUILDER
                .comment("Global multiplier for tanning time. 1.0 = 60 Seconds")
                .defineInRange("timeMultiplier", 1.0, 0.1, 10.0);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}