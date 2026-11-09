package com.example.goldenfisher;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

/** Точка входа. Вся регистрация разнесена по классам - легко дописывать. */
public class GoldenFisher implements ModInitializer {
    public static final String MOD_ID = "goldenfisher";

    @Override
    public void onInitialize() {
        ModBlocks.register();          // золотая бочка
        ModProfessions.register();     // профессия
        UseEntityCallback.EVENT.register(RodInteraction::onUse);   // вставка удочки ПКМ
        ServerTickEvents.END_WORLD_TICK.register(FishermanAI::tickWorld); // поведение
    }
}
