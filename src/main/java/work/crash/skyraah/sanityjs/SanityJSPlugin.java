package work.crash.skyraah.sanityjs;

import croissantnova.sanitydim.ActiveSanitySources;
import croissantnova.sanitydim.SanityProcessor;
import croissantnova.sanitydim.config.ConfigProxy;
import croissantnova.sanitydim.config.SanityIndicatorLocation;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import work.crash.skyraah.sanityjs.event.SanityEventType;
import work.crash.skyraah.sanityjs.event.SanityEvents;
import work.crash.skyraah.sanityjs.item.SanityPassiveArmorItem;
import work.crash.skyraah.sanityjs.item.SanitySourceItem;
import work.crash.skyraah.sanityjs.source.SanityEquippedSourceBuilder;
import work.crash.skyraah.sanityjs.source.SanityEquippedSourceDefinition;
import work.crash.skyraah.sanityjs.source.SanityItemSourceBuilder;
import work.crash.skyraah.sanityjs.source.SanityItemSourceDefinition;
import work.crash.skyraah.sanityjs.util.SanityHelper;

/**
 * @author skyraah
 */
public class SanityJSPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        RegistryInfo.ITEM.addType("sanity_source", SanitySourceItem.Builder.class, SanitySourceItem.Builder::new);
        RegistryInfo.ITEM.addType("sanity_helmet", SanityPassiveArmorItem.HelmetBuilder.class, SanityPassiveArmorItem.HelmetBuilder::new);
        RegistryInfo.ITEM.addType("sanity_chestplate", SanityPassiveArmorItem.ChestplateBuilder.class, SanityPassiveArmorItem.ChestplateBuilder::new);
        RegistryInfo.ITEM.addType("sanity_leggings", SanityPassiveArmorItem.LeggingsBuilder.class, SanityPassiveArmorItem.LeggingsBuilder::new);
        RegistryInfo.ITEM.addType("sanity_boots", SanityPassiveArmorItem.BootsBuilder.class, SanityPassiveArmorItem.BootsBuilder::new);
    }

    @Override
    public void registerEvents() {
        SanityEvents.GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("ActiveSanitySources", ActiveSanitySources.class);
        event.add("SanityConfig", ConfigProxy.class);
        event.add("SanityEventType", SanityEventType.class);
        event.add("SanityHelper", SanityHelper.class);
        event.add("SanityIndicatorLocation", SanityIndicatorLocation.class);
        event.add("SanityEquippedSource", SanityEquippedSourceDefinition.class);
        event.add("SanityEquippedSourceBuilder", SanityEquippedSourceBuilder.class);
        event.add("SanityItemSource", SanityItemSourceDefinition.class);
        event.add("SanityItemSourceBuilder", SanityItemSourceBuilder.class);
        event.add("SanityPassiveArmorItem", SanityPassiveArmorItem.class);
        event.add("SanityProcessor", SanityProcessor.class);
        event.add("SanitySourceItem", SanitySourceItem.class);
    }

    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        super.registerClasses(type, filter);
    }

    @Override
    public void clearCaches() {
        // These registries are populated from startup item builders and startup scripts.
        // Clearing them during later KubeJS reload phases wipes the active source definitions
        // while leaving the event handlers alive, which makes items/armor stop affecting sanity.
    }
}
