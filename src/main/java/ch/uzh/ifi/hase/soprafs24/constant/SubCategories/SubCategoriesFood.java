package ch.uzh.ifi.hase.soprafs24.constant.SubCategories;

public enum SubCategoriesFood {
    ITALIAN_CUISINE("Italian Cuisine"),
    FRENCH_CUISINE("French Cuisine"),
    CHINESE_CUISINE("Chinese Cuisine"),
    INDIAN_CUISINE("Indian Cuisine"),
    MEXICAN_CUISINE("Mexican Cuisine"),
    BAKING("Baking"),
    VEGETARIAN_COOKING("Vegetarian Cooking"),
    SEAFOOD("Seafood"),
    FAST_FOOD("Fast Food"),
    GOURMET_COOKING("Gourmet Cooking"),
    GRILLING("Grilling"),
    FRYING("Frying"),
    BREAKFAST("Breakfast"),
    WINE("Wine"),
    LUXURY_FOOD("Luxury food"),
    HERBS_SPICES("Herbs and spices"); 




    private final String displayName;

    // Constructor
    SubCategoriesFood(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
