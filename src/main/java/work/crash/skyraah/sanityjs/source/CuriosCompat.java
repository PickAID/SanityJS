package work.crash.skyraah.sanityjs.source;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

final class CuriosCompat {
    private CuriosCompat() {
    }

    static void collectMatches(ServerPlayer player, ResourceLocation dimensionId, List<SanityEquippedSourceRegistry.Match> matches) {
        CuriosApi.getCuriosInventory(player).resolve().ifPresent(handler ->
                handler.getCurios().forEach((identifier, stacksHandler) -> {
                    var stacks = stacksHandler.getStacks();

                    for (int index = 0; index < stacks.getSlots(); index++) {
                        ItemStack stack = stacks.getStackInSlot(index);
                        if (stack.isEmpty()) {
                            continue;
                        }

                        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                        SanityEquippedSourceDefinition definition = SanityEquippedSourceRegistry.get(dimensionId, itemId);
                        String slotId = SanityEquippedSourceRegistry.toCurioSlotId(identifier);

                        if (definition == null || !definition.matchesSlot(slotId)) {
                            continue;
                        }

                        int slotIndex = index;
                        matches.add(new SanityEquippedSourceRegistry.Match(slotId, stack, itemId, definition,
                                amount -> damageCurio(player, identifier, slotIndex, stacksHandler.isVisible(), stack, amount)));
                    }
                }));
    }

    private static void damageCurio(ServerPlayer player, String identifier, int index, boolean visible, ItemStack stack, int amount) {
        if (amount <= 0 || stack.isEmpty() || !stack.isDamageableItem()) {
            return;
        }

        SlotContext context = new SlotContext(identifier, player, index, false, visible);
        stack.hurtAndBreak(amount, player, livingEntity -> CuriosApi.broadcastCurioBreakEvent(context));
    }
}
