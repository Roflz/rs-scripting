public enum StoreItems {
    COMPOST ("Compost", 17, 10, 6033),
    SUPERCOMPOST ("Supercompost", 18, 11, 6035),
    ULTRACOMPOST ("Ultracompost", 19, 12, 21484),
    PLANT_CURE ("Plant cure", 14, 7, 6037);

    private final String name;
    private final int widgetID;
    private final int inventoryWidgetID;
    private final int notedID;

    StoreItems(String name, int widgetID, int inventoryWidgetID, int notedID) {
        this.name = name;
        this.widgetID = widgetID;
        this.inventoryWidgetID = inventoryWidgetID;
        this.notedID = notedID;
    }

    public static StoreItems getItemfromName(String itemName) {
        for(StoreItems item : StoreItems.values()) {
            if(item.getName().toLowerCase().contains(itemName.toLowerCase())) {
                return item;
            }
        }
        return null;
    }

    public int getNotedID() {
        return notedID;
    }

    public int getInventoryWidgetID() {
        return inventoryWidgetID;
    }

    public String getName() {
        return name;
    }

    public int getWidgetID() {
        return widgetID;
    }
}