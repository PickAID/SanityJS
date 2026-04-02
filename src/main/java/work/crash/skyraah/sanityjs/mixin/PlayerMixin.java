package work.crash.skyraah.sanityjs.mixin;

import com.mojang.authlib.GameProfile;
import croissantnova.sanitydim.capability.SanityProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.crash.skyraah.sanityjs.util.IPlayerSanity;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author skyraah
 */
@Mixin(Player.class)
public abstract class PlayerMixin implements IPlayerSanity {
    @Unique
    public float sanity;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onConstruct(Level arg, BlockPos arg2, float f, GameProfile gameProfile, ProfilePublicKey arg3, CallbackInfo ci) {
        this.sanity = getSanity();
    }

    @Unique
    @Override
    public float getSanity() {
        AtomicReference<Float> sanityValue = new AtomicReference<>((float) 0);
        ((Player)(Object) this).getCapability(SanityProvider.CAP).ifPresent(sanity -> {
            sanityValue.set((1.0f - sanity.getSanity()) * 100.0f);
        });
        return sanityValue.get();
    }

    @Unique
    @Override
    public void setSanity(float value) {
        ((Player)(Object) this).getCapability(SanityProvider.CAP).ifPresent(sanity -> {
            float clampedValue = Math.max(0.0f, Math.min(100.0f, value));
            sanity.setSanity(1.0f - (clampedValue / 100.0f));
        });
    }

    @Unique
    @Override
    public void addSanity(float value) {
        ((Player)(Object) this).getCapability(SanityProvider.CAP).ifPresent(sanity -> {
            float currentSanity = (1.0f - sanity.getSanity()) * 100.0f;
            float newSanity = currentSanity + value;
            newSanity = Math.max(0.0f, Math.min(100.0f, newSanity));
            sanity.setSanity(1.0f - (newSanity / 100.0f));
        });
    }
}
