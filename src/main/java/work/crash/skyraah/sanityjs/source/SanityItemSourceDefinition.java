package work.crash.skyraah.sanityjs.source;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import work.crash.skyraah.sanityjs.event.SanityItemSourceEventJS;
import work.crash.skyraah.sanityjs.util.SanityHelper;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class SanityItemSourceDefinition {
    private final ResourceLocation itemId;
    private final float rawSanityChange;
    private final SanityItemSourceFunction rawSanityFunction;
    private final String categoryKey;
    private final int cooldown;
    private final Set<ResourceLocation> dimensions;

    public SanityItemSourceDefinition(ResourceLocation itemId, float rawSanityChange, String categoryKey, int cooldown, Set<ResourceLocation> dimensions) {
        this(itemId, rawSanityChange, null, categoryKey, cooldown, dimensions);
    }

    public SanityItemSourceDefinition(ResourceLocation itemId, float rawSanityChange, SanityItemSourceFunction rawSanityFunction,
                                      String categoryKey, int cooldown, Set<ResourceLocation> dimensions) {
        this.itemId = itemId;
        this.rawSanityChange = rawSanityChange;
        this.rawSanityFunction = rawSanityFunction;
        this.categoryKey = categoryKey == null || categoryKey.isBlank() ? itemId.toString() : categoryKey;
        this.cooldown = Math.max(0, cooldown);
        this.dimensions = Collections.unmodifiableSet(new LinkedHashSet<>(dimensions));
    }

    @Info("Get the item id that this source definition applies to")
    public ResourceLocation getItemId() {
        return itemId;
    }

    @HideFromJS
    @Info("Get the raw sanity delta used by the base Sanity mod")
    public float getRawSanityChange() {
        return rawSanityChange;
    }

    @Info("Get the display-scale sanity delta where positive values increase sanity and negative values decrease it")
    public float getSanityChange() {
        return SanityHelper.rawToDisplayChange(rawSanityChange);
    }

    @Info("Check whether this source computes its sanity change dynamically from the item source event")
    public boolean hasDynamicSanityChange() {
        return rawSanityFunction != null;
    }

    @HideFromJS
    @Info("Resolve the raw sanity delta for a specific item source event")
    public float resolveRawSanityChange(SanityItemSourceEventJS event) {
        if (rawSanityFunction == null) {
            return rawSanityChange;
        }

        Float resolved = rawSanityFunction.apply(event);
        return resolved == null || !Float.isFinite(resolved) ? rawSanityChange : resolved;
    }

    @Info("Resolve the display-scale sanity delta for a specific item source event")
    public float resolveSanityChange(SanityItemSourceEventJS event) {
        return SanityHelper.rawToDisplayChange(resolveRawSanityChange(event));
    }

    @Info("Get the shared category key used for source cooldown grouping")
    public String getCategoryKey() {
        return categoryKey;
    }

    @Info("Get the cooldown in ticks for this source")
    public int getCooldown() {
        return cooldown;
    }

    @Info("Get the dimensions this source applies to. An empty set means all dimensions")
    public Set<ResourceLocation> getDimensions() {
        return dimensions;
    }

    @Info("Check whether this source applies in every dimension")
    public boolean isGlobal() {
        return dimensions.isEmpty();
    }

    public boolean matches(ResourceLocation dimensionId, ResourceLocation id) {
        return itemId.equals(id) && (dimensions.isEmpty() || dimensions.contains(dimensionId));
    }
}
