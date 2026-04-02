package work.crash.skyraah.sanityjs.item;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import work.crash.skyraah.sanityjs.source.SanityItemSourceBuilder;
import work.crash.skyraah.sanityjs.source.SanityItemSourceDefinition;
import work.crash.skyraah.sanityjs.source.SanityItemSourceFunction;
import work.crash.skyraah.sanityjs.source.SanityItemSourceProvider;
import work.crash.skyraah.sanityjs.source.SanityItemSourceRegistry;

public class SanitySourceItem extends Item implements SanityItemSourceProvider {
    private static final int DEFAULT_USE_DURATION = 32;
    private final boolean consumeOnUse;
    private final SanityItemSourceDefinition sourceDefinition;

    public SanitySourceItem(Builder builder, SanityItemSourceDefinition sourceDefinition) {
        super(builder.createItemProperties());
        this.consumeOnUse = builder.consumeOnUse;
        this.sourceDefinition = sourceDefinition;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (result.getResult() != InteractionResult.PASS) {
            return result;
        }

        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        UseAnim animation = super.getUseAnimation(stack);
        return animation != UseAnim.NONE ? animation : UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        int duration = super.getUseDuration(stack);
        return duration > 0 ? duration : DEFAULT_USE_DURATION;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        int countBefore = stack.getCount();
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!consumeOnUse || result.isEmpty() || result.getCount() < countBefore) {
            return result;
        }

        if (entity instanceof Player player) {
            player.awardStat(Stats.ITEM_USED.get(this));

            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            }

            if (player.getAbilities().instabuild) {
                return result;
            }
        }

        ItemStack remainder = hasCraftingRemainingItem() ? new ItemStack(getCraftingRemainingItem()) : ItemStack.EMPTY;
        result.shrink(1);

        if (result.isEmpty()) {
            return remainder.isEmpty() ? result : remainder;
        }

        if (!remainder.isEmpty() && entity instanceof Player player && !player.getInventory().add(remainder)) {
            player.drop(remainder, false);
        }

        return result;
    }

    @Override
    public SanityItemSourceDefinition sanityJS$getItemSourceDefinition() {
        return sourceDefinition;
    }

    public static class Builder extends ItemBuilder {
        private final SanityItemSourceBuilder sourceBuilder;
        private boolean consumeOnUse;
        private SanityItemSourceDefinition sourceDefinition;

        public Builder(ResourceLocation id) {
            super(id);
            this.sourceBuilder = new SanityItemSourceBuilder(id);
            this.consumeOnUse = true;
        }

        @Info("Set the source sanity change on the 0-100 display scale. Positive values increase sanity and negative values decrease it")
        public Builder sanity(float displayChange) {
            sourceBuilder.sanity(displayChange);
            return this;
        }

        @HideFromJS
        @Info("Set the raw sanity change used by the base Sanity mod")
        public Builder rawSanity(float rawSanityChange) {
            sourceBuilder.rawSanity(rawSanityChange);
            return this;
        }

        @Info("Resolve the source sanity change dynamically from the item source event on the 0-100 display scale")
        public Builder sanityFromEvent(SanityItemSourceFunction function) {
            sourceBuilder.sanityFromEvent(function);
            return this;
        }

        @HideFromJS
        @Info("Resolve the raw sanity change dynamically from the item source event")
        public Builder rawSanity(SanityItemSourceFunction function) {
            sourceBuilder.rawSanity(function);
            return this;
        }

        @Info("Increase sanity by the given amount on the 0-100 display scale")
        public Builder increaseSanity(float amount) {
            sourceBuilder.increaseSanity(amount);
            return this;
        }

        @Info("Decrease sanity by the given amount on the 0-100 display scale")
        public Builder decreaseSanity(float amount) {
            sourceBuilder.decreaseSanity(amount);
            return this;
        }

        @Info("Set the shared cooldown category key for this source item")
        public Builder category(String categoryKey) {
            sourceBuilder.category(categoryKey);
            return this;
        }

        @Info("Set the cooldown for this source item in ticks")
        public Builder cooldown(int cooldown) {
            sourceBuilder.cooldown(cooldown);
            return this;
        }

        @HideFromJS
        @Info("Limit this source item to a specific dimension")
        public Builder dimension(ResourceLocation dimensionId) {
            sourceBuilder.dimension(dimensionId);
            return this;
        }

        @Info("Limit this source item to a specific dimension")
        public Builder dimension(String dimensionId) {
            sourceBuilder.dimension(dimensionId);
            return this;
        }

        @Info("Remove all dimension restrictions so the source applies in every dimension")
        public Builder allDimensions() {
            sourceBuilder.allDimensions();
            return this;
        }

        @Info("Set whether this source item should consume itself when finished")
        public Builder consume(boolean consumeOnUse) {
            this.consumeOnUse = consumeOnUse;
            return this;
        }

        @Override
        public SanitySourceItem createObject() {
            SanityItemSourceDefinition builtDefinition = getOrCreateSourceDefinition();
            SanityItemSourceRegistry.register(builtDefinition);
            return new SanitySourceItem(this, builtDefinition);
        }

        private SanityItemSourceDefinition getOrCreateSourceDefinition() {
            if (sourceDefinition == null) {
                sourceDefinition = sourceBuilder.build();
            }

            return sourceDefinition;
        }
    }
}
