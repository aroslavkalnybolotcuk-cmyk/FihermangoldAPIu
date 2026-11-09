package com.example.goldenfisher;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/** ПКМ удочкой по рыбаку -> удочка кладётся в слот 0 его инвентаря. */
public class RodInteraction {
    public static final int ROD_SLOT = 0;
    public static final int CATCH_SLOT = 1;

    public static InteractionResult onUse(Player p, Level lvl, InteractionHand h, Entity e, EntityHitResult hit) {
        if (!(e instanceof Villager v) || !ModProfessions.is(v)) return InteractionResult.PASS;
        ItemStack held = p.getItemInHand(h);
        if (!held.is(Items.FISHING_ROD)) return InteractionResult.PASS; // иначе обычное поведение
        if (lvl.isClientSide()) return InteractionResult.SUCCESS;

        if (!v.getInventory().getItem(ROD_SLOT).isEmpty()) return InteractionResult.SUCCESS; // уже есть
        v.getInventory().setItem(ROD_SLOT, held.copyWithCount(1)); // сохраняем чары/прочность
        if (!p.isCreative()) held.shrink(1);
        return InteractionResult.SUCCESS;
    }
}
