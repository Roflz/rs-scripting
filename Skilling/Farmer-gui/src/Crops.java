import java.util.ArrayList;
import java.util.List;

public enum Crops {
    //Allotments
    POTATO ("Potato seed", "Potato", "Diseased potatoes", "Dead potatoes", "Allotment", "Potato", 1942, 1943),
    ONION ("Onion seed", "Onion", "Diseased onions", "Dead onions", "Allotment", "Onion", 1957, 1958),
    CABBAGE ("Cabbage seed", "Cabbage", "Diseased cabbage", "Dead cabbage", "Allotment", "Cabbage", 1965, 1966),
    TOMATO ("Tomato seed", "Tomato", "Diseased tomatoes", "Dead tomatoes", "Allotment", "Tomato",1982, 1983),
    SWEETCORN ("Sweetcorn seed", "Sweetcorn", "Diseased sweetcorn", "Dead sweetcorn", "Allotment", "Sweetcorn",5986, 5987),
    STRAWBERRY ("Strawberry seed", "Strawberry", "Diseased strawberries", "Dead strawberries", "Allotment", "Strawberr", 5504, 5505),
    WATERMELON ("Watermelon seed", "Watermelon", "Diseased watermelons", "Dead watermelons", "Allotment", "Watermelon", 5982, 5983),
    SNAPE_GRASS ("Snape grass seed", "Snape grass", "Diseased snape grass", "Dead snape grass", "Allotment", "Snape",231, 232),
    //Flowers
    MARIGOLD ("Marigold seed", "Marigold", "Diseased marigold", "Dead marigold", "Flower Patch", "Marigold", 6010, 6011),
    ROSEMARY ("Rosemary seed", "Rosemary", "Diseased rosemary", "Dead rosemary", "Flower Patch", "Rosemary",6014, 6015),
    NASTURTIUMS ("Nasturtium seed", "Nasturtiums", "Diseased nasturtiums", "Dead nasturtiums", "Flower Patch", "Nasturt", 6012, 6013),
    WOAD ("Woad seed", "Woad leaf", "Diseased woad", "Dead woad", "Flower Patch", "Woad", 1793, 1794),
    LIMPWURT ("Limpwurt seed", "Limpwurt root", "Diseased limpwurt", "Dead limpwurt", "Flower Patch", "Limpwurt", 225, 226),
    WHITE_LILY ("White lily seed", "White lily", "Diseased white lilies", "Dead white lilies", "Flower Patch", "White lil", 22932, 22933),
    //Herbs
    GUAM ("Guam seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", "Herb",199, 200),
    MARRENTILL ("Marrentill seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", "Herb", 201, 202),
    TARROMIN ("Tarromin seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", "Herb", 203, 204),
    HARRALANDER ("Harralander seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", "Herb",205, 206),
    RANARR ("Ranarr seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", "Herb", 207, 208),
    TOADFLAX ("Toadflax seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", "Herb", 3049, 3050),
    IRIT ("Irit seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", "Herb", 209, 210),
    AVANTOE ("Avantoe seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", "Herb", 211, 212),
    KWUARM ("Kwuarm seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", "Herb", 213, 214),
    SNAPDRAGON ("Snapdragon seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", "Herb", 3051, 3052),
    CADANTINE ("Cadantine seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", "Herb", 215, 216),
    LANTADYME ("Lantadyme seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", "Herb", 2485, 2486),
    DWARF_WEED ("Dwarf weed seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", "Herb",217, 218),
    TORSTOL ("Torstol seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", "Herb", 219, 220),
    //Hops
    BARLEY ("Barley seed", "Barley", "Diseased Barley", "Dead Barley", "Hops Patch", "Barley",6006, 6007),
    HAMMERSTONE ("Hammerstone seed", "Hammerstone hops", "Diseased Hammerstone hops", "Dead Hammerstone hops", "Hops Patch", "Hammerstone", 5994, 5995),
    ASGARNIAN ("Asgarnian seed", "Asgarnian hops", "Diseased Asgarnian hops", "Dead Asgarnian hops", "Hops Patch", "Asgarnian", 5996, 5997),
    JUTE ("Jute seed", "Jute fibre", "Diseased Jute", "Dead Jute", "Hops Patch", "Jute", 5931, 5932),
    YANILLIAN ("Yanillian seed", "Yanillian hops", "Diseased Yanillian hops", "Dead Yanillian hops", "Hops Patch", "Yanill", 5998, 5999),
    KRANDORIAN ("Krandorian seed", "Krandorian hops", "Diseased Krandorian hops", "Dead Krandorian hops", "Hops Patch", "Krandor", 6000, 6001),
    WILDBLOOD ("Wildblood seed", "Wildblood hops", "Diseased Wildblood hops", "Dead Wildblood hops", "Hops Patch", "Wildblood", 6002, 6003);

    private final String seedName;
    private final String cropName;
    private final String diseasedName;
    private final String deadName;
    private final String patchType;
    private final String cropIdentifier;
    private final int unNotedID;
    private final int notedID;

    Crops(String seedName, String cropName, String diseasedName, String deadName, String patchType, String cropIdentifier, int unNotedID, int notedID) {
        this.seedName = seedName;
        this.cropName = cropName;
        this.diseasedName = diseasedName;
        this.deadName = deadName;
        this.patchType = patchType;
        this.cropIdentifier = cropIdentifier;
        this.unNotedID = unNotedID;
        this.notedID = notedID;
    }

    public String getCropName() {
        return cropName;
    }

    public int getUnNotedID() {
        return unNotedID;
    }

    public String getPatchType() {
        return patchType;
    }

    public String getSeedName() {
        return seedName;
    }

    public static List<Integer> getUnNotedIDs() {
        List<Integer> unNotedIDs = new ArrayList<Integer>();
        for(Crops crop : Crops.values()) {
            unNotedIDs.add(crop.unNotedID);
        }
        return unNotedIDs;
    }

    public static Crops getCropfromName(String cropName) {
        for(Crops crop : Crops.values()) {
            if(crop.getSeedName().toLowerCase().contains(cropName.toLowerCase())) {
                return crop;
            }
        }
        return null;
    }

    public static List<String> getIdentifiers(String value) {
        List<String> identifiers = new ArrayList<String>();
        for(Crops crop : Crops.values()) {
            if(crop.patchType.contains(value)) {
                identifiers.add(crop.cropIdentifier);
            }
        }
        return identifiers;
    }

    public static List<String> getDiseasedNames(String patchType) {
        List<String> diseasedNames = new ArrayList<String>();
        for(Crops crop : Crops.values()) {
            if(crop.patchType == patchType) {
                diseasedNames.add(crop.diseasedName);
            }
        }
        return diseasedNames;
    }

    public static List<String> getDeadNames(String patchType) {
        List<String> deadNames = new ArrayList<String>();
        for(Crops crop : Crops.values()) {
            if(crop.patchType == patchType) {
                deadNames.add(crop.deadName);
            }
        }
        return deadNames;
    }


}
