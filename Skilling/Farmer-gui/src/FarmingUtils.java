import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.entity.WidgetChild;
import com.epicbot.api.shared.entity.WidgetGroup;
import com.epicbot.api.shared.model.Area;
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
    public static List<Crops> crops = new ArrayList<Crops>();

    public static StoreItems compostType;
    static boolean harvesting = false;
    static boolean clearing = false;
    public static boolean farmAllotments;
    public static boolean farmHerbs;
    public static boolean farmFlowers;
    public static boolean farmHops;

    public static void initCropsList() {
        crops.add(allotmentCrop);
        crops.add(flowerCrop);
        crops.add(herbCrop);
        crops.add(hopsCrop);
    }

    public static void updateCropsList() {
        crops.set(0, allotmentCrop);
        crops.set(1, flowerCrop);
        crops.set(2, herbCrop);
        crops.set(3, hopsCrop);
    }

    public static List<String> cropsListToSeedNames() {
        List<String> names = new ArrayList<>();
        for(Crops crop: crops) {
            names.add(crop.getSeedName());
        }
        return names;
    }

    public static List<Integer> cropsListToSeedQuantities() {
        List<Integer> quantities = new ArrayList<>();
        for(Crops crop: crops) {
            if(crop.getPatchType() == "Allotment" || crop.getPatchType() == "Hops Patch") {
                quantities.add(50);
            }
            if(crop.getPatchType() == "Herb patch" || crop.getPatchType() == "Flower Patch") {
                quantities.add(10);
            }
        }
        return quantities;
    }

    public static void farm(Area area, List<Crops> cropList, StoreItems compostType) {
        if (GenUtils.playerisInArea(area)) {
            List<SceneObject> patches = getPatches();
            if(!isFarming()) {
                if(patches.get(0) != null) {
                    if(!patches.get(0).hasAction("Harvest", "Pick")) {
                        harvesting = false;
                    }
                    if(!patches.get(0).hasAction("Clear")) {
                        clearing = false;
                    }
                }
                if(stx.inventory().isFull()) {
                    noteProduce(Crops.getUnNotedIDs());
                } else if(patches.get(0) != null && patches.get(0).hasAction("Rake" ) && harvesting == false && clearing == false) {
                    rake(patches.get(0));
                } else if(patches.get(0) != null && !patches.get(0).hasAction("Rake") && harvesting == false && clearing == false) {
                    compost(patches.get(0), compostType);
                    if(!stx.grandExchange().isOpen()) {
                        plantFromListInPatch(cropList, patches.get(0));
                    }
                } else if(patches.get(1) != null && patches.get(1).hasAction("Harvest", "Pick") && harvesting == false && clearing == false) {
                    harvest(patches.get(1));
                } else if(patches.get(2) != null && patches.get(2).hasAction("Cure") && harvesting == false && clearing == false) {
                    cure(patches.get(2));
                } else if(patches.get(3) != null && patches.get(3).hasAction("Clear") && harvesting == false && clearing == false) {
                    clear(patches.get(3));
                }
            }
        }
    }

    public static boolean patchesAreFarmable() {
        List<SceneObject> allPatches;
        if(main.watDo == "Farm Lumbridge" || main.watDo == "Farm Entrana") {
            allPatches = stx.objects().query().actions("Cure", "Pick", "Harvest", "Rake", "Clear").notNamed("Wheat", "Cabbage", "Potato").results().toList();
        } else {
            allPatches = stx.objects().query().actions("Cure", "Pick", "Harvest", "Rake", "Clear").notNamed("Wheat", "Cabbage").results().toList();
        }
//        System.out.println(allPatches.size());
//        System.out.println(allPatches);
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
        if(stx.objects().query().named("Allotment").results().nearest() != null && farmAllotments == true) {
            allPatches.add(stx.objects().query().named("Allotment").results().nearest());
        }
        if(stx.objects().query().named("Flower Patch").results().nearest() != null && farmFlowers == true) {
            allPatches.add(stx.objects().query().named("Flower patch").results().nearest());
        }
        if(stx.objects().query().named("Herb patch").results().nearest() != null && farmHerbs == true) {
            allPatches.add(stx.objects().query().named("Herb patch").results().nearest());
        }
        if(stx.objects().query().named("Hops patch").results().nearest() != null && farmHops == true) {
            allPatches.add(stx.objects().query().named("Hops patch").results().nearest());
        }
//        System.out.println(allPatches.size());
//        System.out.println(allPatches);
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
        List<SceneObject> patchList = stx.objects().query().named("Allotment", "Flower Patch", "Herb patch", "Hops patch").results().nearestList();
        List<SceneObject> produceList;
        if(main.watDo == "Farm Lumbridge") {
            produceList = stx.objects().query().actions("Harvest", "Pick").notNamed("Wheat", "Cabbage", "Potato").results().nearestList();
        } else {
            produceList = stx.objects().query().actions("Harvest", "Pick").notNamed("Wheat", "Cabbage").results().nearestList();
        }
        List<SceneObject> diseasedProduceList = stx.objects().query().actions("Cure").results().nearestList();
        List<SceneObject> deadProduceList = stx.objects().query().actions("Clear").results().nearestList();
        List<SceneObject> patches = Arrays.asList(getPatchZero(patchList), getPatchZero(produceList), getPatchZero(diseasedProduceList), getPatchZero(deadProduceList));
        System.out.println(patches.size());
        System.out.println(patches);
        if(!farmAllotments) {
            patches.set(0, parsePatchList("Allotment", patchList));
            patches.set(1, parseProduceList("Allotment", produceList));
            patches.set(2, parseDiseasedList("Allotment", diseasedProduceList));
            patches.set(3, parseDeadList("Allotment", deadProduceList));
        }
        if(!farmHerbs) {
            patches.set(0, parsePatchList("Herb patch", patchList));
            patches.set(1, parseProduceList("Herb patch", produceList));
            patches.set(2, parseDiseasedList("Herb patch", diseasedProduceList));
            patches.set(3, parseDeadList("Herb patch", deadProduceList));
        }
        if(!farmFlowers) {
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
        System.out.println(patches.size());
        System.out.println(patches);
        return patches;
    }

    public static void getItemFromLeprechaun(StoreItems item, int numberOfItems) {
        NPC leprechaun = stx.npcs().query().named("Tool Leprechaun").results().nearest();
        GenUtils.turnCameraTowards(leprechaun);
        leprechaun.interact("Exchange");
        Time.sleep(10_000, () -> stx.widgets().isInterfaceOpen());
        WidgetChild itemWidget = getLeprechaunStoreItem((item.getWidgetID()));
        System.out.println(itemWidget);
        if(amountOfItemLeft(item).contentEquals("0/1000")) {
            System.out.println("in the loop");
            if(stx.inventory().contains(item.getNotedID())) {
                System.out.println("yoyo");
                WidgetChild inventoryWidget = stx.widgets().get(126).getChild(item.getInventoryWidgetID());
                System.out.println(inventoryWidget);
                inventoryWidget.interact("Store-All");
                stx.widgets().closeInterface();
            } else {
                System.out.println("Going to get more " + item.getName());
                stx.widgets().closeInterface();
                Time.sleep(1_000, 5_000, () -> !stx.widgets().isInterfaceOpen());
                getMore(item);
            }
        } else {
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
            GEUtils.goToGE();
            BankUtils.openBank();
            BankUtils.withdrawAllCoins();
            BankUtils.closeBank();
            GEUtils.openGE();
            GEUtils.makeBuyOffer(item.getName(), 100);
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
            if(stx.inventory().contains(unNotedID)) {
                stx.inventory().selectItem(unNotedID);
                leprechaun.interact();
                System.out.println("Noting Crops");
                Time.sleep(5_000, () -> !stx.inventory().contains(unNotedID));
            }
        }
        harvesting = false;
    }

    public static void rake(SceneObject patch) {
        GenUtils.turnCameraTowards(patch);
        patch.interact("Rake");
        System.out.println("Raking " + patch.getName());
        Time.sleep(5_000, () -> stx.localPlayer().isAnimating());
    }

    public static void compost(SceneObject patch, StoreItems compostType) {
        if(!stx.inventory().contains(compostType.getNotedID() - 1) || stx.inventory().contains(compostType.getNotedID())) {
            getItemFromLeprechaun(compostType, 4);
        }
        if(stx.inventory().contains(compostType.getNotedID() - 1)) {
            GenUtils.turnCameraTowards(patch);
            stx.inventory().selectItem(compostType.getName());
            patch.interact();
            System.out.println("Composting " + patch.getName());
            Time.sleep(1_500, 6_000, () -> stx.localPlayer().isAnimating());
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
        stx.inventory().selectItem(seed);
        patch.interact();
        System.out.println("Planting " + seed + " in " + patch.getName());
        Time.sleep(3_000, () -> stx.localPlayer().isAnimating());
    }

    public static void harvest(SceneObject produce) {
        GenUtils.turnCameraTowards(produce);
        if(produce.hasAction("Harvest")) {
            produce.interact("Harvest");
            System.out.println("Harvesting " + produce.getName());
        } else if(produce.hasAction("Pick")) {
            produce.interact("Pick");
            System.out.println("Picking " + produce.getName());
        }
        harvesting = true;
    }

    public static void cure(SceneObject diseasedCrops) {
        if(!stx.inventory().contains("Plant cure")) {
            getItemFromLeprechaun(StoreItems.PLANT_CURE, 1);
        }
        GenUtils.turnCameraTowards(diseasedCrops);
        diseasedCrops.interact("Cure");
        System.out.println("Cured " + diseasedCrops.getName());
        Time.sleep(10_000, () -> stx.localPlayer().isAnimating());
    }

    public static void clear(SceneObject deadCrops) {
        GenUtils.turnCameraTowards(deadCrops);
        deadCrops.interact("Clear");
        System.out.println("Clearing " + deadCrops.getName());
        clearing = true;
    }

    protected static boolean isFarming() {
        return stx.localPlayer().isAnimating() || stx.localPlayer().isMoving();
    }

}
