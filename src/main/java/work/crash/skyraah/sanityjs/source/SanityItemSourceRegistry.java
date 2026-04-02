package work.crash.skyraah.sanityjs.source;

import croissantnova.sanitydim.SanityProcessor;
import croissantnova.sanitydim.capability.ISanity;
import croissantnova.sanitydim.capability.IPersistentSanity;
import croissantnova.sanitydim.capability.SanityProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import work.crash.skyraah.sanityjs.event.SanityItemSourceEventJS;
import work.crash.skyraah.sanityjs.util.IMathHelper;
import work.crash.skyraah.sanityjs.util.SanityHelper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class SanityItemSourceRegistry {
    private static final Map<ResourceLocation, SanityItemSourceDefinition> GLOBAL_SOURCES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Map<ResourceLocation, SanityItemSourceDefinition>> DIMENSIONAL_SOURCES = new LinkedHashMap<>();
    private static final Map<String, Integer> CATEGORY_IDS = new LinkedHashMap<>();
    private static int nextSyntheticCategoryId = -1;

    private SanityItemSourceRegistry() {
    }

    public static void clear() {
        GLOBAL_SOURCES.clear();
        DIMENSIONAL_SOURCES.clear();
        CATEGORY_IDS.clear();
        nextSyntheticCategoryId = -1;
    }

    public static SanityItemSourceDefinition register(SanityItemSourceDefinition definition) {
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

    public static SanityItemSourceDefinition get(ResourceLocation dimensionId, ResourceLocation itemId) {
        Map<ResourceLocation, SanityItemSourceDefinition> dimensionMap = DIMENSIONAL_SOURCES.get(dimensionId);
        if (dimensionMap != null && dimensionMap.containsKey(itemId)) {
            return dimensionMap.get(itemId);
        }

        SanityItemSourceDefinition definition = GLOBAL_SOURCES.get(itemId);
        return definition != null ? definition : getBuiltIn(dimensionId, itemId);
    }

    public static SanityItemSourceDefinition get(ServerPlayer player, ItemStack itemStack) {
        if (player == null || itemStack == null || itemStack.isEmpty()) {
            return null;
        }

        return get(player.getLevel().dimension().location(), Registry.ITEM.getKey(itemStack.getItem()));
    }

    public static boolean apply(ServerPlayer player, ItemStack itemStack) {
        return apply(player, itemStack, get(player, itemStack));
    }

    public static boolean apply(ServerPlayer player, ItemStack itemStack, SanityItemSourceDefinition definition) {
        ResourceLocation itemId = itemStack == null || itemStack.isEmpty() ? null : Registry.ITEM.getKey(itemStack.getItem());
        return apply(new SanityItemSourceEventJS(player, itemStack, itemId, definition), definition);
    }

    public static boolean apply(SanityItemSourceEventJS event, SanityItemSourceDefinition definition) {
        if (event == null) {
            return false;
        }

        return apply(event.getEntity(), event.getItemStack(), definition, event);
    }

    private static boolean apply(ServerPlayer player, ItemStack itemStack, SanityItemSourceDefinition definition, SanityItemSourceEventJS event) {
        if (player == null || itemStack == null || itemStack.isEmpty() || definition == null) {
            return false;
        }

        Optional<ISanity> sanityOptional = player.getCapability(SanityProvider.CAP).resolve();
        if (sanityOptional.isEmpty()) {
            return false;
        }

        ISanity sanity = sanityOptional.get();
        float rawChange = definition.resolveRawSanityChange(event);

        if (!(sanity instanceof IPersistentSanity persistentSanity) || definition.getCooldown() <= 0) {
            float effectiveRawChange = SanityHelper.getEffectiveRawSanityChange(player, rawChange);
            if (effectiveRawChange == 0.0F) {
                return false;
            }

            SanityProcessor.addSanity(sanity, effectiveRawChange, player);
            return true;
        }

        Map<Integer, Integer> cooldowns = persistentSanity.getItemCooldowns();
        int categoryId = getOrCreateCategoryId(definition.getCategoryKey());
        int currentCooldown = cooldowns.getOrDefault(categoryId, 0);

        float adjustedRawChange = rawChange;
        if (currentCooldown > 0) {
            float normalizedCooldown = IMathHelper.clampNorm((definition.getCooldown() - currentCooldown) / (float) definition.getCooldown());
            adjustedRawChange *= normalizedCooldown;
        }

        float effectiveRawChange = SanityHelper.getEffectiveRawSanityChange(player, adjustedRawChange);
        if (effectiveRawChange == 0.0F) {
            return false;
        }

        SanityProcessor.addSanity(sanity, effectiveRawChange, player);
        cooldowns.put(categoryId, definition.getCooldown());
        return true;
    }

    static int getOrCreateCategoryId(String categoryKey) {
        return CATEGORY_IDS.computeIfAbsent(categoryKey, ignored -> nextSyntheticCategoryId--);
    }

    private static SanityItemSourceDefinition getBuiltIn(ResourceLocation dimensionId, ResourceLocation itemId) {
        if (dimensionId == null || itemId == null) {
            return null;
        }

        Item item = Registry.ITEM.get(itemId);
        if (!(item instanceof SanityItemSourceProvider provider)) {
            return null;
        }

        SanityItemSourceDefinition definition = provider.sanityJS$getItemSourceDefinition();
        return definition != null && definition.matches(dimensionId, itemId) ? definition : null;
    }
}
