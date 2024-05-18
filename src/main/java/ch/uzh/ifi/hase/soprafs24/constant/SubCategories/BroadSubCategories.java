package ch.uzh.ifi.hase.soprafs24.constant.SubCategories;

public enum BroadSubCategories {
    GARDENING("Gardening"),
    WORK("Work"),
    CARS("Cars"),
    EDUCATION("Education"),
    SPORTS("Sports"),
    FASHION("Fashion"),
    HEALTH("Health"),
    ENTERTAINMENT("Entertainment"),
    TRAVEL("Travel"),
    HOME_LIVING("Home Living"),
    FINANCE("Finance"),
    ARTS("Arts"),
    MUSIC("Music"),
    PETS("Pets"),
    CULTURE("Culture"),
    POLITICS("Politics"),
    RELIGION("Religion"),
    NATURE("Nature"),
    INSECTS("Insects"),
    HOBBIES("Hobbies");


    private final String displayName;

    // Constructor
    BroadSubCategories(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
