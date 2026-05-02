package utils;

public final class ProductNavigationState {
    private static String selectedType = "tous";

    private ProductNavigationState() {
    }

    public static void setSelectedType(String type) {
        selectedType = (type == null || type.isBlank()) ? "tous" : type;
    }

    public static String consumeSelectedType() {
        String value = selectedType;
        selectedType = "tous";
        return value;
    }
}
