package work.crash.skyraah.sanityjs.source;

import work.crash.skyraah.sanityjs.event.SanityItemSourceEventJS;

@FunctionalInterface
public interface SanityItemSourceFunction {
    Float apply(SanityItemSourceEventJS event);
}
