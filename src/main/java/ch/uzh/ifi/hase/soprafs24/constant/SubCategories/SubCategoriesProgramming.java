package ch.uzh.ifi.hase.soprafs24.constant.SubCategories;

public enum SubCategoriesProgramming {
    SOFTWARE_DEVELOPMENT("Software Development"),
    DATA_SCIENCE("Data Science"),
    MACHINE_LEARNING("Machine Learning"),
    WEB_DEVELOPMENT("Web Development"),
    CYBERSECURITY("Cybersecurity"),
    DATABASE_MANAGEMENT("Database Management"),
    SYSTEMS_ARCHITECTURE("Systems Architecture"),
    GAME_DEVELOPMENT("Game Development"),
    CLOUD_COMPUTING("Cloud Computing"),
    DEPLOYMENT("Deployment"),
    NETWORK_ENGINEERING("Network Engineering");

    private final String displayName;

    // Constructor
    SubCategoriesProgramming(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}