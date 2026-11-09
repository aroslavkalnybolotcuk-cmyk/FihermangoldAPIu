package com.example.goldenfisher;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;      // если не компилируется: ResourceLocation
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    public static Block GOLDEN_BARREL;
    public static Item GOLDEN_BARREL_ITEM;

    public static void register() {
        Identifier id = Identifier.fromNamespaceAndPath(GoldenFisher.MOD_ID, "golden_barrel");

        // Копия обычной бочки: внутри работает ровно как бочка (27 слотов, воронки и т.д.)
        GOLDEN_BARREL = Registry.register(BuiltInRegistries.BLOCK, id,
            new BarrelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL)
                .setId(ResourceKey.create(Registries.BLOCK, id))));

        GOLDEN_BARREL_ITEM = Registry.register(BuiltInRegistries.ITEM, id,
            new BlockItem(GOLDEN_BARREL, new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id)).useBlockDescriptionPrefix()));

        // Разрешаем ванильному BlockEntity бочки жить в нашем блоке (Fabric API)
                BlockEntityType<?> barrelType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("barrel"));
        try {
            for (java.lang.reflect.Field f : BlockEntityType.class.getDeclaredFields()) {
                if (java.util.Set.class.isAssignableFrom(f.getType()) && !java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    java.util.Set<Block> old = (java.util.Set<Block>) f.get(barrelType);
                    java.util.Set<Block> merged = new java.util.HashSet<>(old);
                    merged.add(GOLDEN_BARREL);
                    f.set(barrelType, merged);
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Golden barrel: failed to register block entity", e);
        }
    }
}
