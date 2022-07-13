import com.epicbot.api.os.model.game.WidgetID;
import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.ItemWidget;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.entity.WidgetChild;
import com.epicbot.api.shared.methods.IGrandExchangeAPI;
import com.epicbot.api.shared.methods.ITabsAPI;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Skill;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.model.ge.GrandExchangeOffer;
import com.epicbot.api.shared.model.ge.GrandExchangeSlot;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@ScriptManifest(name = "Farmer-lvl-based", gameType = GameType.OS)
public class main extends LoopScript {

    public static String watDo = "";
    public static String savedState;
    public static String playerName;

    private ClientGUI gui = new ClientGUI();

    private List<String> startingItemsList = new ArrayList();
    private List<Integer> startingQuantitiesList = new ArrayList();
    public static List<String> shoppingListItems = new ArrayList();
    public static List<Integer> shoppingListQuantities = new ArrayList();
    private int geBuyCounter = 0;
    private int gePriceIncreaseCounter = 1;
    private int timer = 0;
    private int farmTimer;
    private int idleTimer;

    private int farmStartXp;
    private int farmStartLvl;

    private long startTime;

    protected void onPaint(Graphics2D g, APIContext ctx) {
        super.onPaint(g, ctx);
        if (getAPIContext().client().isLoggedIn()) {
            PaintFrame pf = new PaintFrame("Farmer");
            pf.addLine("Runtime: ", Time.getFormattedRuntime(startTime));
            pf.addLine("Status: ", watDo);
            pf.addLine("Levels: ", getAPIContext().skills().farming().getCurrentLevel() + " +(" + getFarmLvls() + ")");
            pf.addLine("Xp/Hr: ", getXpHr() + " +(" + (getAPIContext().skills().farming().getExperience() - farmStartXp) + ")");
            pf.addLine("Xp til level: ", getAPIContext().skills().farming().getExperienceToNextLevel());
            pf.addLine("Gp gained: ", getGPGained());
            pf.addLine("Gp gained per hr: ", getGPHr());
            pf.addLine("Crops: ", FarmingUtils.herbCrop + " , " + FarmingUtils.allotmentCrop + " , " + FarmingUtils.flowerCrop + " , " + FarmingUtils.hopsCrop + " , " + FarmingUtils.bushCrop + " , " + FarmingUtils.cactusCrop);
            pf.draw(g,0,90,ctx);
        }
    }

    private int getGPGained() {
        int gpGained = 0;
        for(String crop : Crops.getNames()) {
            for(ItemWidget item : getAPIContext().inventory().getItems(crop)) {
                gpGained += item.getStackSize()*getAPIContext().pricing().get(crop).getHighestPrice();
            }

        }
        return gpGained;
    }

    private int getGPHr() {
        return (int) ((3600000.0 / (System.currentTimeMillis() - startTime)) * getGPGained());
    }

    private int getFarmLvls() {
        return getAPIContext().skills().get(Skill.Skills.FARMING).getCurrentLevel() - farmStartLvl;
    }
    private long getXpHr() {
        long xpGained = getAPIContext().skills().farming().getExperience() - farmStartXp;
        return (int) ((3600000.0 / (System.currentTimeMillis() - startTime)) * xpGained);
    }

    private enum StartingItems {

        SPADE ("Spade", 1),
        RAKE ("Rake", 1),
        SEED_DIBBER ("Seed dibber", 1),
        CAMELOT_TELEPORT ("Camelot teleport", 10),
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
        System.out.println(startingItemsList);
        System.out.println(startingQuantitiesList);
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
            GEUtils.makeIncreasingBuyOffer(item, 100, gePriceIncreaseCounter);
            geBuyCounter = 0;
            gePriceIncreaseCounter += 1;
            return 1000;
        }
        geBuyCounter += 1;
        return 1000;
    }

    private void setPlayerName() { playerName = getAPIContext().localPlayer().getName(); }
    public static String getPlayerName() { return playerName; }

    private void setCropsBasedOnLevel() {
        int farmingLvl = getAPIContext().skills().farming().getCurrentLevel();
        if(farmingLvl == 1) {
            FarmingUtils.farmAllotments = true;
            FarmingUtils.farmHerbs = false;
            FarmingUtils.farmHops = false;
            FarmingUtils.farmFlowers = false;
            FarmingUtils.farmBushes = false;
            FarmingUtils.farmCacti = false;
        } else if(farmingLvl == 2) {
            FarmingUtils.farmAllotments = true;
            FarmingUtils.farmHerbs = false;
            FarmingUtils.farmHops = false;
            FarmingUtils.farmFlowers = true;
            FarmingUtils.farmBushes = false;
            FarmingUtils.farmCacti = false;
        } else if(farmingLvl >= 3 && farmingLvl < 9) {
            FarmingUtils.farmAllotments = true;
            FarmingUtils.farmHerbs = false;
            FarmingUtils.farmHops = true;
            FarmingUtils.farmFlowers = true;
            FarmingUtils.farmBushes = false;
            FarmingUtils.farmCacti = false;
        } else if(farmingLvl >= 9 && farmingLvl < 48) {
            FarmingUtils.farmAllotments = true;
            FarmingUtils.farmHerbs = true;
            FarmingUtils.farmHops = true;
            FarmingUtils.farmFlowers = true;
            FarmingUtils.farmBushes = false;
            FarmingUtils.farmCacti = false;
        } else if(farmingLvl >= 48 && farmingLvl < 55) {
            FarmingUtils.farmAllotments = true;
            FarmingUtils.farmHerbs = true;
            FarmingUtils.farmHops = true;
            FarmingUtils.farmFlowers = true;
            FarmingUtils.farmBushes = true;
            FarmingUtils.farmCacti = false;
        } else if(farmingLvl >= 55) {
            FarmingUtils.farmAllotments = true;
            FarmingUtils.farmHerbs = true;
            FarmingUtils.farmHops = true;
            FarmingUtils.farmFlowers = true;
            FarmingUtils.farmBushes = true;
            FarmingUtils.farmCacti = true;
        }
        if(farmingLvl == 1) {
            FarmingUtils.allotmentCrop = Crops.POTATO;
        }
        if(farmingLvl >= 2) {
            FarmingUtils.allotmentCrop = Crops.POTATO;
            FarmingUtils.flowerCrop = Crops.MARIGOLD;
        }
        if(farmingLvl >= 3) {
            FarmingUtils.hopsCrop = Crops.BARLEY;
        }
        if(farmingLvl >= 5) {
            FarmingUtils.allotmentCrop = Crops.ONION;
        }
        if(farmingLvl >= 9) {
            FarmingUtils.herbCrop = Crops.GUAM;
        }
        if(farmingLvl >= 11) {
            FarmingUtils.flowerCrop = Crops.ROSEMARY;
        }
        if(farmingLvl >= 12) {
            FarmingUtils.allotmentCrop = Crops.TOMATO;
        }
        if(farmingLvl >= 13) {
            FarmingUtils.hopsCrop = Crops.JUTE;
        }
        if(farmingLvl >= 14) {
            FarmingUtils.herbCrop = Crops.MARRENTILL;
        }
        if(farmingLvl >= 16) {
            FarmingUtils.hopsCrop = Crops.YANILLIAN;
        }
        if(farmingLvl >= 19) {
            FarmingUtils.herbCrop = Crops.TARROMIN;
        }
        if(farmingLvl >= 20) {
            FarmingUtils.allotmentCrop = Crops.SWEETCORN;
        }
        if(farmingLvl >= 26) {
            FarmingUtils.flowerCrop = Crops.LIMPWURT;
            FarmingUtils.herbCrop = Crops.HARRALANDER;
        }
        if(farmingLvl >= 31) {
            FarmingUtils.allotmentCrop = Crops.STRAWBERRY;
        }
        if(farmingLvl >= 38) {
            FarmingUtils.herbCrop = Crops.TOADFLAX;
        }
        if(farmingLvl >= 48) {
            FarmingUtils.bushCrop = Crops.JANGERBERRY;
        }
        if(farmingLvl >= 55) {
            FarmingUtils.cactusCrop = Crops.CACTUS;
        }
    }

    @Override
    public boolean onStart(String... strings) {
        farmStartLvl = getAPIContext().skills().farming().getCurrentLevel();
        farmStartXp = getAPIContext().skills().farming().getExperience();
        startTime = System.currentTimeMillis();

        System.out.println("Lets Farm!");
        return true;
    }

    @Override
    protected int loop() {
        if (!getAPIContext().client().isLoggedIn()) {
            return 300;
        }

//        System.out.println(watDo);
//        System.out.println("Harvesting " + FarmingUtils.harvesting);
//        System.out.println("Clearing " + FarmingUtils.clearing);
//        System.out.println(FarmingUtils.farmBushes);
//        System.out.println(FarmingUtils.farmHerbs);


        if(!FarmingUtils.isFarming() && watDo != "Sell Produce") {
//            System.out.println("Idle Timer: " + idleTimer);
            idleTimer += 1;
            if(watDo == "Farm Rimmington") {
                if(idleTimer == 10) {
                    getAPIContext().webWalking().setUseTeleports(false);
                    BankUtils.goToClosestBank();
                    getAPIContext().webWalking().setUseTeleports(true);
                    FarmingUtils.harvesting = false;
                    FarmingUtils.clearing = false;
                    idleTimer = 0;
                }
            } else if(watDo == "Farm Farming Guild East" || watDo == "Farm Farming Guild West") {
                if(idleTimer == 20) {
                    if(getAPIContext().equipment().contains("Skills necklace")) {
                        getAPIContext().webWalking().setUseTeleports(false);
                        GenUtils.teleportWithChronicle();
                        BankUtils.goToClosestBank();
                        getAPIContext().webWalking().setUseTeleports(true);
                        FarmingUtils.harvesting = false;
                        FarmingUtils.clearing = false;
                        watDo = "Bank";
                        idleTimer = 0;
                    } else {
                        BankUtils.goToClosestBank();
                        FarmingUtils.harvesting = false;
                        FarmingUtils.clearing = false;
                        watDo = "Bank";
                        idleTimer = 0;
                    }
                }
            } else if(idleTimer == 20) {
                getAPIContext().webWalking().setUseTeleports(false);
                GenUtils.teleportWithChronicle();
                BankUtils.goToClosestBank();
                getAPIContext().webWalking().setUseTeleports(true);
                FarmingUtils.harvesting = false;
                FarmingUtils.clearing = false;
                idleTimer = 0;
            }
        } else {
            idleTimer = 0;
        }

        if(!FarmingUtils.isFarming() && (FarmingUtils.harvesting == true || FarmingUtils.clearing == true)) {
//            System.out.println("Farm Timer: " + farmTimer);
            farmTimer += 1;
            if (farmTimer == 15) {
                FarmingUtils.harvesting = false;
                FarmingUtils.clearing = false;
                farmTimer = 0;
            }
        } else {
            farmTimer = 0;
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

        if(getAPIContext().dialogues().hasOption("Yes, I want to clear it for new crops.")) {
            getAPIContext().dialogues().selectOption("Yes, I want to clear it for new crops.");
            FarmingUtils.harvesting = false;
            FarmingUtils.clearing = true;
            return 1000;
        }

        if(FarmingUtils.crops.isEmpty()) {
            setCropsBasedOnLevel();
            FarmingUtils.initCropsList();
            GenUtils.removeNullValues(FarmingUtils.crops);
            return 1000;
        }

        if(startingItemsList.isEmpty() || startingQuantitiesList.isEmpty()) {
            createStartingItemsLists();
            return 1000;
        }

        switch (watDo) {
            case "":
                getAPIContext().camera().setPitch(98);
                getAPIContext().mouse().move(getAPIContext().game().getCenterSceneTile().getCentralPoint());
                getAPIContext().mouse().scroll(false, 20);
                watDo = "Bank";
                return 500;
            case "Bank":
                if(getAPIContext().inventory().contains("Coin pouch")) {
                    getAPIContext().inventory().interactItem("Open-all", "Coin pouch");
                }
                if(!getAPIContext().bank().isOpen() && (!InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList) || !InventoryUtils.doesInventoryHaveItem("Chronicle") || !getAPIContext().inventory().contains("Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)", "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)"))) {
                    System.out.println("Going to pick up starting items");
                    BankUtils.goToClosestBank();
                    BankUtils.openBank();
                    BankUtils.depositInventoryIfNotEmpty();
                    getAPIContext().bank().depositEquipment();
                    return 1200;
                }
                if(InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList) && InventoryUtils.doesInventoryHaveItem("Chronicle") && getAPIContext().inventory().contains("Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)", "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)")) {
                    getAPIContext().inventory().interactItem("Wield", "Chronicle");
                    getAPIContext().inventory().interactItem("Wear", "Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)", "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)");
                    if(getAPIContext().skills().farming().getCurrentLevel() >= 45) {
                        watDo = "Farm Farming Guild East";
                        return 500;
                    } else {
                        watDo = "Farm Catherby";
                        return 500;
                    }
                }
                if(getAPIContext().bank().isOpen()) {
                    if(BankUtils.doesBankHaveItemsWithQuantity(startingItemsList, startingQuantitiesList) && getAPIContext().bank().contains("Chronicle") && getAPIContext().bank().contains("Skills Necklace(1)", "Skills Necklace(2)", "Skills Necklace(3)", "Skills Necklace(4)", "Skills Necklace(5)", "Skills Necklace(6)")) {
                        System.out.println("Withdrawing starting items from Bank");
                        BankUtils.depositInventoryIfNotEmpty();
                        getAPIContext().bank().depositEquipment();
                        getAPIContext().bank().withdraw(1, "Chronicle");
                        getAPIContext().bank().withdraw(1, "Gold elegant legs");
                        getAPIContext().bank().withdraw(1, "Gold elegant shirt");
                        getAPIContext().bank().withdraw(1, "Green partyhat", "Blue partyhat", "Purple partyhat", "Yellow partyhat", "Red partyhat", "White partyhat");
                        getAPIContext().bank().withdraw(1, "Black cape", "Blue cape", "Purple cape", "Orange cape", "Yellow cape", "Red cape", "Green cape");
                        getAPIContext().bank().withdraw(1, "Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)", "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)");
                        getAPIContext().inventory().interactItem("Wield", "Chronicle");
                        getAPIContext().inventory().interactItem("Wear", "Gold elegant legs");
                        getAPIContext().inventory().interactItem("Wear", "Gold elegant shirt");
                        getAPIContext().inventory().interactItem("Wear", "Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)", "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)");
                        getAPIContext().inventory().interactItem("Wear", "Green partyhat", "Blue partyhat", "Purple partyhat", "Yellow partyhat", "Red partyhat", "White partyhat");
                        getAPIContext().inventory().interactItem("Wear", "Black cape", "Blue cape", "Purple cape", "Orange cape", "Yellow cape", "Red cape", "Green cape");
                        BankUtils.withdrawItemsFromListWithQuantity(startingItemsList, startingQuantitiesList);
                        if(InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                            getAPIContext().bank().close();
                            if(getAPIContext().skills().farming().getCurrentLevel() >= 45) {
                                watDo = "Farm Farming Guild East";
                                return 500;
                            } else {
                                watDo = "Farm Catherby";
                                return 500;
                            }
                        }
                    }  else {
                        BankUtils.depositInventoryIfNotEmpty();
                        getAPIContext().bank().depositEquipment();
                        getAPIContext().bank().withdrawAll("Coins");
                        if(!BankUtils.doesBankHaveItem("Chronicle")) {
                            watDo = "Get Chronicle";
                            return 1000;
                        }
                        shoppingListItems = GEUtils.createShoppingListItems(startingItemsList, startingQuantitiesList);
                        shoppingListQuantities = GEUtils.createShoppingListQuantities(startingItemsList, startingQuantitiesList);
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
                    if (GEUtils.anySaleComplete()) {
                        getAPIContext().grandExchange().collectToInventory();
                        return 1200;
                    }
                    for(String item : shoppingListItems) {
                        if(item == "Rake" || item == "Seed dibber" || item == "Spade") {
                            GEUtils.makeBuyOffer(item, getItemQuantity(item), 1000);
                        } else if(item == "Camelot teleport"){
                            GEUtils.makeBuyOffer(item, getItemQuantity(item) * 10, 1000);
                        } else if(item == "Skills necklace(6)") {
                            GEUtils.makeIncreasingBuyOffer(item, 10, gePriceIncreaseCounter);
                        } else if(item != "Toadflax seed") {
                            GEUtils.makeBuyOffer(item, getItemQuantity(item) * 10, 100);
                        } else {
                            GEUtils.makeIncreasingBuyOffer(item, getItemQuantity(item) * 10, gePriceIncreaseCounter);
                        }
                    }
                    watDo = "Wait for sales from GE";
                    return 500;
                }
                break;
            case "Wait for sales from GE":
                if(!getAPIContext().grandExchange().isOpen()) {
                    GEUtils.goToGE();
                    GEUtils.openGE();
                    return 1200;
                }
                if(GEUtils.saleNotYetComplete("Toadflax seed")) {
                    if(geBuyCounter >= 10) {
                        GEUtils.abortOffer(GEUtils.getGESlotWithItem("Toadflax seed"));
                        GEUtils.makeIncreasingBuyOffer("Toadflax seed", getItemQuantity("Toadflax seed") * 10, gePriceIncreaseCounter);
                        geBuyCounter = 0;
                        gePriceIncreaseCounter += 1;
                        return 1000;
                    }
                    geBuyCounter += 1;
                }
                if(GEUtils.allSalesComplete(shoppingListItems)) {
                    getAPIContext().grandExchange().collectToInventory();
                    getAPIContext().grandExchange().close();
                    watDo = "Bank";
                    return 1000;
                } else {
                    if(timer == 180000) {
                        getAPIContext().tabs().open(ITabsAPI.Tabs.EQUIPMENT);
                    }
                    if(timer == 360000) {
                        getAPIContext().tabs().open(ITabsAPI.Tabs.INVENTORY);
                        timer = 0;
                    }
                    timer += 5000;
                    return 5000;
                }
            case "Get Chronicle":
                if(!Constants.DRAYNOR_VILLAGE.contains(getAPIContext().localPlayer().getLocation()) && getAPIContext().npcs().query().named("Diango").results().nearest() == null) {
                    System.out.println("Walking to Draynor Village");
                    goToFarmingAreaCentralTile(Constants.DRAYNOR_VILLAGE);
                    return 1000;
                }
                if(GenUtils.getBottomChatMessage().contains("You can only buy 100 of those per day")) {
                    getAPIContext().store().close();
                    Time.sleep(5_000, () -> !getAPIContext().store().isOpen());
                    getAPIContext().inventory().interactItem("Use", "Teleport card");
                    getAPIContext().inventory().interactItem("Use", "Chronicle");
                    watDo = "Bank";
                    return 1200;
                }
                if((!getAPIContext().inventory().contains("Chronicle") || !getAPIContext().inventory().contains("Teleport card")) && !getAPIContext().store().isOpen()) {
                    getAPIContext().npcs().query().named("Diango").results().nearest().interact("Trade");
                    Time.sleep(10_000, () -> getAPIContext().store().isOpen());
                    return 1200;
                }
                if(!getAPIContext().inventory().contains("Chronicle") && getAPIContext().store().isOpen()) {
                    getAPIContext().store().buyOne("Chronicle");
                    Time.sleep(5_000, () -> getAPIContext().inventory().contains("Chronicle"));
                    return 1200;
                }
                if(getAPIContext().inventory().contains("Chronicle") && !getAPIContext().inventory().contains("Teleport card") && getAPIContext().store().isOpen()) {
                    getAPIContext().store().buyFifty("Teleport card");
                    Time.sleep(5_000, () -> getAPIContext().inventory().contains("Teleport card"));
                    return 1200;
                }
                if(getAPIContext().inventory().contains("Chronicle") && getAPIContext().inventory().contains("Teleport card") && getAPIContext().store().isOpen()) {
                    getAPIContext().store().close();
                    Time.sleep(5_000, () -> !getAPIContext().store().isOpen());
                    return 1200;
                }
                if(getAPIContext().inventory().contains("Chronicle") && getAPIContext().inventory().contains("Teleport card") && !getAPIContext().store().isOpen()) {
                    getAPIContext().inventory().interactItem("Use", "Teleport card");
                    getAPIContext().inventory().interactItem("Use", "Chronicle");
                    return 1200;
                }
                break;

            case "Recharge chronicle":
                if(!Constants.DRAYNOR_VILLAGE.contains(getAPIContext().localPlayer().getLocation()) && getAPIContext().npcs().query().named("Diango").results().nearest() == null) {
                    System.out.println("Walking to Draynor Village");
                    goToFarmingAreaCentralTile(Constants.DRAYNOR_VILLAGE);
                    return 1000;
                }
                if(getAPIContext().inventory().isItemSelected()) {
                    getAPIContext().inventory().deselectItem();
                }
                if(GenUtils.getBottomChatMessage().contains("You can only buy 100 of those per day")) {
                    getAPIContext().store().close();
                    Time.sleep(5_000, () -> !getAPIContext().store().isOpen());
                    getAPIContext().inventory().interactItem("Use", "Teleport card");
                    getAPIContext().inventory().interactItem("Use", "Chronicle");
                    BankUtils.goToClosestBank();
                    BankUtils.openBank();
                    BankUtils.depositInventoryIfNotEmpty();
                    getAPIContext().bank().depositEquipment();
                    getAPIContext().bank().withdraw(1, "Chronicle");
                    getAPIContext().bank().withdraw(1, "Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)", "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)");
                    getAPIContext().inventory().interactItem("Wield", "Chronicle");
                    getAPIContext().inventory().interactItem("Wear", "Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)", "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)");
                    BankUtils.withdrawItemsFromListWithQuantity(startingItemsList, startingQuantitiesList);
                    if(FarmingUtils.farmHops == true) {
                        watDo = "Farm Entrana";
                        return 1200;
                    } else {
                        watDo = "Farm Hosidius";
                        return 1200;
                    }
                }
                if(getAPIContext().inventory().getEmptySlotCount() < 26 && !getAPIContext().inventory().contains("Teleport card") && !getAPIContext().inventory().contains("Chronicle")) {
                    BankUtils.goToClosestBank();
                    BankUtils.openBank();
                    BankUtils.depositInventory();
                    BankUtils.withdrawAllCoins();
                    getAPIContext().bank().close();
                    return 1200;
                }
                if(!getAPIContext().inventory().contains("Chronicle")) {
                    getAPIContext().equipment().getItem("Chronicle").interact("Remove");
                    return 1200;
                }
                if(!getAPIContext().inventory().contains("Teleport card") && !getAPIContext().store().isOpen()) {
                    getAPIContext().npcs().query().named("Diango").results().nearest().interact("Trade");
                    Time.sleep(10_000, () -> getAPIContext().store().isOpen());
                    return 1200;
                }
                if(!getAPIContext().inventory().contains("Teleport card") && getAPIContext().store().isOpen()) {
                    getAPIContext().store().buyFifty("Teleport card");
                    Time.sleep(5_000, () -> getAPIContext().inventory().contains("Teleport card"));
                    return 1200;
                }
                if(getAPIContext().inventory().contains("Teleport card") && getAPIContext().store().isOpen()) {
                    getAPIContext().store().close();
                    Time.sleep(5_000, () -> !getAPIContext().store().isOpen());
                    return 1200;
                }
                if(getAPIContext().inventory().contains("Teleport card") && !getAPIContext().store().isOpen()) {
                    getAPIContext().inventory().interactItem("Use", "Teleport card");
                    getAPIContext().inventory().interactItem("Use", "Chronicle");
                    return 1200;
                }
                break;

            case "Buy Compost":
                if(!getAPIContext().grandExchange().isOpen()) {
                    GenUtils.teleportWithChronicle();
                    getAPIContext().webWalking().setUseTeleports(false);
                    GEUtils.goToGE();
                    getAPIContext().webWalking().setUseTeleports(true);
                    BankUtils.openBank();
                    BankUtils.withdrawAllCoins();
                    BankUtils.closeBank();
                    GEUtils.openGE();
                }
                if(GEUtils.getGESlotWithItem("Ultracompost") == null) {
                    GEUtils.makeBuyOffer("Ultracompost", 400, 1000);
                }
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
                    GenUtils.teleportWithChronicle();
                    watDo = savedState;
                    return 500;
                } else {
                    if (timer == 180000) {
                        getAPIContext().tabs().open(ITabsAPI.Tabs.EQUIPMENT);
                        getAPIContext().grandExchange().close();
                        BankUtils.openBank();
                        if(getAPIContext().bank().contains("Ultracompost")) {
                            getAPIContext().bank().withdrawAll("Ultracompost");
                            BankUtils.depositAllCoins();
                            getAPIContext().bank().withdraw(10_000, "Coins");
                            watDo = savedState;
                            BankUtils.closeBank();
                            GenUtils.teleportWithChronicle();
                            return 500;
                        } else {
                            BankUtils.closeBank();
                            GEUtils.openGE();
                            return 500;
                        }
                    }
                    if (timer == 360000) {
                        getAPIContext().tabs().open(ITabsAPI.Tabs.INVENTORY);
                        timer = 0;
                    }
                    System.out.println("Waiting for GE sales");
                    timer += 5000;
                    return 5000;
                }
            case "Buy Plant Cure":
                if(!getAPIContext().grandExchange().isOpen()) {
                    GenUtils.teleportWithChronicle();
                    getAPIContext().webWalking().setUseTeleports(false);
                    GEUtils.goToGE();
                    getAPIContext().webWalking().setUseTeleports(true);
                    BankUtils.openBank();
                    BankUtils.withdrawAllCoins();
                    BankUtils.closeBank();
                    GEUtils.openGE();
                }
                if(GEUtils.getGESlotWithItem("Plant cure") == null) {
                    GEUtils.makeBuyOffer("Plant cure", 100, 1000);
                }
                if(GEUtils.saleComplete("Plant cure")) {
                    Time.sleep(2_000);
                    getAPIContext().grandExchange().collectToInventory();
                    return 2000;
                } else if(getAPIContext().inventory().contains(StoreItems.PLANT_CURE.getNotedID())) {
                    getAPIContext().grandExchange().close();
                    BankUtils.openBank();
                    BankUtils.depositAllCoins();
                    getAPIContext().bank().withdraw(10_000, "Coins");
                    BankUtils.closeBank();
                    GenUtils.teleportWithChronicle();
                    watDo = savedState;
                    return 500;
                } else {
                    if (timer == 180000) {
                        getAPIContext().tabs().open(ITabsAPI.Tabs.EQUIPMENT);
                        getAPIContext().grandExchange().close();
                        BankUtils.openBank();
                        if(getAPIContext().bank().contains("Plant cure")) {
                            getAPIContext().bank().withdrawAll("Plant cure");
                            BankUtils.depositAllCoins();
                            getAPIContext().bank().withdraw(10_000, "Coins");
                            watDo = savedState;
                            BankUtils.closeBank();
                            GenUtils.teleportWithChronicle();
                            return 500;
                        } else {
                            BankUtils.closeBank();
                            GEUtils.openGE();
                            return 500;
                        }
                    }
                    if (timer == 360000) {
                        getAPIContext().tabs().open(ITabsAPI.Tabs.INVENTORY);
                        timer = 0;
                    }
                    System.out.println("Waiting for GE sales");
                    timer += 5000;
                    return 5000;
                }
            case "Farm Farming Guild East":
                if(!Constants.FarmingAreas.FARMING_GUILD_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Farming East guild Area");
                    getAPIContext().equipment().getItem("Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)", "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)").interact("Farming Guild");
                    System.out.println("Farming Farming Guild East");
                    return 2667;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.FARMING_GUILD_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("Farming Guild East is done :)");
                    if(getAPIContext().skills().farming().getCurrentLevel() >= 65) {
                        watDo = "Farm Farming Guild West";
                        return 1000;
                    } else {
                        watDo = "Farm Catherby";
                        return 1000;
                    }

                }
                return 1000;
            case "Farm Farming Guild West":
                if(!Constants.FarmingAreas.FARMING_GUILD_WEST_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Farming guild West Area");
                    if(getAPIContext().equipment().contains("Skills necklace")) {
                        watDo = "Bank";
                        return 1200;
                    }
                    getAPIContext().equipment().getItem("Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)", "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)").interact("Farming Guild");
                    System.out.println("Farming Farming Guild West");
                    return 2667;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.FARMING_GUILD_WEST_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("Farming Guild West is done :)");
                    watDo = "Farm Catherby";
                    return 1000;
                }
                return 1000;
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
                    getAPIContext().webWalking().setUseTeleports(true);
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
                    getAPIContext().webWalking().setUseTeleports(false);
                    goToFarmingAreaCentralTile(Constants.FarmingAreas.ARDOUGNE_AREA);
                    getAPIContext().webWalking().setUseTeleports(true);
                    System.out.println("Farming Ardougne");
                    return 1000;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.ARDOUGNE_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("Ardougne is done :)");
                    if(FarmingUtils.farmBushes == true) {
                        watDo = "Farm Rimmington";
                    } else {
                        watDo = "Farm Falador";
                    }
                    return 1000;
                }
                return 1000;
            case "Farm Rimmington":
                if(!Constants.FarmingAreas.RIMMINGTON_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Rimmington Farming Area");
                    goToFarmingAreaCentralTile(Constants.FarmingAreas.RIMMINGTON_AREA);
                    System.out.println("Farming Rimmington");
                    return 1000;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.RIMMINGTON_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("Rimmington is done :)");
                    watDo = "Farm Falador";
                    return 1000;
                }
                return 1000;
            case "Farm Falador":
                if(!Constants.FarmingAreas.FALADOR_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Falador Farming Area");
                    goToFarmingAreaCentralTile(Constants.FarmingAreas.FALADOR_AREA);
                    System.out.println("Farming Falador");
                    return 1000;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.FALADOR_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    if (getAPIContext().equipment().contains("Chronicle")) {
                        getAPIContext().equipment().getItem("Chronicle").interact("Check Charges");
                        Time.sleep(2_000, () -> getAPIContext().widgets().get(162).getChild(56).getChild(0).getText().contains("Your book has run out of charges"));
                        if (getAPIContext().widgets().get(162).getChild(56).getChild(0).getText().contains("Your book has run out of charges")) {
                            watDo = "Recharge chronicle";
                            return 1000;
                        }
                    }
                    System.out.println("Falador is done :)");
                    if(FarmingUtils.farmHops == true) {
                        watDo = "Farm Entrana";
                    } else if(FarmingUtils.farmBushes == true) {
                        watDo = "Farm Monastery";
                    } else {
                        watDo = "Farm Hosidius";
                    }
                    return 1000;
                }
                return 1000;
            case "Farm Entrana":
                if(FarmingUtils.farmBushes == true && GenUtils.playerisInArea(Constants.ONSHIP_PORT_SARIM)) {
                    watDo = "Farm Monastery";
                    return 1000;
                } else if(FarmingUtils.farmHops == true && GenUtils.playerisInArea(Constants.ONSHIP_PORT_SARIM)) {
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
            case "Farm Monastery":
                if(!Constants.FarmingAreas.MONASTERY_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Monastery Farming Area");
                    getAPIContext().webWalking().setUseTeleports(false);
                    goToFarmingAreaCentralTile(Constants.FarmingAreas.MONASTERY_AREA);
                    getAPIContext().webWalking().setUseTeleports(true);
                    System.out.println("Farming Monastery");
                    return 1000;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.MONASTERY_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("Monastery is done :)");
                    if(FarmingUtils.farmHops == true) {
                        watDo = "Farm Yanille";
                    } else {
                        watDo = "Farm Hosidius";
                    }
                    return 1000;
                }
                return 1000;
            case "Farm Yanille":
                if(!Constants.FarmingAreas.YANILLE_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Yanille Farming Area");
                    getAPIContext().webWalking().setUseTeleports(false);
                    goToFarmingAreaNearestTile(Constants.FarmingAreas.YANILLE_AREA);
                    getAPIContext().webWalking().setUseTeleports(true);
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
                    if(GenUtils.getBottomChatMessage().contains("You must travel to Great Kourend before")) {
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
                        getAPIContext().webWalking().setUseTeleports(false);
                        goToFarmingAreaCentralTile(Constants.FarmingAreas.HOSIDIUS_AREA);
                        getAPIContext().webWalking().setUseTeleports(true);
                    }
                    System.out.println("Farming Hosidius");
                    return 1000;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.HOSIDIUS_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("Hosidius is done :)");
                    if(FarmingUtils.farmBushes == true) {
                        watDo = "Farm Champions Guild";
                    } else if(FarmingUtils.farmHops == true) {
                        watDo = "Farm Lumbridge";
                    } else {
                        watDo = "Go to GE and complete existing offers";
                    }
                }
                return 1000;
            case "Farm Champions Guild":
                if(!Constants.FarmingAreas.CHAMPIONS_GUILD_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Champions Guild Farming Area");
                    GenUtils.teleportWithChronicle();
                    goToFarmingAreaCentralTile(Constants.FarmingAreas.CHAMPIONS_GUILD_AREA);
                    System.out.println("Farming Champions Guild");
                    return 1000;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.CHAMPIONS_GUILD_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("Champions Guild is done :)");
                    if(FarmingUtils.farmHops == true) {
                        watDo = "Farm Lumbridge";
                    } else {
                        watDo = "Go to GE and complete existing offers";
                    }
                    return 1000;
                }
                return 1000;
            case "Farm Lumbridge":
                if(!Constants.FarmingAreas.LUMBRIDGE_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Lumbridge Farming Area");
                    if(FarmingUtils.farmBushes == false) {
                        GenUtils.teleportWithChronicle();
                    }
                    goToFarmingAreaCentralTile(Constants.FarmingAreas.LUMBRIDGE_AREA);
                    System.out.println("Farming Lumbridge");
                    return 1000;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.LUMBRIDGE_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    if(FarmingUtils.farmCacti == true) {
                        watDo = "Farm Al Kharid";
                    } else {
                        watDo = "Go to GE and complete existing offers";
                    }
                }
                return 1000;
            case "Farm Al Kharid":
                if(!Constants.FarmingAreas.AL_KHARID_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    System.out.println("Walking to Al Kharid Farming Area");
                    if(FarmingUtils.farmBushes == false && FarmingUtils.farmHops == false) {
                        GenUtils.teleportWithChronicle();
                    }
                    goToFarmingAreaCentralTile(Constants.FarmingAreas.AL_KHARID_AREA);
                    System.out.println("Farming Al Kharid");
                    return 1000;
                }
                if(FarmingUtils.patchesAreFarmable()) {
                    FarmingUtils.farm(Constants.FarmingAreas.AL_KHARID_AREA, FarmingUtils.crops, FarmingUtils.compostType);
                } else {
                    System.out.println("Al Kharid is done :)");
                    watDo = "Go to GE and complete existing offers";
                    return 1000;
                }
                return 1000;

            case "Go to GE and complete existing offers":
                if(!getAPIContext().grandExchange().isOpen()) {
                    GenUtils.teleportWithChronicle();
                    getAPIContext().webWalking().setUseTeleports(false);
                    GEUtils.goToGE();
                    getAPIContext().webWalking().setUseTeleports(true);
                    GEUtils.openGE();
                    return 1200;
                } else if (GEUtils.anySaleComplete() && !getAPIContext().inventory().isFull()) {
                    getAPIContext().grandExchange().collectToInventory();
                    return 1200;
                } else {
                    for(GrandExchangeSlot slot : getAPIContext().grandExchange().getSlots()) {
                        if((slot.getState() == GrandExchangeOffer.OfferState.SELLING || slot.getState() == GrandExchangeOffer.OfferState.BUYING)) {
                            slot.abortOffer();
                            return 1200;
                        }
                        if((slot.getState() == GrandExchangeOffer.OfferState.CANCELLED_BUY || slot.getState() == GrandExchangeOffer.OfferState.CANCELLED_SELL)  && !getAPIContext().inventory().isFull()) {
                            getAPIContext().grandExchange().collectToInventory();
                            return 1200;
                        }
                    }
                }
                watDo = "Sell Produce";
                break;

            case "Sell Produce":
                if(!getAPIContext().grandExchange().isOpen()) {
                    GEUtils.openGE();
                    return 1200;
                } else if (GEUtils.anySaleComplete()) {
                    getAPIContext().grandExchange().collectToInventory();
                    return 1200;
                } else if(getAPIContext().grandExchange().getCurrentScreen() == IGrandExchangeAPI.GrandExchangeScreen.OVERVIEW) {
                    List<WidgetChild> GEInv = getAPIContext().widgets().get(467).getChild(0).getChildren();
                    for(WidgetChild item : GEInv) {
                        for(String crop : Crops.getNames()) {
                            if(item.getName().contains(crop)) {
                                for(GrandExchangeSlot slot : getAPIContext().grandExchange().getSlots()) {
                                    if(slot.getState() == GrandExchangeOffer.OfferState.EMPTY) {
                                        getAPIContext().widgets().get(465).getChild(7 + slot.getIndex()).getChild(1).interact();
                                        return 1200;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    for(WidgetChild item : getAPIContext().widgets().get(467).getChild(0).getChildren()) {
                        for(Crops crop : Crops.values()) {
                            if(getAPIContext().widgets().get(465).getChild(25).getChild(25).getText().contains(crop.getCropName())) {
                                getAPIContext().grandExchange().setPrice(getAPIContext().pricing().get(crop.getCropName()).getHighestPrice() - 1);
                                getAPIContext().grandExchange().confirmOffer();
                                getAPIContext().grandExchange().close();
                                return 1200;
                            }
                            if(item.getId() == crop.getNotedID()) {
                                item.interact();
                                return 1200;
                            }
                        }
                    }
                }
                for(Crops crop : Crops.values()) {
                    if(!getAPIContext().inventory().contains(crop.getCropName())) {
                        watDo = "Done";
                        return 1200;
                    }
                }
                break;

            case "Done":
                if(!getAPIContext().bank().isOpen()) {
                    getAPIContext().webWalking().setUseTeleports(false);
                    BankUtils.goToClosestBank();
                    BankUtils.openBank();
                } else {
                    BankUtils.depositInventory();
//                    getAPIContext().bank().depositAllExcept("Camelot teleport");
                    getAPIContext().bank().depositEquipment();
                    BankUtils.closeBank();
                    GenUtils.logOut();
                }

            default:
                break;
        }
        return 500;
    }
}

