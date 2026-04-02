package work.crash.skyraah.sanityjs.source;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import work.crash.skyraah.sanityjs.util.SanityHelper;

import java.util.LinkedHashSet;
import java.util.Set;

public class SanityItemSourceBuilder {
    private final ResourceLocation itemId;
    private float rawSanityChange;
    private SanityItemSourceFunction rawSanityFunction;
    private String categoryKey;
    private int cooldown;
    private final Set<ResourceLocation> dimensions;

    public SanityItemSourceBuilder(ResourceLocation itemId) {
        this.itemId = itemId;
        this.rawSanityChange = 0.0F;
        this.rawSanityFunction = null;
        this.categoryKey = itemId.toString();
        this.cooldown = 0;
        this.dimensions = new LinkedHashSet<>();
    }

    @Info("Set the source sanity change on the 0-100 display scale. Positive values increase sanity and negative values decrease it")
    public SanityItemSourceBuilder sanity(float displayChange) {
        return rawSanity(SanityHelper.displayToRawChange(displayChange));
    }

    @Info("Resolve the source sanity change dynamically from the item source event on the 0-100 display scale. Return positive values to increase sanity and negative values to decrease it. Return null, undefined, or NaN to fall back to the amount set by sanity()")
    public SanityItemSourceBuilder sanityFromEvent(SanityItemSourceFunction function) {
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
    @Info("Set the raw sanity change used by the base Sanity mod")
    public SanityItemSourceBuilder rawSanity(float rawSanityChange) {
        this.rawSanityChange = rawSanityChange;
        this.rawSanityFunction = null;
        return this;
    }

    @HideFromJS
    @Info("Resolve the raw sanity change dynamically from the item source event. Return positive values to decrease displayed sanity and negative values to increase it, matching the base mod's raw scale")
    public SanityItemSourceBuilder rawSanity(SanityItemSourceFunction function) {
        this.rawSanityFunction = function;
        this.rawSanityChange = 0.0F;
        return this;
    }

    @Info("Increase sanity by the given amount on the 0-100 display scale")
    public SanityItemSourceBuilder increaseSanity(float amount) {
        return sanity(Math.abs(amount));
    }

    @Info("Decrease sanity by the given amount on the 0-100 display scale")
    public SanityItemSourceBuilder decreaseSanity(float amount) {
        return sanity(-Math.abs(amount));
    }

    @Info("Set the shared cooldown category key for this source. Reuse the same key across items to share cooldowns")
    public SanityItemSourceBuilder category(String categoryKey) {
        if (categoryKey != null && !categoryKey.isBlank()) {
            this.categoryKey = categoryKey;
        }
        return this;
    }

    @Info("Set the cooldown for this source in ticks")
    public SanityItemSourceBuilder cooldown(int cooldown) {
        this.cooldown = Math.max(0, cooldown);
        return this;
    }

    @HideFromJS
    @Info("Limit this source to a specific dimension")
    public SanityItemSourceBuilder dimension(ResourceLocation dimensionId) {
        if (dimensionId != null) {
            dimensions.add(dimensionId);
        }
        return this;
    }

    @Info("Limit this source to a specific dimension")
    public SanityItemSourceBuilder dimension(String dimensionId) {
        return dimension(SanityHelper.toId(dimensionId));
    }

    @Info("Remove all dimension restrictions so the source applies in every dimension")
    public SanityItemSourceBuilder allDimensions() {
        dimensions.clear();
        return this;
    }

    @Info("Get the item id for this source definition")
    public ResourceLocation getItemId() {
        return itemId;
    }

    @Info("Get the configured cooldown in ticks")
    public int getCooldown() {
        return cooldown;
    }

    @Info("Get the configured source category key")
    public String getCategoryKey() {
        return categoryKey;
    }

    @HideFromJS
    @Info("Get the configured raw sanity change")
    public float getRawSanityChange() {
        return rawSanityChange;
    }

    @Info("Check whether this source computes its sanity change dynamically from the item source event")
    public boolean hasDynamicSanityChange() {
        return rawSanityFunction != null;
    }

    @Info("Get the configured display-scale sanity change")
    public float getSanityChange() {
        return SanityHelper.rawToDisplayChange(rawSanityChange);
    }

    public SanityItemSourceDefinition build() {
        return new SanityItemSourceDefinition(itemId, rawSanityChange, rawSanityFunction, categoryKey, cooldown, dimensions);
    }

    @Info("Register this item source definition")
    public SanityItemSourceDefinition register() {
        SanityItemSourceDefinition definition = build();
        SanityItemSourceRegistry.register(definition);
        return definition;
    }
}
