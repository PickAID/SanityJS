package work.crash.skyraah.sanityjs.item;

import dev.latvian.mods.kubejs.core.ModifiableItemKJS;
import dev.latvian.mods.kubejs.item.custom.ArmorItemBuilder;
import dev.latvian.mods.kubejs.registry.KubeJSRegistries;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import work.crash.skyraah.sanityjs.source.SanityEquippedSourceBuilder;
import work.crash.skyraah.sanityjs.source.SanityEquippedSourceDefinition;
import work.crash.skyraah.sanityjs.source.SanityEquippedSourceFunction;
import work.crash.skyraah.sanityjs.source.SanityEquippedSourceProvider;
import work.crash.skyraah.sanityjs.source.SanityEquippedSourceRegistry;

public class SanityPassiveArmorItem extends ArmorItem implements SanityEquippedSourceProvider {
    private final SanityEquippedSourceDefinition sourceDefinition;

    protected SanityPassiveArmorItem(Builder<?> builder, SanityEquippedSourceDefinition sourceDefinition) {
        super(builder.armorTier, builder.equipmentSlot, builder.createItemProperties());
        this.sourceDefinition = sourceDefinition;
    }

    @Override
    public SanityEquippedSourceDefinition sanityJS$getEquippedSourceDefinition() {
        return sourceDefinition;
    }

    public abstract static class Builder<T extends Builder<T>> extends ArmorItemBuilder {
        protected final SanityEquippedSourceBuilder sourceBuilder;
        private SanityEquippedSourceDefinition sourceDefinition;

        protected Builder(ResourceLocation id, EquipmentSlot slot) {
            super(id, slot);
            this.sourceBuilder = new SanityEquippedSourceBuilder(id).slot(slot);
        }

        protected abstract T self();

        @Info("Set the passive sanity change on the 0-100 display scale. Positive values increase sanity and negative values decrease it")
        public T sanity(float displayChange) {
            sourceBuilder.sanity(displayChange);
            return self();
        }

        @HideFromJS
        @Info("Set the raw passive sanity change used by the base Sanity mod")
        public T rawSanity(float rawSanityChange) {
            sourceBuilder.rawSanity(rawSanityChange);
            return self();
        }

        @Info("Resolve the passive sanity change dynamically from the equipped source event on the 0-100 display scale")
        public T sanityFromEvent(SanityEquippedSourceFunction function) {
            sourceBuilder.sanityFromEvent(function);
            return self();
        }

        @HideFromJS
        @Info("Resolve the raw passive sanity change dynamically from the equipped source event")
        public T rawSanity(SanityEquippedSourceFunction function) {
            sourceBuilder.rawSanity(function);
            return self();
        }

        @Info("Increase sanity passively by the given amount on the 0-100 display scale")
        public T increaseSanity(float amount) {
            sourceBuilder.increaseSanity(amount);
            return self();
        }

        @Info("Decrease sanity passively by the given amount on the 0-100 display scale")
        public T decreaseSanity(float amount) {
            sourceBuilder.decreaseSanity(amount);
            return self();
        }

        @HideFromJS
        @Info("Limit this passive source to a specific dimension")
        public T dimension(ResourceLocation dimensionId) {
            sourceBuilder.dimension(dimensionId);
            return self();
        }

        @Info("Limit this passive source to a specific dimension")
        public T dimension(String dimensionId) {
            sourceBuilder.dimension(dimensionId);
            return self();
        }

        @Info("Remove all dimension restrictions so the passive source applies in every dimension")
        public T allDimensions() {
            sourceBuilder.allDimensions();
            return self();
        }

        @Info("Set the durability damage applied while this passive source is active")
        public T damage(int amount) {
            sourceBuilder.damage(amount);
            return self();
        }

        @Info("Set the tick interval between durability damage applications")
        public T damageInterval(int ticks) {
            sourceBuilder.damageInterval(ticks);
            return self();
        }

        @Override
        public Item createObject() {
            SanityEquippedSourceDefinition builtDefinition = getOrCreateSourceDefinition();
            SanityEquippedSourceRegistry.register(builtDefinition);
            SanityPassiveArmorItem item = new SanityPassiveArmorItem(this, builtDefinition);
            ModifiableItemKJS modifiableItem = (ModifiableItemKJS) (Object) item;

            if (!attributes.isEmpty()) {
                attributes.forEach((attributeId, modifier) -> {
                    var attribute = KubeJSRegistries.attributes().get(attributeId);
                    if (attribute != null) {
                        modifiableItem.kjs$getMutableAttributeMap().put(attribute, modifier);
                    }
                });
            }

            return item;
        }

        private SanityEquippedSourceDefinition getOrCreateSourceDefinition() {
            if (sourceDefinition == null) {
                sourceDefinition = sourceBuilder.build();
            }

            return sourceDefinition;
        }
    }

    public static class HelmetBuilder extends Builder<HelmetBuilder> {
        public HelmetBuilder(ResourceLocation id) {
            super(id, EquipmentSlot.HEAD);
        }

        @Override
        protected HelmetBuilder self() {
            return this;
        }
    }

    public static class ChestplateBuilder extends Builder<ChestplateBuilder> {
        public ChestplateBuilder(ResourceLocation id) {
            super(id, EquipmentSlot.CHEST);
        }

        @Override
        protected ChestplateBuilder self() {
            return this;
        }
    }

    public static class LeggingsBuilder extends Builder<LeggingsBuilder> {
        public LeggingsBuilder(ResourceLocation id) {
            super(id, EquipmentSlot.LEGS);
        }

        @Override
        protected LeggingsBuilder self() {
            return this;
        }
    }

    public static class BootsBuilder extends Builder<BootsBuilder> {
        public BootsBuilder(ResourceLocation id) {
            super(id, EquipmentSlot.FEET);
        }

        @Override
        protected BootsBuilder self() {
            return this;
        }
    }
}
