package work.crash.skyraah.sanityjs.event;

import dev.latvian.mods.kubejs.player.PlayerEventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.world.entity.player.Player;
import work.crash.skyraah.sanityjs.util.IMathHelper;
import work.crash.skyraah.sanityjs.util.IPlayerSanity;

/**
 * @author skyraah
 */
@Info("Handle Sanity Change Event caused by Passive Events.")
public class SanityChangeEventJS extends PlayerEventJS {
    private final float sanity;
    private final float previousValue;
    private final float change;
    private final float rawChange;
    private final Player player;

    public SanityChangeEventJS(float rawChange, float previousValue, Player player) {
        this.rawChange = rawChange;
        this.previousValue = previousValue;
        this.change = -rawChange * 100.0F;
        this.sanity = Math.max(0.0F, Math.min(100.0F, previousValue + this.change));
        this.player = player;
    }

    @Override
    public Player getEntity() {
        return player;
    }

    @Info("Get the sanity value")
    public float getSanity() {
        return sanity;
    }

    @Info("Get the sanity value before the change")
    public float getPreviousSanity() {
        return previousValue;
    }

    @Info("Get the sanity change amount on the same 0-100 scale as getSanity()")
    public float getChange() {
        return change;
    }

    @HideFromJS
    @Info("Get the raw sanity delta from the base Sanity mod before it is converted to 0-100 scale")
    public float getRawChange() {
        return rawChange;
    }

    @HideFromJS
    @Info("Get the raw sanity value after the change on the base mod's 0-1 scale")
    public float getRawSanity() {
        return 1.0F - (sanity / 100.0F);
    }

    @HideFromJS
    @Info("Get the raw sanity value before the change on the base mod's 0-1 scale")
    public float getRawPreviousSanity() {
        return 1.0F - (previousValue / 100.0F);
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
