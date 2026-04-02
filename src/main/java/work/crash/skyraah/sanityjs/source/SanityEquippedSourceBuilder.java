package work.crash.skyraah.sanityjs.source;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import work.crash.skyraah.sanityjs.util.SanityHelper;

import java.util.LinkedHashSet;
import java.util.Set;

public class SanityEquippedSourceBuilder {
    private final ResourceLocation itemId;
    private float rawSanityChange;
    private SanityEquippedSourceFunction rawSanityFunction;
    private final Set<String> slotIds;
    private final Set<ResourceLocation> dimensions;
    private int durabilityDamage;
    private int damageInterval;

    public SanityEquippedSourceBuilder(ResourceLocation itemId) {
        this.itemId = itemId;
        this.rawSanityChange = 0.0F;
        this.rawSanityFunction = null;
        this.slotIds = new LinkedHashSet<>();
        this.dimensions = new LinkedHashSet<>();
        this.durabilityDamage = 0;
        this.damageInterval = 0;
    }

    @Info("Set the passive sanity change on the 0-100 display scale. Positive values increase sanity and negative values decrease it")
    public SanityEquippedSourceBuilder sanity(float displayChange) {
        return rawSanity(SanityHelper.displayToRawChange(displayChange));
    }

    @Info("Resolve the passive sanity change dynamically from the equipped source event on the 0-100 display scale. Return positive values to increase sanity and negative values to decrease it. Return null, undefined, or NaN to fall back to the amount set by sanity()")
    public SanityEquippedSourceBuilder sanityFromEvent(SanityEquippedSourceFunction function) {
        if (function == null) {
            this.rawSanityFunction = null;
            return this;
        }

        return rawSanity(event -> {
            Float displayChange = function.apply(event);
            if (displayChange == null || !Float.isFinite(displayChange)) {
                return null;
            }

            return SanityHelper.displayToRawChange(displayChange);
        });
    }

    @HideFromJS
    @Info("Set the raw passive sanity change used by the base Sanity mod")
    public SanityEquippedSourceBuilder rawSanity(float rawSanityChange) {
        this.rawSanityChange = rawSanityChange;
        this.rawSanityFunction = null;
        return this;
    }

    @HideFromJS
    @Info("Resolve the raw passive sanity change dynamically from the equipped source event. Return positive values to decrease displayed sanity and negative values to increase it, matching the base mod's raw scale")
    public SanityEquippedSourceBuilder rawSanity(SanityEquippedSourceFunction function) {
        this.rawSanityFunction = function;
        this.rawSanityChange = 0.0F;
        return this;
    }

    @Info("Increase sanity passively by the given amount on the 0-100 display scale")
    public SanityEquippedSourceBuilder increaseSanity(float amount) {
        return sanity(Math.abs(amount));
    }

    @Info("Decrease sanity passively by the given amount on the 0-100 display scale")
    public SanityEquippedSourceBuilder decreaseSanity(float amount) {
        return sanity(-Math.abs(amount));
    }

    @HideFromJS
    @Info("Limit this source to a specific vanilla equipment slot")
    public SanityEquippedSourceBuilder slot(EquipmentSlot slot) {
        if (slot != null) {
            slotIds.add(SanityEquippedSourceDefinition.toSlotId(slot));
        }
        return this;
    }

    @Info("Limit this source to a specific slot id such as head, offhand, or curios:charm")
    public SanityEquippedSourceBuilder slot(String slotId) {
        if (slotId != null && !slotId.isBlank()) {
            slotIds.add(SanityEquippedSourceDefinition.normalizeSlotId(slotId));
        }
        return this;
    }

    @Info("Limit this source to the head slot")
    public SanityEquippedSourceBuilder head() {
        return slot(EquipmentSlot.HEAD);
    }

    @Info("Limit this source to the chest slot")
    public SanityEquippedSourceBuilder chest() {
        return slot(EquipmentSlot.CHEST);
    }

    @Info("Limit this source to the legs slot")
    public SanityEquippedSourceBuilder legs() {
        return slot(EquipmentSlot.LEGS);
    }

    @Info("Limit this source to the feet slot")
    public SanityEquippedSourceBuilder feet() {
        return slot(EquipmentSlot.FEET);
    }

    @Info("Limit this source to the main hand slot")
    public SanityEquippedSourceBuilder mainHand() {
        return slot(EquipmentSlot.MAINHAND);
    }

    @Info("Limit this source to the offhand slot")
    public SanityEquippedSourceBuilder offHand() {
        return slot(EquipmentSlot.OFFHAND);
    }

    @Info("Limit this source to all armor slots")
    public SanityEquippedSourceBuilder armor() {
        return head().chest().legs().feet();
    }

    @Info("Limit this source to both hand slots")
    public SanityEquippedSourceBuilder hands() {
        return mainHand().offHand();
    }

    @Info("Limit this source to a Curios slot identifier such as charm or belt")
    public SanityEquippedSourceBuilder curio(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return this;
        }

        String normalized = identifier.startsWith("curios:") ? identifier : "curios:" + identifier;
        return slot(normalized);
    }

    @Info("Remove all slot restrictions so the source applies in every equipped slot")
    public SanityEquippedSourceBuilder allSlots() {
        slotIds.clear();
        return this;
    }

    @HideFromJS
    @Info("Limit this source to a specific dimension")
    public SanityEquippedSourceBuilder dimension(ResourceLocation dimensionId) {
        if (dimensionId != null) {
            dimensions.add(dimensionId);
        }
        return this;
    }

    @Info("Limit this source to a specific dimension")
    public SanityEquippedSourceBuilder dimension(String dimensionId) {
        return dimension(SanityHelper.toId(dimensionId));
    }

    @Info("Remove all dimension restrictions so the source applies in every dimension")
    public SanityEquippedSourceBuilder allDimensions() {
        dimensions.clear();
        return this;
    }

    @Info("Set the durability damage applied while this source is active")
    public SanityEquippedSourceBuilder damage(int amount) {
        this.durabilityDamage = Math.max(0, amount);
        return this;
    }

    @Info("Set the tick interval between durability damage applications")
    public SanityEquippedSourceBuilder damageInterval(int ticks) {
        this.damageInterval = Math.max(0, ticks);
        return this;
    }

    @Info("Get the item id for this equipped source definition")
    public ResourceLocation getItemId() {
        return itemId;
    }

    @HideFromJS
    @Info("Get the configured raw passive sanity change")
    public float getRawSanityChange() {
        return rawSanityChange;
    }

    @Info("Check whether this equipped source computes its sanity change dynamically from the equipped source event")
    public boolean hasDynamicSanityChange() {
        return rawSanityFunction != null;
    }

    @Info("Get the configured display-scale passive sanity change")
    public float getSanityChange() {
        return SanityHelper.rawToDisplayChange(rawSanityChange);
    }

    @Info("Get the configured slot ids")
    public Set<String> getSlotIds() {
        return new LinkedHashSet<>(slotIds);
    }

    @Info("Get the configured durability damage")
    public int getDurabilityDamage() {
        return durabilityDamage;
    }

    @Info("Get the configured damage interval")
    public int getDamageInterval() {
        return damageInterval;
    }

    public SanityEquippedSourceDefinition build() {
        return new SanityEquippedSourceDefinition(itemId, rawSanityChange, rawSanityFunction, slotIds, dimensions, durabilityDamage, damageInterval);
    }

    @Info("Register this equipped source definition")
    public SanityEquippedSourceDefinition register() {
        SanityEquippedSourceDefinition definition = build();
        SanityEquippedSourceRegistry.register(definition);
        return definition;
    }
}
