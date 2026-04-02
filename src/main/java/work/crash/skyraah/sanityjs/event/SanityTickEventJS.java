package work.crash.skyraah.sanityjs.event;

import dev.latvian.mods.kubejs.player.PlayerEventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.world.entity.player.Player;
import work.crash.skyraah.sanityjs.util.IMathHelper;
import work.crash.skyraah.sanityjs.util.IPlayerSanity;

/**
 * @author M1hono
 */
public class SanityTickEventJS extends PlayerEventJS {
    private final Player player;
    private final float sanity;

    public SanityTickEventJS(Player player) {
        this.player = player;
        this.sanity = ((IPlayerSanity) player).getSanity();
    }

    @Override
    public Player getEntity() {
        return player;
    }

    @Info("Get the sanity value")
    public float getSanity() {
        return sanity;
    }

    @HideFromJS
    @Info("Get the raw sanity value on the base mod's 0-1 scale")
    public float getRawSanity() {
        return 1.0F - (sanity / 100.0F);
    }

    @Info("Set the sanity value to a specific number (0-100)")
    public void setSanity(float value) {
        ((IPlayerSanity) player).setSanity(value);
    }

    @Info("Increase sanity")
    public void addSanity(float value) {
        ((IPlayerSanity) player).addSanity(value);
    }

    @Info("Decrease sanity")
    public void removeSanity(float value) {
        ((IPlayerSanity) player).addSanity(-Math.abs(value));
    }

    @Info("Convert a number to a value between 0 and 1")
    public float clampNorm(float value) {
        return IMathHelper.clampNorm(value);
    }

    @Info("Convert the value between 0 and 1 into a readable number")
    public float unclampNorm(float normalizedValue) {
        return IMathHelper.unclampNorm(normalizedValue);
    }

    @Info("Truncate a number to n significant decimal places (rounding mode)")
    public float approximation(float value, int n) {
        return IMathHelper.approximation(value, n);
    }
}
