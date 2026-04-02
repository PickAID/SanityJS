package work.crash.skyraah.sanityjs.event;

import dev.latvian.mods.kubejs.player.PlayerEventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import work.crash.skyraah.sanityjs.source.SanityItemSourceDefinition;
import work.crash.skyraah.sanityjs.source.SanityItemSourceRegistry;
import work.crash.skyraah.sanityjs.util.IPlayerSanity;
import work.crash.skyraah.sanityjs.util.SanityHelper;

public class SanityItemSourceEventJS extends PlayerEventJS {
    private final ServerPlayer player;
    private final ItemStack itemStack;
    private final ResourceLocation itemId;
    private final SanityItemSourceDefinition registeredSource;
    private boolean handled;

    public SanityItemSourceEventJS(ServerPlayer player, ItemStack itemStack, ResourceLocation itemId, SanityItemSourceDefinition registeredSource) {
        this.player = player;
        this.itemStack = itemStack;
        this.itemId = itemId;
        this.registeredSource = registeredSource;
        this.handled = false;
    }

    @Override
    public ServerPlayer getEntity() {
        return player;
    }

    @Info("Get the item stack that triggered this sanity source")
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Info("Get the item id that triggered this sanity source")
    public ResourceLocation getItemId() {
        return itemId;
    }

    @Info("Check whether this item has a registered SanityJS source definition")
    public boolean hasRegisteredSource() {
        return registeredSource != null;
    }

    @Info("Get the registered SanityJS source definition for this item, if one exists")
    public SanityItemSourceDefinition getRegisteredSource() {
        return registeredSource;
    }

    @Info("Get the registered source sanity change on the 0-100 display scale")
    public float getRegisteredSanityChange() {
        return registeredSource == null ? 0.0F : registeredSource.resolveSanityChange(this);
    }

    @HideFromJS
    @Info("Get the registered source raw sanity change used by the base Sanity mod")
    public float getRegisteredRawSanityChange() {
        return registeredSource == null ? 0.0F : registeredSource.resolveRawSanityChange(this);
    }

    @Info("Check whether the registered source computes its sanity change dynamically from this event")
    public boolean hasDynamicRegisteredSource() {
        return registeredSource != null && registeredSource.hasDynamicSanityChange();
    }

    @Info("Get the registered source cooldown in ticks")
    public int getRegisteredCooldown() {
        return registeredSource == null ? 0 : registeredSource.getCooldown();
    }

    @Info("Get the registered source category key")
    public String getRegisteredCategoryKey() {
        return registeredSource == null ? null : registeredSource.getCategoryKey();
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

    @Info("Apply a sanity change using the base Sanity mod processor and multipliers. Positive values increase sanity on the 0-100 display scale.")
    public void applyChange(float displayChange) {
        handled = true;
        SanityHelper.applySanityChange(player, displayChange);
    }

    @HideFromJS
    @Info("Apply a raw sanity delta using the base Sanity mod processor. Positive values follow the base mod's raw 0-1 sanity logic.")
    public void applyRawChange(float rawChange) {
        handled = true;
        SanityHelper.applyRawSanityChange(player, rawChange);
    }

    @Info("Increase sanity using the base Sanity mod processor")
    public void increaseSanity(float amount) {
        applyChange(Math.abs(amount));
    }

    @Info("Decrease sanity using the base Sanity mod processor")
    public void decreaseSanity(float amount) {
        applyChange(-Math.abs(amount));
    }

    @Info("Apply the registered SanityJS source definition for this item")
    public boolean applyRegisteredSource() {
        if (registeredSource == null) {
            return false;
        }

        handled = SanityItemSourceRegistry.apply(this, registeredSource);
        return handled;
    }

    @Info("Consume this item source event without applying any additional change")
    public void consume() {
        handled = true;
    }

    public boolean isHandled() {
        return handled;
    }
}
