import com.epicbot.api.os.model.game.WidgetID;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.time.Time;

import java.util.ArrayList;
import java.util.List;

@ScriptManifest(name = "Farmer-gui", gameType = GameType.OS)
public class main extends LoopScript {

    public static String watDo = "Farm Entrana";
    public static String savedState;
    public static String playerName;

    private ClientGUI gui = new ClientGUI();

    private List<String> startingItemsList = new ArrayList();
    private List<Integer> startingQuantitiesList = new ArrayList();
    public static List<String> shoppingListItems = new ArrayList();
    public static List<Integer> shoppingListQuantities = new ArrayList();
    private int geBuyCounter = 0;
    private int gePriceIncreaseCounter = 1;

    private enum StartingItems {

        SPADE ("Spade", 1),
        RAKE ("Rake", 1),
        SEED_DIBBER ("Seed dibber", 1),
        CAMELOT_TELEPORT ("Camelot teleport", 1),
        COINS ("Coins", 10000);


        private final String name;
        private final int quantityNeeded;

        StartingItems(String name, int quantityNeeded) {
            this.name = name;
            this.quantityNeeded = quantityNeeded;
        }
    }

    public List<String> getStartingItemNames() {
        List<String> names = new ArrayList<String>();
        for(StartingItems item : StartingItems.values()) {
            names.add(item.name);
        }
        return names;
    }

    public List<Integer> getStartingItemQuantities() {
        List<Integer> quantities = new ArrayList<Integer>();
        for(StartingItems item : StartingItems.values()) {
            quantities.add(item.quantityNeeded);
        }
        return quantities;
    }

    public int getItemQuantity(String item) {
        int i = 0;
        for(String startingItem : startingItemsList) {
            if(startingItem.contains(item)) {
                return startingQuantitiesList.get(i);
            }
            i += 1;
        }
        return 0;
    }

    private void createStartingItemsLists() {
        startingItemsList.addAll(getStartingItemNames());
        startingItemsList.addAll(FarmingUtils.cropsListToSeedNames());
        startingQuantitiesList.addAll(getStartingItemQuantities());
        startingQuantitiesList.addAll(FarmingUtils.cropsListToSeedQuantities());
    }

    private void goToFarmingAreaNearestTile(Area farmingArea) {
        GenUtils.walkToAreaNearestTile(farmingArea);
        FarmingUtils.harvesting = false;
        FarmingUtils.clearing = false;
    }

    private void goToFarmingAreaCentralTile(Area farmingArea) {
        GenUtils.walkToAreaCentralTile(farmingArea);
        FarmingUtils.harvesting = false;
        FarmingUtils.clearing = false;
    }

    private int waitForBuyAndIncreaseOffer(String item, int waitTime) {
        if(geBuyCounter >= waitTime) {
            GEUtils.abortOffer(GEUtils.getGESlotWithItem(item));
            GEUtils.makeBuyOffer(item, 100, gePriceIncreaseCounter);
            geBuyCounter = 0;
            gePriceIncreaseCounter += 1;
            return 1000;
        }
        geBuyCounter += 1;
        return 1000;
    }

    private void setPlayerName() { playerName = getAPIContext().localPlayer().getName(); }
    public static String getPlayerName() { return playerName; }

    @Override
    public boolean onStart(String... strings) {
        setPlayerName();
        FarmingUtils.initCropsList();

        System.out.println(strings);

        gui.initUI();
        System.out.println("Lets Farm!");
        return true;
    }

    @Override
    protected int loop() {
        if (!getAPIContext().client().isLoggedIn()) {
            return 300;
        }

        if (gui.isOpen()) {
            FarmingUtils.updateCropsList();
            return 100;
        }

        if(GenUtils.hasNullValues(FarmingUtils.crops)) {
            GenUtils.removeNullValues(FarmingUtils.crops);
        }

        if(GenUtils.inventoryContainsTrash(FarmingUtils.trash) && !FarmingUtils.isFarming()) {
            GenUtils.dropTrash(FarmingUtils.trash);
        }

        if(getAPIContext().dialogues().canContinue()) {
            getAPIContext().dialogues().selectContinue();
            FarmingUtils.harvesting = false;
            FarmingUtils.clearing = false;
            return 1000;
        }

        if(startingItemsList.isEmpty() || startingQuantitiesList.isEmpty()) {
            createStartingItemsLists();
        }

        System.out.println(watDo);

        switch (watDo) {
            case "":
                getAPIContext().camera().setPitch(98);
                getAPIContext().mouse().move(getAPIContext().game().getCenterSceneTile().getCentralPoint());
                getAPIContext().mouse().scroll(false, 20);
                watDo = "Bank";
                return 500;
            case "Bank":
                if(!getAPIContext().bank().isOpen() && !InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                    System.out.println("Going to pick up starting items");
                    BankUtils.goToClosestBank();
                    BankUtils.openBank();
                    BankUtils.depositInventoryIfNotEmpty();
                    return 500;
                }
                if(InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                    watDo = "Farm Catherby";
                    return 500;
                }
                if(getAPIContext().bank().isOpen()) {
                    if(BankUtils.doesBankHaveItemsWithQuantity(startingItemsList, startingQuantitiesList)) {
                        System.out.println("Withdrawing starting items from Bank");
                        BankUtils.depositInventoryIfNotEmpty();
                        getAPIContext().bank().depositEquipment();
                        getAPIContext().bank().withdraw(1, "Chronicle");
                        getAPIContext().bank().withdraw(1, "Skills Necklace(1)", "Skills Necklace(2)", "Skills Necklace(3)", "Skills Necklace(4)", "Skills Necklace(5)", "Skills Necklace(6)");
                        getAPIContext().inventory().interactItem("Wield", "Chronicle");
                        getAPIContext().inventory().interactItem("Wear", "Skills Necklace(1)", "Skills Necklace(2)", "Skills Necklace(3)", "Skills Necklace(4)", "Skills Necklace(5)", "Skills Necklace(6)");
                        BankUtils.withdrawItemsFromListWithQuantity(startingItemsList, startingQuantitiesList);
                        if(InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                            getAPIContext().bank().close();
                            watDo = "Farm Catherby";
                            return 500;
                        }
                    }  else {
                        BankUtils.depositInventoryIfNotEmpty();
                        getAPIContext().bank().withdrawAll("Coins");
                        shoppingListItems = GEUtils.createShoppingListItems(startingItemsList, startingQuantitiesList);
                        shoppingListQuantities = GEUtils.createShoppingListQuantities(startingItemsList, startingQuantitiesList);
                        System.out.println(shoppingListItems);
                        System.out.println(shoppingListQuantities);
                        getAPIContext().bank().close();
                        System.out.println("Bank does not have items, so going to buy them from the GE");
                        watDo = "Buy items from GE";
                    }
                }
                break;
            case "Buy items from GE":
                if(!getAPIContext().grandExchange().isOpen()) {
                    if(!Constants.GRAND_EXCHANGE_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                        GenUtils.teleportWithChronicle();
                        GEUtils.goToGE();
                        return 500;
                    } else if(!getAPIContext().inventory().contains("Coins")){
                        getAPIContext().bank().open();
                        BankUtils.withdrawAllCoins();
                        getAPIContext().bank().close();
                        return 500;
                    } else {
                        getAPIContext().grandExchange().open();
                        return 500;
                    }
                }
                if(getAPIContext().grandExchange().isOpen()) {
                    if(!GEUtils.allSalesComplete(shoppingListItems)) {
                        System.out.println(shoppingListItems);
                        System.out.println(shoppingListQuantities);
                        GEUtils.buyItemsFromShoppingList(shoppingListItems, shoppingListQuantities);
                        watDo = "Wait for sales from GE";
                        return 500;
                    }
                }
                break;
            case "Wait for sales from GE":
                if(GEUtils.anySaleNotYetComplete(shoppingListItems) && shoppingListItems.size() > 0 &&  gePriceIncreaseCounter <= 10) {
                    if(geBuyCounter >= 10) {
                        System.out.println(shoppingListItems + "Items left");
                        System.out.println(shoppingListQuantities + "Quantities left");
                        int i = 0;
                        for(String item : GEUtils.getSalesNotYetCompleteWithNames(shoppingListItems)) {
                            System.out.println(GEUtils.getSalesNotYetCompleteWithNames(shoppingListItems) + "Sales not yet complete");
                            GEUtils.abortOffer(GEUtils.getGESlotWithItem(item));
                            if(item == "Rake" || item == "Seed dibber" || item == "Spade") {
                                GEUtils.makeBuyOffer(item, getItemQuantity(item), gePriceIncreaseCounter);
                            } else {
                                GEUtils.makeBuyOffer(item, getItemQuantity(item) * 10, gePriceIncreaseCounter);
                            }
                            i += 1;
                        }
                        geBuyCounter = 0;
                        gePriceIncreaseCounter += 1;
                        return 1000;
                    }
                    GEUtils.doesInventoryHaveItemsWithQuantity(shoppingListItems, shoppingListQuantities);
                    geBuyCounter += 1;
                    return 1000;
                }
                if(gePriceIncreaseCounter >= 10) {
                    for(String item : shoppingListItems) {
                        FarmingUtils.crops.remove(Crops.getCropfromName(item));
                        if(Crops.getCropfromName(item).getPatchType() == "Allotment") {
                            FarmingUtils.farmAllotments = false;
                        }
                        if(Crops.getCropfromName(item).getPatchType() == "Flower Patch") {
                            FarmingUtils.farmAllotments = false;
                        }
                        if(Crops.getCropfromName(item).getPatchType() == "Herb patch") {
                            FarmingUtils.farmAllotments = false;
                        }
                        if(Crops.getCropfromName(item).getPatchType() == "Hops Patch") {
                            FarmingUtils.farmAllotments = false;
                        }
                    }
                }
                System.out.println(shoppingListItems.size());
                if(GEUtils.allSalesComplete(shoppingListItems) || shoppingListItems.size() == 0) {
                    getAPIContext().grandExchange().collectToBank();
                    getAPIContext().grandExchange().close();
                    watDo = "Bank";
                    return 500;
                }
                break;
            case "Buy Compost":
                System.out.println(getAPIContext().inventory().contains(FarmingUtils.compostType.getNotedID()));
                System.out.println(GEUtils.saleComplete(FarmingUtils.compostType.getName()));
                if(GEUtils.saleComplete(FarmingUtils.compostType.getName())) {
                    Time.sleep(2_000);
                    getAPIContext().grandExchange().collectToInventory();
                    return 2000;
                } else if(getAPIContext().inventory().contains(FarmingUtils.compostType.getNotedID())) {
                    getAPIContext().grandExchange().close();
                    BankUtils.openBank();
                    BankUtils.depositAllCoins();
                    getAPIContext().bank().withdraw(10_000, "Coins");
                    BankUtils.closeBank();
                    watDo = savedState;
                    return 500;
                } else {
                    waitForBuyAndIncreaseOffer(FarmingUtils.compostType.getName(), 10);
                }
                break;
            case "Buy Plant Cure":
                if(GEUtils.saleComplete("Plant cure")) {
                    Time.sleep(2_000);
                    getAPIContext().grandExchange().collectToInventory();
                    return 2000;
                } else if(getAPIContext().inventory().contains(FarmingUtils.compostType.getNotedID())) {
                    getAPIContext().grandExchange().close();
                    BankUtils.openBank();
                    BankUtils.depositAllCoins();
                    getAPIContext().bank().withdraw(10_000, "Coins");
                    BankUtils.closeBank();
                    watDo = savedState;
                    return 500;
                } else {
                    waitForBuyAndIncreaseOffer("Plant cure", 10);
                }
                break;
            case "Farm Catherby":
                if(!Constants.FarmingAreas.CATHERBY_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Catherby Farming Area");
                    goToFarmingAreaCentralTile(Constants.FarmingAreas.CATHERBY_AREA);
                    System.out.println("Farming Catherby");
                    return 1000;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.CATHERBY_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("Catherby is done :)");
                    if(FarmingUtils.farmHops == true) {
                        watDo = "Farm McGrubor";
                    } else {
                        watDo = "Farm Ardougne";
                    }
                    return 1000;
                }
                return 1000;
            case "Farm McGrubor":
                if(!Constants.FarmingAreas.MCGRUBOR_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to McGrubor Farming Area");
                    getAPIContext().webWalking().setUseTeleports(false);
                    goToFarmingAreaNearestTile(Constants.FarmingAreas.MCGRUBOR_AREA);
                    System.out.println("Farming McGrubor");
                    return 1000;
                }

                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.MCGRUBOR_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("McGrubor is done :)");
                    watDo = "Farm Ardougne";
                    return 1000;
                }
                return 1000;
            case "Farm Ardougne":
                if(!Constants.FarmingAreas.ARDOUGNE_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Ardougne Farming Area");
                    goToFarmingAreaCentralTile(Constants.FarmingAreas.ARDOUGNE_AREA);
                    System.out.println("Farming Ardougne");
                    return 1000;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.ARDOUGNE_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("Ardougne is done :)");
                    watDo = "Farm Falador";
                    return 1000;
                }
                return 1000;
            case "Farm Falador":
                if(!Constants.FarmingAreas.FALADOR_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Falador Farming Area");
                    getAPIContext().webWalking().setUseTeleports(true);
                    goToFarmingAreaCentralTile(Constants.FarmingAreas.FALADOR_AREA);
                    System.out.println("Farming Falador");
                    return 1000;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.FALADOR_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("Falador is done :)");
                    if(FarmingUtils.farmHops == true) {
                        watDo = "Farm Entrana";
                    } else {
                        watDo = "Farm Hosidius";
                    }
                    return 1000;
                }
                return 1000;
            case "Farm Entrana":
                if(FarmingUtils.farmHops == true && GenUtils.playerisInArea(Constants.ONSHIP_PORT_SARIM)) {
                    watDo = "Farm Yanille";
                    return 1000;
                } else if(GenUtils.playerisInArea(Constants.ONSHIP_PORT_SARIM)){
                    watDo = "Farm Hosidius";
                    return 1000;
                }
                if(!Constants.FarmingAreas.ENTRANA_BIGGER_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    if(Constants.ONSHIP_ENTRANA.contains(getAPIContext().localPlayer().getLocation())){
                        SceneObject gangplank = getAPIContext().objects().query().named("Gangplank").results().nearest();
                        gangplank.interact("Cross");
                        return 5000;
                    } else if(Constants.MIDLAND_ENTRANA.contains(getAPIContext().localPlayer().getLocation())) {
                        getAPIContext().webWalking().setUseTeleports(false);
                        goToFarmingAreaCentralTile(Constants.FarmingAreas.ENTRANA_AREA);
                        getAPIContext().webWalking().setUseTeleports(true);
                        System.out.println("Farming Entrana");
                    } else if(getAPIContext().localPlayer().getSceneOffset().getY() != Constants.ENTRANA_BOAT_OFFSET.getY() && !Constants.OFFSHIP_ENTRANA.contains(getAPIContext().localPlayer().getLocation()) && !Constants.ONSHIP_ENTRANA.contains(getAPIContext().localPlayer().getLocation())){
                        System.out.println("Walking to Entrana Farming Area");
                        GenUtils.walkToAreaNearestTile(Constants.DOCK_TO_ENTRANA);
                        NPC monk = getAPIContext().npcs().query().named("Monk of Entrana").results().nearest();
                        monk.interact("Take-boat");
                        Time.sleep(3_000, 10_000, () -> !getAPIContext().localPlayer().isMoving());
                    }
                    return 1000;
                }
                if(Constants.FarmingAreas.ENTRANA_BIGGER_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    if(FarmingUtils.patchesAreFarmable()) {
                        FarmingUtils.farm(Constants.FarmingAreas.ENTRANA_BIGGER_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                    } else {
                        System.out.println("Entrana is done :)");
                        GenUtils.walkToAreaNearestTile(Constants.DOCK_FROM_ENTRANA);
                        NPC monk = getAPIContext().npcs().query().named("Monk of Entrana").results().nearest();
                        monk.interact("Take-boat");
                        Time.sleep(1_000, 10_000, () -> !getAPIContext().localPlayer().isMoving());
                        return 1000;
                    }
                }
                return 1000;
            case "Farm Yanille":
                if(!Constants.FarmingAreas.YANILLE_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Yanille Farming Area");
                    goToFarmingAreaNearestTile(Constants.FarmingAreas.YANILLE_AREA);
                    System.out.println("Farming Yanille");
                    return 1000;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.YANILLE_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("Yanille is done :)");
                    watDo = "Farm Hosidius";
                    return 1000;
                }
                return 1000;
            case "Farm Hosidius":
                if(!Constants.FarmingAreas.HOSIDIUS_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Hosidius Farming Area");
                    System.out.println(GenUtils.getBottomChatMessage());
                    if(GenUtils.getBottomChatMessage().contains("You must travel to Great Kourend before")) {
                        System.out.println("in this loop");
                        GenUtils.walkToAreaCentralTile(Constants.VEOS_PORT_SARIM);
                        NPC veos = getAPIContext().npcs().query().named("Veos").results().nearest();
                        veos.interact("Talk-to");
                        getAPIContext().dialogues().selectContinue();
                        getAPIContext().dialogues().selectContinue();
                        getAPIContext().dialogues().selectOption(2);
                        getAPIContext().dialogues().selectContinue();
                        getAPIContext().dialogues().selectContinue();
                        goToFarmingAreaCentralTile(Constants.FarmingAreas.HOSIDIUS_AREA);
                    } else {
                        goToFarmingAreaCentralTile(Constants.FarmingAreas.HOSIDIUS_AREA);
                    }
                    System.out.println("Farming Hosidius");
                    return 1000;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.HOSIDIUS_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("Hosidius is done :)");
                    if(FarmingUtils.farmHops == true) {
                        watDo = "Farm Lumbridge";
                    } else {
                        watDo = "Done";
                    }
                }
                return 1000;
            case "Farm Lumbridge":
                if(!Constants.FarmingAreas.LUMBRIDGE_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Lumbridge Farming Area");
                    GenUtils.teleportWithChronicle();
                    goToFarmingAreaCentralTile(Constants.FarmingAreas.LUMBRIDGE_AREA);
                    System.out.println("Farming Lumbridge");
                    return 1000;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.LUMBRIDGE_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("Lumbridge is done :)");
                    watDo = "Done";
                    return 1000;
                }
                return 1000;
            case "Done":
                if(!getAPIContext().bank().isOpen()) {
                    BankUtils.goToClosestBank();
                    BankUtils.openBank();
                } else {
                    BankUtils.depositInventory();
                    BankUtils.closeBank();
                    GenUtils.logOut();
                }
            default:
                break;
        }

        return 500;
    }
}

