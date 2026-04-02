package work.crash.skyraah.sanityjs.mixin;

import croissantnova.sanitydim.SanityProcessor;
import croissantnova.sanitydim.capability.ISanity;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import work.crash.skyraah.sanityjs.event.SanityChangeEventJS;
import work.crash.skyraah.sanityjs.event.SanityEquippedSourceEventJS;
import work.crash.skyraah.sanityjs.event.SanityEventType;
import work.crash.skyraah.sanityjs.event.SanityEvents;
import work.crash.skyraah.sanityjs.event.SanityItemSourceEventJS;
import work.crash.skyraah.sanityjs.event.SanityTickEventJS;
import work.crash.skyraah.sanityjs.event.SanityTriggerEventJS;
import work.crash.skyraah.sanityjs.source.SanityEquippedSourceRegistry;
import work.crash.skyraah.sanityjs.source.SanityItemSourceRegistry;
import work.crash.skyraah.sanityjs.util.IPlayerSanity;

import java.util.function.Consumer;

/**
 * @author M1hono
 */
@Mixin(SanityProcessor.class)
public abstract class SanityProcessorMixin {

    @Inject(method = "addSanity", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$addSanity(ISanity sanity, float value, ServerPlayer player, CallbackInfo ci) {
        var changeEvent = SanityEvents.SAN_CHANGE.post(new SanityChangeEventJS(value, ((IPlayerSanity) player).getSanity(), player));
        if (changeEvent.interruptFalse()) {
            ci.cancel();
        }
    }

    @Inject(method = "tickPlayer", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$tickPlayer(ServerPlayer player, CallbackInfo ci) {
        var tickEvent = SanityEvents.TICK.post(new SanityTickEventJS(player));
        if (tickEvent.interruptFalse()) {
            ci.cancel();
        }
    }

    private static boolean sanityJS$postTrigger(ServerPlayer player, SanityEventType type) {
        return sanityJS$postTrigger(player, type, builder -> {
        });
    }

    private static boolean sanityJS$postTrigger(ServerPlayer player, SanityEventType type, Consumer<SanityTriggerEventJS.Builder> extras) {
        SanityTriggerEventJS.Builder builder = SanityTriggerEventJS.builder(player, type);
        extras.accept(builder);
        return SanityEvents.TRIGGER.post(builder.build(), type.id()).interruptFalse();
    }

    private static float sanityJS$collectEquippedSources(ServerPlayer player) {
        float total = 0.0F;

        for (var match : SanityEquippedSourceRegistry.getMatches(player)) {
            var equippedSourceEvent = new SanityEquippedSourceEventJS(player, match.itemStack(), match.itemId(), match.slotId(), match.definition());
            if (SanityEvents.EQUIPPED_SOURCE.post(equippedSourceEvent, match.itemStack().getItem()).interruptFalse() || equippedSourceEvent.isHandled()) {
                float contribution = SanityEquippedSourceRegistry.getEffectiveContribution(player, equippedSourceEvent.getQueuedRawSanityChange());
                if (equippedSourceEvent.shouldApplyRegisteredDamage() && contribution != 0.0F) {
                    SanityEquippedSourceRegistry.applyDamageIfNeeded(player, match);
                }

                total += contribution;
                continue;
            }

            total += SanityEquippedSourceRegistry.getContribution(player, match, equippedSourceEvent);
        }

        return total;
    }

    @Inject(method = "handlePlayerHurt", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerHurt(ServerPlayer player, float amount, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_HURT, builder -> builder.extra("amount", amount))) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerHurtAnimal", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerHurtAnimal(ServerPlayer player, Animal animal, float amount, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_HURT_ANIMAL, builder -> builder.extra("amount", amount).extra("animal", animal))) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerPetDeath", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerPetDeath(ServerPlayer player, TamableAnimal pet, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_PET_DEATH, builder -> builder.extra("pet", pet))) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerEnderManAngered", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerEnderManAngered(ServerPlayer player, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_ENDERMAN_ANGERED)) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerGotAdvancement", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerGotAdvancement(ServerPlayer player, Advancement advancement, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_ADVANCEMENT, builder -> builder.extra("advancement", advancement))) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerBredAnimals", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerBredAnimals(ServerPlayer player, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_BRED_ANIMALS)) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerTradedWithVillager", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerTradedWithVillager(ServerPlayer player, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_TRADED_WITH_VILLAGER)) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerUsedShears", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerUsedShears(ServerPlayer player, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_USED_SHEARS)) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerSpawnedChicken", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerSpawnedChicken(ServerPlayer player, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_SPAWNED_CHICKEN)) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerUsedItem", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerUsedItem(ServerPlayer player, ItemStack itemStack, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_USED_ITEM, builder -> builder.extra("itemStack", itemStack))) {
            ci.cancel();
            return;
        }

        var item = itemStack.getItem();
        var itemId = Registry.ITEM.getKey(item);
        var registeredSource = SanityItemSourceRegistry.get(player.getLevel().dimension().location(), itemId);
        var itemSourceEvent = new SanityItemSourceEventJS(player, itemStack, itemId, registeredSource);
        if (SanityEvents.ITEM_SOURCE.post(itemSourceEvent, item).interruptFalse() || itemSourceEvent.isHandled()) {
            ci.cancel();
            return;
        }

        if (registeredSource != null && SanityItemSourceRegistry.apply(itemSourceEvent, registeredSource)) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerFishedItem", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerFishedItem(ServerPlayer player, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_FISHED_ITEM)) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerMinedBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerMinedBlock(ServerPlayer player, BlockPos blockPos, BlockState blockState, Block block, boolean blockBroken, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_MINED_BLOCK, builder -> builder
                .extra("blockPos", blockPos)
                .extra("blockState", blockState)
                .extra("block", block)
                .extra("blockBroken", blockBroken))) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerTrampledFarmland", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerTrampledFarmland(ServerPlayer player, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_TRAMPLED_FARMLAND)) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerPottedFlower", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerPottedFlower(ServerPlayer player, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_POTTED_FLOWER)) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerChangedDimensions", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerChangedDimensions(ServerPlayer player, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_CHANGED_DIMENSIONS)) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerStruckByLightning", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sanityJS$handlePlayerStruckByLightning(ServerPlayer player, CallbackInfo ci) {
        if (sanityJS$postTrigger(player, SanityEventType.PLAYER_STRUCK_BY_LIGHTNING)) {
            ci.cancel();
        }
    }

    @Inject(method = "calcPassive", at = @At("TAIL"), cancellable = true, remap = false)
    private static void sanityJS$calcPassive(ServerPlayer player, ISanity sanity, CallbackInfoReturnable<Float> cir) {
        float value = cir.getReturnValue() + sanityJS$collectEquippedSources(player);

        if (value != 0) {
            var changeEvent = SanityEvents.SAN_CHANGE.post(new SanityChangeEventJS(value, ((IPlayerSanity) player).getSanity(), player));
            if (changeEvent.interruptFalse()) {
                value = 0.0F;
            }
        }

        cir.setReturnValue(value);
    }
}
