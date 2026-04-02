package work.crash.skyraah.sanityjs.event;

import dev.latvian.mods.kubejs.player.PlayerEventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import work.crash.skyraah.sanityjs.source.SanityEquippedSourceDefinition;
import work.crash.skyraah.sanityjs.source.SanityEquippedSourceRegistry;
import work.crash.skyraah.sanityjs.util.IPlayerSanity;
import work.crash.skyraah.sanityjs.util.SanityHelper;

public class SanityEquippedSourceEventJS extends PlayerEventJS {
    private final ServerPlayer player;
    private final ItemStack itemStack;
    private final ResourceLocation itemId;
    private final String slotId;
    private final SanityEquippedSourceDefinition registeredSource;
    private boolean handled;
    private boolean applyRegisteredDamage;
    private float accumulatedRawChange;

    public SanityEquippedSourceEventJS(ServerPlayer player, ItemStack itemStack, ResourceLocation itemId, String slotId,
                                       SanityEquippedSourceDefinition registeredSource) {
        this.player = player;
        this.itemStack = itemStack;
        this.itemId = itemId;
        this.slotId = slotId;
        this.registeredSource = registeredSource;
        this.handled = false;
        this.applyRegisteredDamage = false;
        this.accumulatedRawChange = 0.0F;
    }

    @Override
    public ServerPlayer getEntity() {
        return player;
    }

    @Info("Get the equipped item stack that triggered this passive source")
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Info("Get the equipped item id that triggered this passive source")
    public ResourceLocation getItemId() {
        return itemId;
    }

    @Info("Get the slot id for this equipped source, such as head, offhand, or curios:charm")
    public String getSlotId() {
        return slotId;
    }

    @Info("Check whether this source came from a Curios slot")
    public boolean isCurioSlot() {
        return slotId.startsWith("curios:");
    }

    @Info("Get the Curios slot identifier for this source, if it came from Curios")
    public String getCurioIdentifier() {
        return isCurioSlot() ? slotId.substring("curios:".length()) : null;
    }

    @Info("Check whether this equipped item has a registered SanityJS passive source definition")
    public boolean hasRegisteredSource() {
        return registeredSource != null;
    }

    @Info("Get the registered SanityJS passive source definition for this equipped item, if one exists")
    public SanityEquippedSourceDefinition getRegisteredSource() {
        return registeredSource;
    }

    @Info("Get the registered passive sanity change on the 0-100 display scale")
    public float getRegisteredSanityChange() {
        return registeredSource == null ? 0.0F : registeredSource.resolveSanityChange(this);
    }

    @HideFromJS
    @Info("Get the registered raw passive sanity change used by the base Sanity mod")
    public float getRegisteredRawSanityChange() {
        return registeredSource == null ? 0.0F : registeredSource.resolveRawSanityChange(this);
    }

    @Info("Check whether the registered passive source computes its sanity change dynamically from this event")
    public boolean hasDynamicRegisteredSource() {
        return registeredSource != null && registeredSource.hasDynamicSanityChange();
    }

    @Info("Get the player's current sanity on the 0-100 display scale")
    public float getSanity() {
        return ((IPlayerSanity) player).getSanity();
    }

    @HideFromJS
    @Info("Get the player's current raw sanity on the base mod's 0-1 scale")
    public float getRawSanity() {
        return 1.0F - (getSanity() / 100.0F);
    }

    @Info("Queue a passive sanity change using the 0-100 display scale. Positive values increase sanity.")
    public void applyChange(float displayChange) {
        handled = true;
        accumulatedRawChange += SanityHelper.displayToRawChange(displayChange);
    }

    @HideFromJS
    @Info("Queue a passive raw sanity delta using the base Sanity mod's 0-1 scale")
    public void applyRawChange(float rawChange) {
        handled = true;
        accumulatedRawChange += rawChange;
    }

    @Info("Increase sanity passively")
    public void increaseSanity(float amount) {
        applyChange(Math.abs(amount));
    }

    @Info("Decrease sanity passively")
    public void decreaseSanity(float amount) {
        applyChange(-Math.abs(amount));
    }

    @Info("Apply the registered passive source definition for this equipped item")
    public boolean applyRegisteredSource() {
        if (registeredSource == null) {
            return false;
        }

        float rawChange = registeredSource.resolveRawSanityChange(this);
        float effectiveContribution = SanityEquippedSourceRegistry.getEffectiveContribution(player, rawChange);
        if (effectiveContribution == 0.0F) {
            return false;
        }

        handled = true;
        applyRegisteredDamage = registeredSource.hasDurabilityCost();
        accumulatedRawChange += rawChange;
        return true;
    }

    @Info("Consume this passive source event without applying any passive change")
    public void consume() {
        handled = true;
    }

    @Info("Get the queued passive sanity change on the 0-100 display scale")
    public float getQueuedSanityChange() {
        return SanityHelper.rawToDisplayChange(accumulatedRawChange);
    }

    @HideFromJS
    @Info("Get the queued passive raw sanity change")
    public float getQueuedRawSanityChange() {
        return accumulatedRawChange;
    }

    public boolean isHandled() {
        return handled;
    }

    public boolean shouldApplyRegisteredDamage() {
        return applyRegisteredDamage;
    }
}
