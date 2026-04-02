package work.crash.skyraah.sanityjs.event;

import dev.latvian.mods.kubejs.bindings.event.ItemEvents;
import dev.latvian.mods.kubejs.event.Extra;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import net.minecraft.world.item.Item;

/**
 * @author skyraah
 */
public interface SanityEvents {
    EventGroup GROUP = EventGroup.of("SanityEvents");
    Extra SUPPORTS_TRIGGER = new Extra()
            .toString(extraId -> {
                SanityEventType type = SanityEventType.coerce(extraId);
                return type == null ? null : type.id();
            })
            .identity()
            .required()
            .validator(extraId -> SanityEventType.coerce(extraId) != null)
            .describeType(context -> context.javaType(SanityEventType.class));
    Extra SUPPORTS_ITEM = new Extra()
            .transformer(ItemEvents.SUPPORTS_ITEM.transformer)
            .toString(ItemEvents.SUPPORTS_ITEM.toString)
            .validator(ItemEvents.SUPPORTS_ITEM.validator)
            .identity()
            .required()
            .describeType(context -> context.javaType(Item.class));

    EventHandler SAN_CHANGE = GROUP.server("change", () -> SanityChangeEventJS.class).hasResult();
    EventHandler TRIGGER = GROUP.server("trigger", () -> SanityTriggerEventJS.class).extra(SUPPORTS_TRIGGER).hasResult();
    EventHandler ITEM_SOURCE = GROUP.server("itemSource", () -> SanityItemSourceEventJS.class).extra(SUPPORTS_ITEM).hasResult();
    EventHandler EQUIPPED_SOURCE = GROUP.server("equippedSource", () -> SanityEquippedSourceEventJS.class).extra(SUPPORTS_ITEM).hasResult();
    EventHandler TICK = GROUP.server("tick", () -> SanityTickEventJS.class).hasResult();
    EventHandler INDICATOR_RENDER = GROUP.client("indicatorRender", () -> SanityIndicatorRenderEventJS.class).hasResult();
}
