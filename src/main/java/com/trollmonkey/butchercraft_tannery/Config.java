package com.trollmonkey.butchercraft_tannery;

import net.neoforged.neoforge.common.ModConfigSpec;

/*
 * Configuration for Butchercraft Tannery.
 *
 * At present, this controls:
 *  - TANNING_TIME_TICKS: How long (in ticks) a scraped hide takes to tan
 *
 * Future options might include:
 *  - Whether rain can ruin hides
 *  - Tool durability per scrape
 *  - Smoke / sunlight requirement toggles
 */
public class Config {

    /* Base builder for config entries. */
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /* Time (in ticks) for a scraped hide to tan into leather.
       Default: 20 * 60 = 1200 ticks = 60 seconds. */
    public static final ModConfigSpec.IntValue TANNING_TIME_TICKS = BUILDER
            .comment("Time in ticks for a scraped hide on a valid tanning rack to become leather.",
                    "Default: 1200 ticks (60 seconds).")
            .defineInRange("tanningTimeTicks", 20 * 60, 1, Integer.MAX_VALUE);

    /* Final spec registered in ButchercraftTannery. */
    static final ModConfigSpec SPEC = BUILDER.build();
}