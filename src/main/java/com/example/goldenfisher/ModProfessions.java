package com.example.goldenfisher;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

public class ModProfessions {
    public static final Identifier ID =
        Identifier.fromNamespaceAndPath(GoldenFisher.MOD_ID, "golden_fisherman");
    public static final ResourceKey<PoiType> POI_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ID);
    public static VillagerProfession GOLDEN_FISHERMAN;

    public static void register() {
        // POI = блок, который житель "занимает" (наша бочка)
        PoiType poi = Registry.register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, ID,
            new PoiType(ImmutableSet.copyOf(ModBlocks.GOLDEN_BARREL.getStateDefinition().getPossibleStates()), 1, 1));

        // Профессия без торговли: список обменов не задаём, ничего не продаётся.
        // Текстура (золотая шляпа): assets/goldenfisher/textures/entity/villager/profession/golden_fisherman.png
        GOLDEN_FISHERMAN = Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, ID,
            new VillagerProfession(
                net.minecraft.network.chat.Component.translatable("entity.goldenfisher.golden_fisherman"),
                holder -> holder.is(POI_KEY), holder -> holder.is(POI_KEY),
                ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_FISHERMAN,
                new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<>()));

    public static boolean is(net.minecraft.world.entity.npc.villager.Villager v) {
        return v.getVillagerData().profession().value() == GOLDEN_FISHERMAN;
    }
}
