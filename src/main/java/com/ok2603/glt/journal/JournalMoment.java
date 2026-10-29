package com.ok2603.glt.journal;

/**
 * Moments that write themselves into Chito's journal the first time they happen.
 * Each one has a line in en_us.json: "journal.glt.<id>".
 * "Together" moments only count when the other girl is close by (in single player they always count).
 */
public enum JournalMoment {
    // Food
    RATION("ration", false),
    CHOCOLATE("chocolate", false),
    FISH("fish", false),
    SOUP("soup", false),
    BEER("beer", true),
    TANK_CHOCOLATE("tank_chocolate", false),
    // The road
    CAMP("camp", true),
    SLEEP_HOME("sleep_home", true),
    OUT_OF_FUEL("out_of_fuel", false),
    PUSHING("pushing", false),
    BREAKDOWN("breakdown", false),
    REPAIR("repair", false),
    // The cold and the sky
    SNOW("snow", false),
    FREEZING("freezing", false),
    HUDDLE("huddle", true),
    STARS("stars", true),
    HIGH_PLACE("high_place", false),
    // Nuko and the rifle
    RIFLE("rifle", false),
    NUKO("nuko", false),
    NUKO_RADIO("nuko_radio", true),
    // Small moments
    BATH("bath", true),
    RAIN_MUSIC("rain_music", true),
    FOOD_FACTORY("food_factory", false),
    OFFERING("offering", false),
    CAIRN("cairn", false),
    STREET_LAMPS("street_lamps", true),
    PHOTO("photo", true),
    // The city
    ELEVATOR("elevator", true),
    NEW_FLOOR("new_floor", true),
    OLD_SIGN("old_sign", false),
    // Paper
    OLD_NOTE("old_note", false),
    BURN_BOOK("burn_book", false),
    JOURNAL_BURNED("journal_burned", false);

    private final String id;
    private final boolean together;

    JournalMoment(String id, boolean together) {
        this.id = id;
        this.together = together;
    }

    public String id() {
        return id;
    }

    /** True if both girls have to be there for this moment to count. */
    public boolean together() {
        return together;
    }

    public String translationKey() {
        return "journal.glt." + id;
    }

    public static JournalMoment byId(String id) {
        for (JournalMoment moment : values()) {
            if (moment.id.equals(id)) {
                return moment;
            }
        }
        return null;
    }
}
