package work.crash.skyraah.sanityjs.event;

import dev.latvian.mods.kubejs.player.PlayerEventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import work.crash.skyraah.sanityjs.util.IPlayerSanity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SanityTriggerEventJS extends PlayerEventJS {
    private final ServerPlayer player;
    private final SanityEventType type;
    private final Map<String, Object> extras;

    private SanityTriggerEventJS(ServerPlayer player, SanityEventType type, Map<String, Object> extras) {
        this.player = player;
        this.type = type;
        this.extras = Collections.unmodifiableMap(new LinkedHashMap<>(extras));
    }

    public static Builder builder(ServerPlayer player, SanityEventType type) {
        return new Builder(player, type);
    }

    @Override
    public ServerPlayer getEntity() {
        return player;
    }

    @Info("Get the trigger type enum. Compare against SanityEventType.*")
    public SanityEventType getType() {
        return type;
    }

    @Info("Get the trigger type id string")
    public String getTypeId() {
        return type.id();
    }

    @Info("Check whether this trigger matches a specific type enum")
    public boolean isType(SanityEventType type) {
        return this.type == type;
    }

    @Info("Check whether this trigger matches a specific type id")
    public boolean hasTypeId(String type) {
        return this.type.matches(type);
    }

    @Info("Get all extra values attached to this trigger event")
    public Map<String, Object> getExtras() {
        return extras;
    }

    @Info("Get all extra keys attached to this trigger event")
    public Set<String> getExtraKeys() {
        return extras.keySet();
    }

    @Info("Check whether an extra value exists")
    public boolean hasExtra(String key) {
        return extras.containsKey(key);
    }

    @Info("Get an extra value by key")
    public Object getExtra(String key) {
        return extras.get(key);
    }

    @Info("Get an extra string value by key")
    public String getStringExtra(String key) {
        Object value = getExtra(key);
        return value == null ? null : String.valueOf(value);
    }

    @Info("Get an extra float value by key, or 0 if it is missing")
    public float getFloatExtra(String key) {
        Object value = getExtra(key);
        return value instanceof Number number ? number.floatValue() : 0.0F;
    }

    @Info("Get an extra int value by key, or 0 if it is missing")
    public int getIntExtra(String key) {
        Object value = getExtra(key);
        return value instanceof Number number ? number.intValue() : 0;
    }

    @Info("Get an extra boolean value by key, or false if it is missing")
    public boolean getBooleanExtra(String key) {
        Object value = getExtra(key);
        return value instanceof Boolean bool && bool;
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

    @Info("Get the current dimension id")
    public ResourceLocation getDimensionId() {
        return player.level().dimension().location();
    }

    @Info("Get the amount associated with the trigger, such as damage")
    public float getAmount() {
        return getFloatExtra("amount");
    }

    @Info("Check whether the block trigger reported a fully broken block")
    public boolean isBlockBroken() {
        return getBooleanExtra("blockBroken");
    }

    public static class Builder {
        private final ServerPlayer player;
        private final SanityEventType type;
        private final Map<String, Object> extras;

        private Builder(ServerPlayer player, SanityEventType type) {
            this.player = player;
            this.type = type;
            this.extras = new LinkedHashMap<>();
        }

        public Builder extra(String key, Object value) {
            extras.put(key, value);
            return this;
        }

        public Builder extraIfNotNull(String key, Object value) {
            if (value != null) {
                extras.put(key, value);
            }
            return this;
        }

        public SanityTriggerEventJS build() {
            return new SanityTriggerEventJS(player, type, extras);
        }
    }
}
