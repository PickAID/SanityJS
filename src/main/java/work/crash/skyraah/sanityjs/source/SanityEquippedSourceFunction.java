package work.crash.skyraah.sanityjs.source;

import work.crash.skyraah.sanityjs.event.SanityEquippedSourceEventJS;

@FunctionalInterface
public interface SanityEquippedSourceFunction {
    Float apply(SanityEquippedSourceEventJS event);
}
