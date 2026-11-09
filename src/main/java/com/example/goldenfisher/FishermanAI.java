package com.example.goldenfisher;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Простой автомат состояний: GO_TO_WATER -> FISHING -> GO_TO_BARREL -> (повтор).
 * Состояние хранится в памяти (после перезапуска сервера начнёт цикл заново - это ок для v1).
 */
public class FishermanAI {
    enum State { GO_TO_WATER, FISHING, GO_TO_BARREL }
    static class Data { State state = State.GO_TO_WATER; BlockPos water; int timer; }

    private static final Map<UUID, Data> DATA = new HashMap<>();
    static final int SEARCH_RADIUS = 16;
    static final int FISH_TIME_MIN = 200, FISH_TIME_MAX = 600; // тики (10-30 сек)

    // ---- Что можно выловить: сюда легко дописывать новое (предмет, вес) ----
    record Loot(ItemStack stack, int weight) {}
    static List<Loot> lootTable() {
        return List.of(
            new Loot(new ItemStack(Items.COD), 60),
            new Loot(new ItemStack(Items.SALMON), 25),
            new Loot(new ItemStack(Items.TROPICAL_FISH), 2),
            new Loot(new ItemStack(Items.PUFFERFISH), 13)
        );
    }

    public static void tickWorld(ServerLevel level) {
        if (level.getGameTime() % 5 != 0) return; // не каждый тик - экономим TPS
        for (Villager v : level.getEntities(EntityTypeTest.forClass(Villager.class), ModProfessions::is)) tick(level, v);
        }

    private static void tick(ServerLevel level, Villager v) {
        if (v.getInventory().getItem(RodInteraction.ROD_SLOT).isEmpty()) { DATA.remove(v.getUUID()); return; }
        Optional<GlobalPos> job = v.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        if (job.isEmpty() || !job.get().dimension().equals(level.dimension())) return;
        BlockPos barrel = job.get().pos();

        Data d = DATA.computeIfAbsent(v.getUUID(), k -> new Data());
        switch (d.state) {
            case GO_TO_WATER -> {
                if (d.water == null) d.water = findWater(level, barrel);
                if (d.water == null) return; // воды рядом нет
                if (v.blockPosition().distSqr(d.water) <= 6) {
                    d.state = State.FISHING;
                    d.timer = FISH_TIME_MIN + v.getRandom().nextInt(FISH_TIME_MAX - FISH_TIME_MIN);
                } else v.getNavigation().moveTo(d.water.getX() + .5, d.water.getY(), d.water.getZ() + .5, 0.6);
            }
            case FISHING -> {
                v.getNavigation().stop();
                v.getLookControl().setLookAt(Vec3.atCenterOf(d.water.below()));
                d.timer -= 5;
                if (d.timer <= 0) {
                    v.getInventory().setItem(RodInteraction.CATCH_SLOT, roll(v));
                    d.state = State.GO_TO_BARREL;
                }
            }
            case GO_TO_BARREL -> {
                if (v.blockPosition().distSqr(barrel) <= 6) {
                    deliver(level, v, barrel);
                    d.state = State.GO_TO_WATER;
                } else v.getNavigation().moveTo(barrel.getX() + .5, barrel.getY(), barrel.getZ() + .5, 0.6);
            }
        }
    }

    private static ItemStack roll(Villager v) {
        List<Loot> t = lootTable();
        int total = t.stream().mapToInt(Loot::weight).sum();
        int r = v.getRandom().nextInt(total);
        for (Loot l : t) { r -= l.weight(); if (r < 0) return l.stack().copy(); }
        return ItemStack.EMPTY;
    }

    private static void deliver(ServerLevel level, Villager v, BlockPos barrel) {
        ItemStack catchStack = v.getInventory().getItem(RodInteraction.CATCH_SLOT);
        if (catchStack.isEmpty()) return;
        if (level.getBlockEntity(barrel) instanceof BarrelBlockEntity be) {
            for (int i = 0; i < be.getContainerSize() && !catchStack.isEmpty(); i++) {
                ItemStack s = be.getItem(i);
                if (s.isEmpty()) { be.setItem(i, catchStack.copy()); catchStack.setCount(0); }
                else if (ItemStack.isSameItemSameComponents(s, catchStack) && s.getCount() < s.getMaxStackSize()) {
                    s.grow(1); catchStack.shrink(1);
                }
            }
            be.setChanged();
        }
        if (catchStack.isEmpty()) v.getInventory().setItem(RodInteraction.CATCH_SLOT, ItemStack.EMPTY);
        // если бочка полная - рыба останется в слоте 1, житель попробует снова
    }

    /** Ищет ближайший берег: твёрдый блок рядом с водой, куда можно встать. */
    private static BlockPos findWater(ServerLevel level, BlockPos center) {
        BlockPos best = null; double bestDist = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-SEARCH_RADIUS, -4, -SEARCH_RADIUS),
                                                 center.offset(SEARCH_RADIUS, 4, SEARCH_RADIUS))) {
            if (!level.getFluidState(p).is(FluidTags.WATER)) continue;
            for (var dir : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                BlockPos shore = p.relative(dir);
                if (level.getBlockState(shore).isAir() && level.getBlockState(shore.below()).isSolid()) {
                    double dist = shore.distSqr(center);
                    if (dist < bestDist) { bestDist = dist; best = shore.immutable(); }
                }
            }
        }
        return best;
    }
}
