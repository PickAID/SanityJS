package work.crash.skyraah.sanityjs.source;

import croissantnova.sanitydim.SanityProcessor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import work.crash.skyraah.sanityjs.event.SanityEquippedSourceEventJS;
import work.crash.skyraah.sanityjs.util.SanityHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

public final class SanityEquippedSourceRegistry {
    private static final Map<ResourceLocation, SanityEquippedSourceDefinition> GLOBAL_SOURCES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Map<ResourceLocation, SanityEquippedSourceDefinition>> DIMENSIONAL_SOURCES = new LinkedHashMap<>();

    public record Match(String slotId, ItemStack itemStack, ResourceLocation itemId, SanityEquippedSourceDefinition definition,
                        IntConsumer damageConsumer) {
    }

    private SanityEquippedSourceRegistry() {
    }

    public static void clear() {
        GLOBAL_SOURCES.clear();
        DIMENSIONAL_SOURCES.clear();
    }

    public static SanityEquippedSourceDefinition register(SanityEquippedSourceDefinition definition) {
        GLOBAL_SOURCES.remove(definition.getItemId());
        DIMENSIONAL_SOURCES.values().forEach(map -> map.remove(definition.getItemId()));

        if (definition.isGlobal()) {
            GLOBAL_SOURCES.put(definition.getItemId(), definition);
            return definition;
        }

        for (ResourceLocation dimensionId : definition.getDimensions()) {
            DIMENSIONAL_SOURCES
                    .computeIfAbsent(dimensionId, ignored -> new LinkedHashMap<>())
                    .put(definition.getItemId(), definition);
        }

        return definition;
    }

    public static SanityEquippedSourceDefinition get(ResourceLocation dimensionId, ResourceLocation itemId) {
        Map<ResourceLocation, SanityEquippedSourceDefinition> dimensionMap = DIMENSIONAL_SOURCES.get(dimensionId);
        if (dimensionMap != null && dimensionMap.containsKey(itemId)) {
            return dimensionMap.get(itemId);
        }

        SanityEquippedSourceDefinition definition = GLOBAL_SOURCES.get(itemId);
        return definition != null ? definition : getBuiltIn(dimensionId, itemId);
    }

    public static SanityEquippedSourceDefinition get(ServerPlayer player, ItemStack itemStack) {
        if (player == null || itemStack == null || itemStack.isEmpty()) {
            return null;
        }

        return get(player.getLevel().dimension().location(), Registry.ITEM.getKey(itemStack.getItem()));
    }

    public static List<Match> getMatches(ServerPlayer player) {
        List<Match> matches = new ArrayList<>();

        if (player == null) {
            return matches;
        }

        ResourceLocation dimensionId = player.getLevel().dimension().location();
        addVanillaMatch(player, dimensionId, EquipmentSlot.MAINHAND, matches);
        addVanillaMatch(player, dimensionId, EquipmentSlot.OFFHAND, matches);
        addVanillaMatch(player, dimensionId, EquipmentSlot.HEAD, matches);
        addVanillaMatch(player, dimensionId, EquipmentSlot.CHEST, matches);
        addVanillaMatch(player, dimensionId, EquipmentSlot.LEGS, matches);
        addVanillaMatch(player, dimensionId, EquipmentSlot.FEET, matches);

        if (ModList.get().isLoaded("curios")) {
            CuriosCompat.collectMatches(player, dimensionId, matches);
        }

        return matches;
    }

    public static float scaleRawChange(ServerPlayer player, float rawChange) {
        return rawChange == 0.0F ? 0.0F : rawChange * SanityProcessor.getSanityMultiplier(player, rawChange);
    }

    public static float getEffectiveContribution(ServerPlayer player, float rawChange) {
        return SanityHelper.getEffectiveRawSanityChange(player, scaleRawChange(player, rawChange));
    }

    public static float getContribution(ServerPlayer player, Match match) {
        if (match == null) {
            return 0.0F;
        }

        return getContribution(player, match, new SanityEquippedSourceEventJS(player, match.itemStack(), match.itemId(), match.slotId(), match.definition()));
    }

    public static float getContribution(ServerPlayer player, Match match, SanityEquippedSourceEventJS event) {
        if (player == null || match == null || match.itemStack() == null || match.itemStack().isEmpty()) {
            return 0.0F;
        }

        float contribution = getEffectiveContribution(player, match.definition().resolveRawSanityChange(event));
        if (contribution == 0.0F) {
            return 0.0F;
        }

        applyDamageIfNeeded(player, match);
        return contribution;
    }

    public static void applyDamageIfNeeded(ServerPlayer player, Match match) {
        if (player == null || match == null || match.damageConsumer() == null) {
            return;
        }

        SanityEquippedSourceDefinition definition = match.definition();
        if (definition == null || !definition.shouldDamageThisTick(player.tickCount)) {
            return;
        }

        match.damageConsumer().accept(definition.getDurabilityDamage());
    }

    static String toCurioSlotId(String identifier) {
        return SanityEquippedSourceDefinition.normalizeSlotId("curios:" + identifier);
    }

    private static void addVanillaMatch(ServerPlayer player, ResourceLocation dimensionId, EquipmentSlot slot, List<Match> matches) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) {
            return;
        }

        ResourceLocation itemId = Registry.ITEM.getKey(stack.getItem());
        SanityEquippedSourceDefinition definition = get(dimensionId, itemId);
        String slotId = SanityEquippedSourceDefinition.toSlotId(slot);

        if (definition == null || !definition.matchesSlot(slotId)) {
            return;
        }

        matches.add(new Match(slotId, stack, itemId, definition, amount -> damageVanilla(player, slot, stack, amount)));
    }

    private static void damageVanilla(ServerPlayer player, EquipmentSlot slot, ItemStack stack, int amount) {
        if (amount <= 0 || stack.isEmpty() || !stack.isDamageableItem()) {
            return;
        }

        stack.hurtAndBreak(amount, player, livingEntity -> livingEntity.broadcastBreakEvent(slot));
    }

    private static SanityEquippedSourceDefinition getBuiltIn(ResourceLocation dimensionId, ResourceLocation itemId) {
        if (dimensionId == null || itemId == null) {
            return null;
        }

        Item item = Registry.ITEM.get(itemId);
        if (!(item instanceof SanityEquippedSourceProvider provider)) {
            return null;
        }

        SanityEquippedSourceDefinition definition = provider.sanityJS$getEquippedSourceDefinition();
        return definition != null && definition.matchesDimension(dimensionId) ? definition : null;
    }
}
