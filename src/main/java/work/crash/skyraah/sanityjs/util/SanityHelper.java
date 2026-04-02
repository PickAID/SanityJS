package work.crash.skyraah.sanityjs.util;

import croissantnova.sanitydim.SanityProcessor;
import croissantnova.sanitydim.capability.IPassiveSanity;
import croissantnova.sanitydim.capability.IPersistentSanity;
import croissantnova.sanitydim.capability.SanityProvider;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import work.crash.skyraah.sanityjs.source.SanityEquippedSourceBuilder;
import work.crash.skyraah.sanityjs.source.SanityEquippedSourceDefinition;
import work.crash.skyraah.sanityjs.source.SanityEquippedSourceRegistry;
import work.crash.skyraah.sanityjs.source.SanityItemSourceBuilder;
import work.crash.skyraah.sanityjs.source.SanityItemSourceDefinition;
import work.crash.skyraah.sanityjs.source.SanityItemSourceRegistry;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public final class SanityHelper {
    private SanityHelper() {
    }

    public static float clampSanity(float value) {
        return Math.max(0.0F, Math.min(100.0F, value));
    }

    @HideFromJS
    public static float displayToRawChange(float displayChange) {
        return -(displayChange / 100.0F);
    }

    @HideFromJS
    public static float rawToDisplayChange(float rawChange) {
        return -rawChange * 100.0F;
    }

    @HideFromJS
    public static float clampRawSanity(float value) {
        return IMathHelper.clampNorm(value);
    }

    @HideFromJS
    public static float getEffectiveRawSanityChange(Player player, float rawChange) {
        if (player == null || rawChange == 0.0F) {
            return 0.0F;
        }

        float currentRawSanity = getRawSanity(player);
        float targetRawSanity = clampRawSanity(currentRawSanity + rawChange);
        return targetRawSanity - currentRawSanity;
    }

    @HideFromJS
    public static boolean hasEffectiveRawSanityChange(Player player, float rawChange) {
        return getEffectiveRawSanityChange(player, rawChange) != 0.0F;
    }

    public static boolean hasSanity(Player player) {
        return player.getCapability(SanityProvider.CAP).resolve().isPresent();
    }

    public static float getSanity(Player player) {
        return ((IPlayerSanity) player).getSanity();
    }

    @HideFromJS
    public static float getRawSanity(Player player) {
        return 1.0F - (getSanity(player) / 100.0F);
    }

    public static void setSanity(Player player, float value) {
        ((IPlayerSanity) player).setSanity(value);
    }

    @HideFromJS
    public static void setRawSanity(Player player, float value) {
        player.getCapability(SanityProvider.CAP).ifPresent(sanity -> sanity.setSanity(IMathHelper.clampNorm(value)));
    }

    public static void addSanity(Player player, float value) {
        ((IPlayerSanity) player).addSanity(value);
    }

    public static void removeSanity(Player player, float value) {
        ((IPlayerSanity) player).addSanity(-Math.abs(value));
    }

    public static ResourceLocation toId(String id) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid resource location: " + id);
        }
        return parsed;
    }

    public static void applySanityChange(ServerPlayer player, float displayChange) {
        applyRawSanityChange(player, displayToRawChange(displayChange));
    }

    @HideFromJS
    public static void applyRawSanityChange(ServerPlayer player, float rawChange) {
        player.getCapability(SanityProvider.CAP).ifPresent(sanity -> SanityProcessor.addSanity(sanity, rawChange, player));
    }

    public static float getPassiveIncrease(Player player) {
        return player.getCapability(SanityProvider.CAP)
                .filter(IPassiveSanity.class::isInstance)
                .map(IPassiveSanity.class::cast)
                .map(IPassiveSanity::getPassiveIncrease)
                .orElse(0.0F);
    }

    public static void setPassiveIncrease(Player player, float value) {
        player.getCapability(SanityProvider.CAP)
                .filter(IPassiveSanity.class::isInstance)
                .map(IPassiveSanity.class::cast)
                .ifPresent(passive -> passive.setPassiveIncrease(value));
    }

    public static int[] getActiveSourceCooldowns(Player player) {
        return player.getCapability(SanityProvider.CAP)
                .filter(IPersistentSanity.class::isInstance)
                .map(IPersistentSanity.class::cast)
                .map(IPersistentSanity::getActiveSourcesCooldowns)
                .orElseGet(() -> new int[0]);
    }

    public static Map<Integer, Integer> getItemCooldowns(Player player) {
        return player.getCapability(SanityProvider.CAP)
                .filter(IPersistentSanity.class::isInstance)
                .map(IPersistentSanity.class::cast)
                .map(IPersistentSanity::getItemCooldowns)
                .orElseGet(Collections::emptyMap);
    }

    public static Map<Integer, Integer> getBrokenBlockCooldowns(Player player) {
        return player.getCapability(SanityProvider.CAP)
                .filter(IPersistentSanity.class::isInstance)
                .map(IPersistentSanity.class::cast)
                .map(IPersistentSanity::getBrokenBlocksCooldowns)
                .orElseGet(Collections::emptyMap);
    }

    @HideFromJS
    public static SanityItemSourceBuilder itemSource(ResourceLocation itemId) {
        return new SanityItemSourceBuilder(itemId);
    }

    @HideFromJS
    public static SanityEquippedSourceBuilder equippedSource(ResourceLocation itemId) {
        return new SanityEquippedSourceBuilder(itemId);
    }

    public static SanityItemSourceBuilder itemSource(String itemId) {
        return itemSource(toId(itemId));
    }

    public static SanityEquippedSourceBuilder equippedSource(String itemId) {
        return equippedSource(toId(itemId));
    }

    @HideFromJS
    public static SanityItemSourceDefinition registerItemSource(ResourceLocation itemId, Consumer<SanityItemSourceBuilder> consumer) {
        SanityItemSourceBuilder builder = itemSource(itemId);
        consumer.accept(builder);
        return builder.register();
    }

    @HideFromJS
    public static SanityEquippedSourceDefinition registerEquippedSource(ResourceLocation itemId, Consumer<SanityEquippedSourceBuilder> consumer) {
        SanityEquippedSourceBuilder builder = equippedSource(itemId);
        consumer.accept(builder);
        return builder.register();
    }

    public static SanityItemSourceDefinition registerItemSource(String itemId, Consumer<SanityItemSourceBuilder> consumer) {
        return registerItemSource(toId(itemId), consumer);
    }

    public static SanityEquippedSourceDefinition registerEquippedSource(String itemId, Consumer<SanityEquippedSourceBuilder> consumer) {
        return registerEquippedSource(toId(itemId), consumer);
    }

    @HideFromJS
    public static SanityItemSourceDefinition getRegisteredItemSource(ResourceLocation dimensionId, ResourceLocation itemId) {
        return SanityItemSourceRegistry.get(dimensionId, itemId);
    }

    @HideFromJS
    public static SanityEquippedSourceDefinition getRegisteredEquippedSource(ResourceLocation dimensionId, ResourceLocation itemId) {
        return SanityEquippedSourceRegistry.get(dimensionId, itemId);
    }

    public static SanityItemSourceDefinition getRegisteredItemSourceInDimension(String dimensionId, String itemId) {
        return getRegisteredItemSource(toId(dimensionId), toId(itemId));
    }

    public static SanityEquippedSourceDefinition getRegisteredEquippedSourceInDimension(String dimensionId, String itemId) {
        return getRegisteredEquippedSource(toId(dimensionId), toId(itemId));
    }

    public static SanityItemSourceDefinition getRegisteredItemSource(ServerPlayer player, ItemStack itemStack) {
        return SanityItemSourceRegistry.get(player, itemStack);
    }

    public static SanityEquippedSourceDefinition getRegisteredEquippedSource(ServerPlayer player, ItemStack itemStack) {
        return SanityEquippedSourceRegistry.get(player, itemStack);
    }

    public static boolean applyRegisteredItemSource(ServerPlayer player, ItemStack itemStack) {
        return SanityItemSourceRegistry.apply(player, itemStack);
    }

    public static void triggerItemSource(ServerPlayer player, ItemStack itemStack) {
        SanityProcessor.handlePlayerUsedItem(player, itemStack);
    }

    public static void triggerPlayerHurt(ServerPlayer player, float amount) {
        SanityProcessor.handlePlayerHurt(player, amount);
    }

    public static void triggerAdvancement(ServerPlayer player, Advancement advancement) {
        SanityProcessor.handlePlayerGotAdvancement(player, advancement);
    }

    public static void triggerChangedDimension(ServerPlayer player) {
        SanityProcessor.handlePlayerChangedDimensions(player);
    }

    public static void triggerMinedBlock(ServerPlayer player, BlockPos pos, BlockState state, Block block, boolean broken) {
        SanityProcessor.handlePlayerMinedBlock(player, pos, state, block, broken);
    }
}
