package work.crash.skyraah.sanityjs.source;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import work.crash.skyraah.sanityjs.event.SanityEquippedSourceEventJS;
import work.crash.skyraah.sanityjs.util.SanityHelper;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class SanityEquippedSourceDefinition {
    private final ResourceLocation itemId;
    private final float rawSanityChange;
    private final SanityEquippedSourceFunction rawSanityFunction;
    private final Set<String> slotIds;
    private final Set<ResourceLocation> dimensions;
    private final int durabilityDamage;
    private final int damageInterval;

    public SanityEquippedSourceDefinition(ResourceLocation itemId, float rawSanityChange, Set<String> slotIds,
                                          Set<ResourceLocation> dimensions, int durabilityDamage, int damageInterval) {
        this(itemId, rawSanityChange, null, slotIds, dimensions, durabilityDamage, damageInterval);
    }

    public SanityEquippedSourceDefinition(ResourceLocation itemId, float rawSanityChange, SanityEquippedSourceFunction rawSanityFunction,
                                         Set<String> slotIds, Set<ResourceLocation> dimensions, int durabilityDamage, int damageInterval) {
        this.itemId = itemId;
        this.rawSanityChange = rawSanityChange;
        this.rawSanityFunction = rawSanityFunction;
        this.slotIds = Collections.unmodifiableSet(new LinkedHashSet<>(slotIds.stream().map(SanityEquippedSourceDefinition::normalizeSlotId).toList()));
        this.dimensions = Collections.unmodifiableSet(new LinkedHashSet<>(dimensions));
        this.durabilityDamage = Math.max(0, durabilityDamage);
        this.damageInterval = Math.max(0, damageInterval);
    }

    public static String toSlotId(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> "mainhand";
            case OFFHAND -> "offhand";
            case FEET -> "feet";
            case LEGS -> "legs";
            case CHEST -> "chest";
            case HEAD -> "head";
        };
    }

    public static String normalizeSlotId(String slotId) {
        if (slotId == null) {
            return "";
        }

        String normalized = slotId.trim().toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "main", "main_hand", "mainhand", "hand" -> "mainhand";
            case "off", "off_hand", "offhand" -> "offhand";
            case "helmet", "head" -> "head";
            case "chest", "chestplate" -> "chest";
            case "legs", "leggings" -> "legs";
            case "feet", "boots" -> "feet";
            default -> normalized;
        };
    }

    @Info("Get the item id that this equipped source definition applies to")
    public ResourceLocation getItemId() {
        return itemId;
    }

    @HideFromJS
    @Info("Get the raw passive sanity delta used by the base Sanity mod")
    public float getRawSanityChange() {
        return rawSanityChange;
    }

    @Info("Get the display-scale passive sanity delta where positive values increase sanity and negative values decrease it")
    public float getSanityChange() {
        return SanityHelper.rawToDisplayChange(rawSanityChange);
    }

    @Info("Check whether this equipped source computes its sanity change dynamically from the equipped source event")
    public boolean hasDynamicSanityChange() {
        return rawSanityFunction != null;
    }

    @HideFromJS
    @Info("Resolve the raw passive sanity delta for a specific equipped source event")
    public float resolveRawSanityChange(SanityEquippedSourceEventJS event) {
        if (rawSanityFunction == null) {
            return rawSanityChange;
        }

        Float resolved = rawSanityFunction.apply(event);
        return resolved == null || !Float.isFinite(resolved) ? rawSanityChange : resolved;
    }

    @Info("Resolve the display-scale passive sanity delta for a specific equipped source event")
    public float resolveSanityChange(SanityEquippedSourceEventJS event) {
        return SanityHelper.rawToDisplayChange(resolveRawSanityChange(event));
    }

    @Info("Get the slot ids this source applies to. An empty set means all equipped slots")
    public Set<String> getSlotIds() {
        return slotIds;
    }

    @Info("Get the dimensions this source applies to. An empty set means all dimensions")
    public Set<ResourceLocation> getDimensions() {
        return dimensions;
    }

    @Info("Get the durability damage applied when this source triggers")
    public int getDurabilityDamage() {
        return durabilityDamage;
    }

    @Info("Get the tick interval between durability damage applications")
    public int getDamageInterval() {
        return damageInterval;
    }

    @Info("Check whether this source applies in every equipped slot")
    public boolean isAnySlot() {
        return slotIds.isEmpty();
    }

    @Info("Check whether this source applies in every dimension")
    public boolean isGlobal() {
        return dimensions.isEmpty();
    }

    @Info("Check whether this source damages the equipped item while active")
    public boolean hasDurabilityCost() {
        return durabilityDamage > 0 && damageInterval > 0;
    }

    public boolean matches(ResourceLocation dimensionId, ResourceLocation id, String slotId) {
        return itemId.equals(id) && matchesDimension(dimensionId) && matchesSlot(slotId);
    }

    public boolean matchesDimension(ResourceLocation dimensionId) {
        return dimensions.isEmpty() || dimensions.contains(dimensionId);
    }

    public boolean matchesSlot(String slotId) {
        return slotIds.isEmpty() || slotIds.contains(normalizeSlotId(slotId));
    }

    public boolean shouldDamageThisTick(int tickCount) {
        return hasDurabilityCost() && tickCount > 0 && tickCount % damageInterval == 0;
    }
}
