package work.crash.skyraah.sanityjs.event;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public enum SanityEventType {
    PLAYER_HURT("playerHurt"),
    PLAYER_HURT_ANIMAL("playerHurtAnimal"),
    PLAYER_PET_DEATH("playerPetDeath"),
    PLAYER_ENDERMAN_ANGERED("playerEnderManAngered"),
    PLAYER_ADVANCEMENT("playerAdvancement"),
    PLAYER_BRED_ANIMALS("playerBredAnimals"),
    PLAYER_TRADED_WITH_VILLAGER("playerTradedWithVillager"),
    PLAYER_USED_SHEARS("playerUsedShears"),
    PLAYER_SPAWNED_CHICKEN("playerSpawnedChicken"),
    PLAYER_USED_ITEM("playerUsedItem"),
    PLAYER_FISHED_ITEM("playerFishedItem"),
    PLAYER_MINED_BLOCK("playerMinedBlock"),
    PLAYER_TRAMPLED_FARMLAND("playerTrampledFarmland"),
    PLAYER_POTTED_FLOWER("playerPottedFlower"),
    PLAYER_CHANGED_DIMENSIONS("playerChangedDimensions"),
    PLAYER_STRUCK_BY_LIGHTNING("playerStruckByLightning");

    private static final Set<String> IDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.stream(values()).map(SanityEventType::id).toList()));

    private final String id;

    SanityEventType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public boolean matches(String value) {
        return id.equals(value);
    }

    public static SanityEventType fromId(String value) {
        if (value == null) {
            return null;
        }

        for (SanityEventType type : values()) {
            if (type.id.equals(value)) {
                return type;
            }
        }

        return null;
    }

    public static SanityEventType coerce(Object value) {
        if (value instanceof SanityEventType type) {
            return type;
        }

        if (value instanceof String stringValue) {
            return fromId(stringValue);
        }

        return null;
    }

    public static boolean isValid(String value) {
        return fromId(value) != null;
    }

    public static Set<String> ids() {
        return IDS;
    }

    @Override
    public String toString() {
        return id;
    }
}
