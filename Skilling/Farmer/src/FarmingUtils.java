import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.entity.WidgetChild;
import com.epicbot.api.shared.entity.WidgetGroup;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.util.time.Time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FarmingUtils {


    private static APIContext stx = APIContext.get();

    public static List<String> trash = Arrays.asList("Weeds", "Bucket", "Vial");
    protected static Crops allotmentCrop;
    protected static Crops flowerCrop;
    protected static Crops herbCrop;
    protected static Crops hopsCrop;
    protected static Crops bushCrop;
    protected static Crops cactusCrop;
    public static List<Crops> crops = new ArrayList<Crops>();

    public static StoreItems compostType = StoreItems.ULTRACOMPOST;
    static boolean harvesting = false;
    static boolean clearing = false;
    public static boolean farmAllotments;
    public static boolean farmHerbs;
    public static boolean farmFlowers;
    public static boolean farmHops;
    public static boolean farmBushes;
    public static boolean farmCacti;

    public static void initCropsList() {
        crops.add(allotmentCrop);
        crops.add(flowerCrop);
        crops.add(herbCrop);
        crops.add(hopsCrop);
        crops.add(bushCrop);
        crops.add(cactusCrop);
    }

    public static void updateCropsList() {
        crops.set(0, allotmentCrop);
        crops.set(1, flowerCrop);
        crops.set(2, herbCrop);
        crops.set(3, hopsCrop);
        crops.set(4, bushCrop);
        crops.set(5, cactusCrop);
    }

    public static List<String> cropsListToSeedNames() {
        List<String> names = new ArrayList<>();
        for(int i = 0 ; i < crops.size() ; i++) {
            names.add(crops.get(i).getSeedName());
        }
        return names;
    }

    public static List<Integer> cropsListToSeedQuantities() {
        List<Integer> quantities = new ArrayList<>();
        for(Crops crop: crops) {
            if(crop.getPatchType() == "Allotment" || crop.getPatchType() == "Hops Patch") {
                quantities.add(50);
            }
            if(crop.getPatchType() == "Herb patch" || crop.getPatchType() == "Flower Patch" || crop.getPatchType() == "Bush Patch" || crop.getPatchType() == "Cactus patch") {
                quantities.add(10);
            }
        }
        return quantities;
    }

    public static void farm(Area area, List<Crops> cropList, StoreItems compostType) {
        if (GenUtils.playerisInArea(area)) {
            List<SceneObject> patches = getPatches();
            if(!isFarming()) {
                System.out.println("Farming");
                if(patches.get(0) != null) {
                    if(!patches.get(0).hasAction("Harvest", "Pick", "Pick-from", "Pick-spine")) {
                        harvesting = false;
                    }
                    if(!patches.get(0).hasAction("Clear")) {
                        clearing = false;
                    }
                }
                if(stx.objects().query().named("Jangerberry bush", "Cactus", "Strawberry", "Limpwurt plant", "Dead strawberry plant", "Dead limpwurt plant", "Diseased strawberry plant", "Diseased limpwurt plant", "Allotment", "Flower Patch").results().nearest() != null) {
                    if(stx.objects().query().named("Jangerberry bush", "Cactus", "Strawberry", "Limpwurt plant", "Dead strawberry plant", "Dead limpwurt plant", "Diseased strawberry plant", "Diseased limpwurt plant", "Allotment", "Flower Patch").results().nearest().getName().contains("Jangerberry bush") || stx.objects().query().named("Jangerberry bush", "Cactus", "Strawberry", "Limpwurt plant", "Dead strawberry plant", "Dead limpwurt plant", "Diseased strawberry plant", "Diseased limpwurt plant", "Allotment", "Flower Patch").results().nearest().getName().contains("Cactus")) {
                        if(!stx.objects().query().named("Jangerberry bush", "Cactus", "Strawberry", "Limpwurt plant").results().nearest().hasAction("Pick-from", "Pick-spine")) {
                            harvesting = false;
                        }
                    }
                }
                if(stx.inventory().isFull()) {
                    noteProduce(Crops.getUnNotedIDs());
                } else if(patches.get(0) != null && patches.get(0).hasAction("Rake" ) && harvesting == false && clearing == false) {
                    rake(patches.get(0));
                } else if(patches.get(0) != null && !patches.get(0).hasAction("Rake") && harvesting == false && clearing == false) {
                    compost(patches.get(0), compostType);
                    if(!stx.grandExchange().isOpen() && stx.widgets().get(162).getChild(56).getChild(0).getText().contains("with ultracompost")) {
                        plantFromListInPatch(cropList, patches.get(0));
                    }
                } else if(patches.get(1) != null && patches.get(1).hasAction("Check-health") && harvesting == false && clearing == false) {
                    checkHealth(patches.get(1));
                } else if(patches.get(1) != null && patches.get(1).hasAction("Harvest", "Pick", "Pick-from", "Pick-spine") && harvesting == false && clearing == false) {
                    if(patches.get(1).getName().toLowerCase().contains("limpwurt")) {
                        noteProduce(Crops.getUnNotedIDs());
                    }
                    harvest(patches.get(1));
                } else if(patches.get(2) != null && patches.get(2).hasAction("Cure") && harvesting == false && clearing == false) {
                    cure(patches.get(2));
                } else if(patches.get(3) != null && patches.get(3).hasAction("Clear") && harvesting == false && clearing == false) {
                    System.out.println("Clearing");
                    clear(patches.get(3));
                }
                System.out.println("Done Farming");
            }
        }
    }

    public static boolean patchesAreFarmable() {
        List<SceneObject> allPatches;
        if(main.watDo == "Farm Lumbridge" || main.watDo == "Farm Entrana") {
            allPatches = stx.objects().query().actions("Cure", "Pick", "Harvest", "Rake", "Clear").notNamed("Wheat", "Cabbage", "Potato").results().toList();
        } else if(main.watDo == "Farm Farming Guild East") {
            allPatches = stx.objects().query().actions("Pick-from", "Pick-spine", "Check-health", "Cure", "Pick", "Harvest", "Rake", "Clear").notNamed("Wheat", "Cabbage", "Anima patch", "Spirit Tree Patch", "Celastrus patch", "Tree patch", "Fruit Tree Patch", "Redwood tree patch", "Herb patch", "Herbs", "Diseased herbs", "Dead herbs", "Spirit Tree", "Harvested Celastrus tree", "Redwood tree", "Redwood tree patch").results().toList();
        } else if(main.watDo == "Farm Farming Guild West") {
            allPatches = stx.objects().query().actions("Pick-from", "Pick-spine", "Check-health", "Cure", "Pick", "Harvest", "Rake", "Clear").notNamed("Wheat", "Cabbage", "Anima patch", "Spirit Tree Patch", "Celastrus patch", "Tree patch", "Fruit Tree Patch", "Redwood tree patch", "Flower Patch", "Limpwurt plant", "Diseased limpwurt plant", "Dead limpwurt plant", "Allotment", "Strawberry plant", "Diseased strawberry plant", "Dead strawberry plant", "Strawberries", "Bush Patch", "Jangerberries", "Diseased jangerberry bush", "Dead jangerberry bush", "Cactus", "Cactus patch", "Diseased cactus", "Dead cactus", "Spirit Tree", "Harvested Celastrus tree", "Redwood tree", "Redwood tree patch").results().toList();
        } else {
            allPatches = stx.objects().query().actions("Pick-from", "Pick-spine", "Check-health", "Cure", "Pick", "Harvest", "Rake", "Clear").notNamed("Wheat", "Cabbage").results().toList();
        }
        System.out.println(allPatches.size());
        System.out.println(allPatches);
        if(farmAllotments == false) {
            allPatches = parseFarmablePatches("Allotment", allPatches);
        }
        if(farmHerbs == false) {
            allPatches = parseFarmablePatches("Herb patch", allPatches);
        }
        if(farmFlowers == false) {
            allPatches = parseFarmablePatches("Flower Patch", allPatches);
        }
        if(farmHops == false) {
            allPatches = parseFarmablePatches("Hops Patch", allPatches);
        }
        if(farmBushes == false) {
            allPatches = parseFarmablePatches("Bush Patch", allPatches);
        } else {
            for(int i = allPatches.size() - 1 ; i >= 0 ; i--) {
                if(allPatches.get(i).hasAction("Clear") && !allPatches.get(i).hasAction("Pick-from") && allPatches.get(i).getName().contains(bushCrop.getCropIdentifier()) && !allPatches.get(i).getName().contains(bushCrop.getDeadName())) {
                    allPatches.remove(i);
                }
            }
        }
        if(farmCacti == false) {
            allPatches = parseFarmablePatches("Cactus patch", allPatches);
        } else {
            for(int i = allPatches.size() - 1 ; i >= 0 ; i--) {
                if(allPatches.get(i).hasAction("Clear") && !allPatches.get(i).hasAction("Pick-spine") && allPatches.get(i).getName().contains(cactusCrop.getCropIdentifier()) && !allPatches.get(i).getName().contains(cactusCrop.getDeadName())) {
                    allPatches.remove(i);
                }
            }
        }
        System.out.println(allPatches.size());
        System.out.println(allPatches);
        if(stx.objects().query().named("Allotment").results().nearest() != null && farmAllotments == true) {
            allPatches.add(stx.objects().query().named("Allotment").results().nearest());
        }
        if(stx.objects().query().named("Flower Patch").results().nearest() != null && farmFlowers == true) {
            allPatches.add(stx.objects().query().named("Flower patch").results().nearest());
        }
        if(main.watDo != "Farm Farming Guild East") {
            if(stx.objects().query().named("Herb patch").results().nearest() != null && farmHerbs == true) {
                allPatches.add(stx.objects().query().named("Herb patch").results().nearest());
            }
        }
        if(stx.objects().query().named("Hops patch").results().nearest() != null && farmHops == true) {
            allPatches.add(stx.objects().query().named("Hops patch").results().nearest());
        }
        if(stx.objects().query().named("Bush Patch").results().nearest() != null && farmBushes == true) {
            allPatches.add(stx.objects().query().named("Bush Patch").results().nearest());
        }
        if(stx.objects().query().named("Cactus patch").results().nearest() != null && farmCacti == true) {
            allPatches.add(stx.objects().query().named("Cactus patch").results().nearest());
        }
        System.out.println(allPatches.size());
        System.out.println(allPatches);
        if(allPatches.isEmpty()) {
            System.out.println("Patches are farmed :)");
            return false;
        } else {
            return true;
        }
    }

    private static List<SceneObject> parseFarmablePatches(String patchType, List<SceneObject> patches) {
        for(int i = patches.size() - 1 ; i >= 0 ; i--) {
            for(String cropName : Crops.getIdentifiers(patchType)) {
                if(patches.get(i).getName().contains(cropName) || patches.get(i).getName().contains(patchType)) {
                    patches.remove(i);
                    break;
                }
            }
        }
        return patches;
    }

    public static SceneObject parseProduceList(String patchType, List<SceneObject> patchList) {
        if(patchList.size() > 0) {
            for(int i = patchList.size() - 1 ; i >= 0 ; i--) {
                for(String crop : Crops.getIdentifiers(patchType)) {
                    if(patchList.get(i).getName().toLowerCase().contains(crop.toLowerCase())) {
                        patchList.remove(i);
                        break;
                    }
                }
            }
        }
        if(patchList.size() > 0) {
            return patchList.get(0);
        } else {
            return null;
        }
    }

    public static SceneObject parsePatchList(String patchType, List<SceneObject> patchList) {
        if(patchList.size() > 0) {
            for(int i = patchList.size() - 1 ; i >= 0 ; i--) {
                if(patchList.get(i).getName().contains(patchType)) {
                    patchList.remove(i);
                }
            }
        }
        if(patchList.size() > 0) {
            return patchList.get(0);
        } else {
            return null;
        }
    }

    public static SceneObject parseDiseasedList(String patchType, List<SceneObject> patchList) {
        if(patchList.size() > 0) {
            for(int i = patchList.size() - 1 ; i >= 0 ; i--) {
                for(String crop : Crops.getDiseasedNames(patchType)) {
                    if(patchList.get(i).getName().toLowerCase().contains(crop.toLowerCase())) {
                        patchList.remove(i);
                        break;
                    }
                }
            }
        }
        if(patchList.size() > 0) {
            return patchList.get(0);
        } else {
            return null;
        }
    }

    public static SceneObject parseDeadList(String patchType, List<SceneObject> patchList) {
        if(patchList.size() > 0) {
            for(int i = patchList.size() - 1 ; i >= 0 ; i--) {
                for(String crop : Crops.getDeadNames(patchType)) {
                    if(patchList.get(i).getName().toLowerCase().contains(crop.toLowerCase())) {
                        patchList.remove(i);
                        break;
                    }
                }
            }
        }
        if(patchList.size() > 0) {
            return patchList.get(0);
        } else {
            return null;
        }
    }

    private static SceneObject getPatchZero(List<SceneObject> patches) {
        if(patches.size() > 0) {
            return patches.get(0);
        } else {
            return null;
        }
    }

    public static List<SceneObject> getPatches() {
        List<SceneObject> patchList = stx.objects().query().named("Allotment", "Flower Patch", "Herb patch", "Hops patch", "Cactus patch", "Bush Patch").results().nearestList();
        List<SceneObject> produceList;
        if(main.watDo == "Farm Lumbridge") {
            produceList = stx.objects().query().actions("Harvest", "Pick").notNamed("Wheat", "Cabbage", "Potato").results().nearestList();
        } else {
            produceList = stx.objects().query().actions("Harvest", "Pick", "Pick-from", "Pick-spine", "Check-health").notNamed("Wheat", "Cabbage", "Nettles").results().nearestList();
        }
        List<SceneObject> diseasedProduceList = stx.objects().query().actions("Cure").results().nearestList();
        List<SceneObject> deadProduceList = stx.objects().query().actions("Clear").notNamed("Jangerberry bush", "Cactus").results().nearestList();
        List<SceneObject> patches = Arrays.asList(getPatchZero(patchList), getPatchZero(produceList), getPatchZero(diseasedProduceList), getPatchZero(deadProduceList));
        if(!farmAllotments || main.watDo == "Farm Farming Guild West") {
            patches.set(0, parsePatchList("Allotment", patchList));
            patches.set(1, parseProduceList("Allotment", produceList));
            patches.set(2, parseDiseasedList("Allotment", diseasedProduceList));
            patches.set(3, parseDeadList("Allotment", deadProduceList));
        }
        if(!farmHerbs || main.watDo == "Farm Farming Guild East") {
            patches.set(0, parsePatchList("Herb patch", patchList));
            patches.set(1, parseProduceList("Herb patch", produceList));
            patches.set(2, parseDiseasedList("Herb patch", diseasedProduceList));
            patches.set(3, parseDeadList("Herb patch", deadProduceList));
        }
        if(!farmFlowers || main.watDo == "Farm Farming Guild West") {
            patches.set(0, parsePatchList("Flower Patch", patchList));
            patches.set(1, parseProduceList("Flower Patch", produceList));
            patches.set(2, parseDiseasedList("Flower Patch", diseasedProduceList));
            patches.set(3, parseDeadList("Flower Patch", deadProduceList));
        }
        if(!farmHops) {
            patches.set(0, parsePatchList("Hops patch", patchList));
            patches.set(1, parseProduceList("Hops patch", produceList));
            patches.set(2, parseDiseasedList("Hops patch", diseasedProduceList));
            patches.set(3, parseDeadList("Hops patch", deadProduceList));
        }
        if(!farmBushes || main.watDo == "Farm Farming Guild West") {
            patches.set(0, parsePatchList("Bush Patch", patchList));
            patches.set(1, parseProduceList("Bush Patch", produceList));
            patches.set(2, parseDiseasedList("Bush Patch", diseasedProduceList));
            patches.set(3, parseDeadList("Bush Patch", deadProduceList));
        }
        if(!farmCacti || main.watDo == "Farm Farming Guild West") {
            patches.set(0, parsePatchList("Cactus patch", patchList));
            patches.set(1, parseProduceList("Cactus patch", produceList));
            patches.set(2, parseDiseasedList("Cactus patch", diseasedProduceList));
            patches.set(3, parseDeadList("Cactus patch", deadProduceList));
        }
        return patches;
    }

    public static void getItemFromLeprechaun(StoreItems item, int numberOfItems) {
        NPC leprechaun;
        if(main.watDo == "Farm Farming Guild West") {
            leprechaun = stx.npcs().query().named("Tool Leprechaun").within(Constants.FARMING_GUILD_WEST_LEPRECHAUN).results().nearest();
        } else if(main.watDo == "Farm Farming Guild East") {
            leprechaun = stx.npcs().query().named("Tool Leprechaun").within(Constants.FARMING_GUILD_EAST_LEPRECHAUN).results().nearest();
        } else {
            leprechaun = stx.npcs().query().named("Tool Leprechaun").results().nearest();
        }
        GenUtils.turnCameraTowards(leprechaun);
        leprechaun.interact("Exchange");
        Time.sleep(10_000, () -> stx.widgets().isInterfaceOpen());
        WidgetChild itemWidget = getLeprechaunStoreItem((item.getWidgetID()));
        if(amountOfItemLeft(item).contentEquals("0/1000")) {
            if(stx.inventory().contains(item.getNotedID())) {
                WidgetChild inventoryWidget = stx.widgets().get(126).getChild(item.getInventoryWidgetID());
                inventoryWidget.interact("Store-All");
            } else {
                System.out.println("Going to get more " + item.getName());
                stx.widgets().closeInterface();
                Time.sleep(1_000, 5_000, () -> !stx.widgets().isInterfaceOpen());
                getMore(item);
            }
        }
        if(!stx.grandExchange().isOpen()) {
            for(int i = 1; i <= numberOfItems; i++) {
                itemWidget.interact("Remove-1");
                Time.sleep(1_000);
            }
            stx.widgets().closeInterface();
            Time.sleep(1_000, 5_000, () -> !stx.widgets().isInterfaceOpen());
        }
    }

    private static void getMore(StoreItems item) {
        System.out.println("Getting more " + item.getName());
        BankUtils.goToClosestBank();
        BankUtils.openBank();
        if(BankUtils.doesBankHaveItem(item.getName())) {
            BankUtils.withdrawXAsNote(1000, item.getName());
            BankUtils.closeBank();
        } else {
            System.out.println("Going to buy more " + item.getName() + " from the GE");
            GenUtils.saveState();
            if(item.getName().toLowerCase().contains("compost")) {
                GenUtils.setState("Buy Compost");
            } else if(item.getName().contains("Plant cure")) {
                GenUtils.setState("Buy Plant Cure");
            }

        }
    }

    private static WidgetChild getLeprechaunStoreItem(int widgetID) {
        return stx.widgets().get(125).getChild(widgetID);
    }

    private static String amountOfItemLeft(StoreItems item) {
        return getLeprechaunStoreItem(item.getWidgetID()).getChild(10).getText();
    }

    private static void noteProduce(List<Integer> unNotedIDs) {
        NPC leprechaun = stx.npcs().query().named("Tool Leprechaun").results().nearest();
        GenUtils.turnCameraTowards(leprechaun);
        for(int unNotedID : unNotedIDs) {
            while(stx.inventory().contains(unNotedID)) {
                stx.inventory().selectItem(unNotedID);
                leprechaun.interact();
                System.out.println("Noting Crops");
                Time.sleep(4_000, () -> !stx.inventory().contains(unNotedID));
            }
        }
        harvesting = false;
    }

    public static void rake(SceneObject patch) {
        GenUtils.turnCameraTowards(patch);
        if(patch.isVisible()) {
            patch.interact("Rake");
            System.out.println("Raking " + patch.getName());
            Time.sleep(5_000, () -> stx.localPlayer().isAnimating());
        } else {
            if(farmCacti == true) {
                if(patch.getName().contains("Cactus patch")) {
                    stx.mouse().click(new Tile(patch.getX(), patch.getY() - 9, 0));
                    Time.sleep(3_000, 10_000, () -> patch.isVisible());
                    patch.interact("Rake");
                    System.out.println("Raking " + patch.getName());
                    Time.sleep(5_000, () -> stx.localPlayer().isAnimating());
                }
            }
            if(farmFlowers == true) {
                if(patch.getName().contains("Flower Patch")) {
                    stx.mouse().click(new Tile(patch.getX(), patch.getY() + 5, 0));
                    Time.sleep(3_000, 10_000, () -> patch.isVisible());
                    patch.interact("Rake");
                    System.out.println("Raking " + patch.getName());
                    Time.sleep(5_000, () -> stx.localPlayer().isAnimating());
                }
            }
            if(farmAllotments == true) {
                if(patch.getName().contains("Allotment")) {
                    stx.mouse().click(new Tile(patch.getX() - 3, patch.getY() + 3, 0));
                    Time.sleep(3_000, 10_000, () -> patch.isVisible());
                    patch.interact("Rake");
                    System.out.println("Raking " + patch.getName());
                    Time.sleep(5_000, () -> stx.localPlayer().isAnimating());
                }
            }
        }
    }

    public static void compost(SceneObject patch, StoreItems compostType) {
        if(!stx.inventory().contains(compostType.getNotedID() - 1) || stx.inventory().contains(compostType.getNotedID())) {
            getItemFromLeprechaun(compostType, 4);
        }
        if(stx.inventory().contains(compostType.getNotedID() - 1)) {
            GenUtils.turnCameraTowards(patch);
            if(patch.isVisible()) {
                stx.inventory().selectItem(compostType.getName());
                patch.interact();
            } else {
                if(farmCacti == true) {
                    if(patch.getName().contains("Cactus patch")) {
                        stx.mouse().click(new Tile(patch.getX(), patch.getY() - 9, 0));
                        Time.sleep(3_000, 10_000, () -> patch.isVisible());
                        stx.inventory().selectItem(compostType.getName());
                        patch.interact();
                    }
                }
                if(farmFlowers == true) {
                    if(patch.getName().contains("Flower Patch")) {
                        stx.mouse().click(new Tile(patch.getX(), patch.getY() + 5, 0));
                        Time.sleep(3_000, 10_000, () -> patch.isVisible());
                        stx.inventory().selectItem(compostType.getName());
                        patch.interact();
                    }
                }
                if(farmAllotments == true) {
                    if(patch.getName().contains("Allotment")) {
                        stx.mouse().click(new Tile(patch.getX() - 3, patch.getY() + 3, 0));
                        Time.sleep(3_000, 10_000, () -> patch.isVisible());
                        stx.inventory().selectItem(compostType.getName());
                        patch.interact();
                    }
                }
            }
            System.out.println("Composting " + patch.getName());
            Time.sleep(1_500, 6_000, () -> stx.widgets().get(162).getChild(56).getChild(0).getText().contains("ultracompost"));
            Time.sleep(1_000);
        }
    }

    public static void plantFromListInPatch(List<Crops> cropList, SceneObject patch) {
        for(Crops crop : cropList) {
            if(crop.getPatchType().contains(patch.getName())) {
                plant(crop.getSeedName(), patch);
            }
        }
    }

    public static void plant(String seed, SceneObject patch) {
        GenUtils.turnCameraTowards(patch);
        if(patch.isVisible()) {
            stx.inventory().selectItem(seed);
            patch.interact();
            System.out.println("Planting " + seed + " in " + patch.getName());
            Time.sleep(3_000, () -> stx.localPlayer().isAnimating());
        } else {
            if(farmCacti == true) {
                if(patch.getName().contains("Cactus patch")) {
                    stx.mouse().click(new Tile(patch.getX(), patch.getY() - 9, 0));
                    Time.sleep(3_000, 10_000, () -> patch.isVisible());
                    stx.inventory().selectItem(seed);
                    patch.interact();
                    System.out.println("Planting " + seed + " in " + patch.getName());
                    Time.sleep(3_000, () -> stx.localPlayer().isAnimating());
                }
            }
            if(farmFlowers == true) {
                if(patch.getName().contains("Flower Patch")) {
                    stx.mouse().click(new Tile(patch.getX(), patch.getY() + 5, 0));
                    Time.sleep(3_000, 10_000, () -> patch.isVisible());
                    stx.inventory().selectItem(seed);
                    patch.interact();
                    System.out.println("Planting " + seed + " in " + patch.getName());
                    Time.sleep(3_000, () -> stx.localPlayer().isAnimating());
                }
            }
            if(farmAllotments == true) {
                if(patch.getName().contains("Allotment")) {
                    stx.mouse().click(new Tile(patch.getX() - 3, patch.getY() + 3, 0));
                    Time.sleep(3_000, 10_000, () -> patch.isVisible());
                    stx.inventory().selectItem(seed);
                    patch.interact();
                    System.out.println("Planting " + seed + " in " + patch.getName());
                    Time.sleep(3_000, () -> stx.localPlayer().isAnimating());
                }
            }
        }
    }

    public static void harvest(SceneObject produce) {
        GenUtils.turnCameraTowards(produce);
        if(produce.isVisible()) {
            if(produce.hasAction("Harvest")) {
                produce.interact("Harvest");
                System.out.println("Harvesting " + produce.getName());
            } else if(produce.hasAction("Pick")) {
                produce.interact("Pick");
                System.out.println("Picking " + produce.getName());
            } else if(produce.hasAction("Pick-from")) {
                produce.interact("Pick-from");
                System.out.println("Picking " + produce.getName());
            } else if(produce.hasAction("Pick-spine")) {
                produce.interact("Pick-spine");
                System.out.println("Picking " + produce.getName());
            }
        } else {
            if(farmCacti == true) {
                if(produce.getName().contains(cactusCrop.getCropIdentifier())) {
                    stx.mouse().click(new Tile(produce.getX(), produce.getY() - 9, 0));
                    Time.sleep(3_000, 10_000, () -> produce.isVisible());
                    if(produce.hasAction("Harvest")) {
                        produce.interact("Harvest");
                        System.out.println("Harvesting " + produce.getName());
                    } else if(produce.hasAction("Pick")) {
                        produce.interact("Pick");
                        System.out.println("Picking " + produce.getName());
                    } else if(produce.hasAction("Pick-from")) {
                        produce.interact("Pick-from");
                        System.out.println("Picking " + produce.getName());
                    } else if(produce.hasAction("Pick-spine")) {
                        produce.interact("Pick-spine");
                        System.out.println("Picking " + produce.getName());
                    }
                }
            }
            if(farmFlowers == true) {
                if(produce.getName().contains(flowerCrop.getCropIdentifier())) {
                    stx.mouse().click(new Tile(produce.getX(), produce.getY() + 5, 0));
                    Time.sleep(3_000, 10_000, () -> produce.isVisible());
                    if(produce.hasAction("Harvest")) {
                        produce.interact("Harvest");
                        System.out.println("Harvesting " + produce.getName());
                    } else if(produce.hasAction("Pick")) {
                        produce.interact("Pick");
                        System.out.println("Picking " + produce.getName());
                    } else if(produce.hasAction("Pick-from")) {
                        produce.interact("Pick-from");
                        System.out.println("Picking " + produce.getName());
                    } else if(produce.hasAction("Pick-spine")) {
                        produce.interact("Pick-spine");
                        System.out.println("Picking " + produce.getName());
                    }
                }
            }
            if(farmAllotments == true) {
                if(produce.getName().contains(allotmentCrop.getCropIdentifier())) {
                    stx.mouse().click(new Tile(produce.getX() - 6, produce.getY() + 3, 0));
                    Time.sleep(3_000, 10_000, () -> produce.isVisible());
                    if(produce.hasAction("Harvest")) {
                        produce.interact("Harvest");
                        System.out.println("Harvesting " + produce.getName());
                    } else if(produce.hasAction("Pick")) {
                        produce.interact("Pick");
                        System.out.println("Picking " + produce.getName());
                    } else if(produce.hasAction("Pick-from")) {
                        produce.interact("Pick-from");
                        System.out.println("Picking " + produce.getName());
                    } else if(produce.hasAction("Pick-spine")) {
                        produce.interact("Pick-spine");
                        System.out.println("Picking " + produce.getName());
                    }
                }
            }
        }
        harvesting = true;
    }

    public static void checkHealth(SceneObject produce) {
        GenUtils.turnCameraTowards(produce);
        if(produce.isVisible()) {
            produce.interact("Check-health");
            System.out.println("Checking health of " + produce.getName());
        } else {
            if(farmCacti == true) {
                if(produce.getName().contains(cactusCrop.getCropIdentifier())) {
                    stx.mouse().click(new Tile(produce.getX(), produce.getY() - 9, 0));
                    Time.sleep(3_000, 10_000, () -> produce.isVisible());
                    if(produce.hasAction("Check-health")) {
                        produce.interact("Check-health");
                        System.out.println("Checking health of " + produce.getName());
                    }
                }
            }
            if(farmBushes == true) {
                if(produce.getName().contains(bushCrop.getCropIdentifier())) {
                    stx.mouse().click(new Tile(produce.getX(), produce.getY() + 5, 0));
                    Time.sleep(3_000, 10_000, () -> produce.isVisible());
                    if(produce.hasAction("Check-health")) {
                        produce.interact("Check-health");
                        System.out.println("Checking health of " + produce.getName());
                    }
                }
            }
        }
    }

    public static void cure(SceneObject diseasedCrops) {
        if(!stx.inventory().contains(6036)) {
            getItemFromLeprechaun(StoreItems.PLANT_CURE, 1);
        } else {
            GenUtils.turnCameraTowards(diseasedCrops);
            if(diseasedCrops.isVisible()) {
                diseasedCrops.interact("Cure");
                System.out.println("Cured " + diseasedCrops.getName());
                Time.sleep(10_000, () -> stx.localPlayer().isAnimating());
            } else {
                if(farmCacti == true) {
                    if(diseasedCrops.getName().contains(cactusCrop.getDiseasedName())) {
                        stx.mouse().click(new Tile(diseasedCrops.getX(), diseasedCrops.getY() - 9, 0));
                        Time.sleep(3_000, 10_000, () -> diseasedCrops.isVisible());
                        diseasedCrops.interact("Cure");
                        System.out.println("Cured " + diseasedCrops.getName());
                        Time.sleep(10_000, () -> stx.localPlayer().isAnimating());
                    }
                }
                if(farmFlowers == true) {
                    if(diseasedCrops.getName().contains(flowerCrop.getDiseasedName())) {
                        stx.mouse().click(new Tile(diseasedCrops.getX(), diseasedCrops.getY() + 5, 0));
                        Time.sleep(3_000, 10_000, () -> diseasedCrops.isVisible());
                        diseasedCrops.interact("Cure");
                        System.out.println("Cured " + diseasedCrops.getName());
                        Time.sleep(10_000, () -> stx.localPlayer().isAnimating());
                    }
                }
                if(farmAllotments == true) {
                    if(diseasedCrops.getName().contains(allotmentCrop.getDiseasedName())) {
                        stx.mouse().click(new Tile(diseasedCrops.getX() - 3, diseasedCrops.getY() + 3, 0));
                        Time.sleep(3_000, 10_000, () -> diseasedCrops.isVisible());
                        diseasedCrops.interact("Cure");
                        System.out.println("Cured " + diseasedCrops.getName());
                        Time.sleep(10_000, () -> stx.localPlayer().isAnimating());
                    }
                }
            }
        }
    }

    public static void clear(SceneObject deadCrops) {
        GenUtils.turnCameraTowards(deadCrops);

        if(deadCrops.isVisible()) {
            deadCrops.interact("Clear");
            System.out.println("Clearing " + deadCrops.getName());
        } else {
            if(farmCacti == true) {
                if(deadCrops.getName().contains(cactusCrop.getDeadName())) {
                    stx.mouse().click(new Tile(deadCrops.getX(), deadCrops.getY() - 9, 0));
                    Time.sleep(3_000, 10_000, () -> deadCrops.isVisible());
                    deadCrops.interact("Clear");
                    System.out.println("Clearing " + deadCrops.getName());
                }
            }
            if(farmFlowers == true) {
                if(deadCrops.getName().contains(flowerCrop.getDeadName())) {
                    stx.mouse().click(new Tile(deadCrops.getX(), deadCrops.getY() + 5, 0));
                    Time.sleep(3_000, 10_000, () -> deadCrops.isVisible());
                    deadCrops.interact("Clear");
                    System.out.println("Clearing " + deadCrops.getName());
                }
            }
            if(farmAllotments == true) {
                if(deadCrops.getName().contains(allotmentCrop.getDeadName())) {
                    stx.mouse().click(new Tile(deadCrops.getX() - 3, deadCrops.getY() + 3, 0));
                    Time.sleep(3_000, 10_000, () -> deadCrops.isVisible());
                    deadCrops.interact("Clear");
                    System.out.println("Clearing " + deadCrops.getName());
                }
            }
        }
        clearing = true;
    }

    protected static boolean isFarming() {
        return stx.localPlayer().isAnimating() || stx.localPlayer().isMoving();
    }

}
