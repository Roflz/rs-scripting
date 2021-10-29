import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.*;
import com.epicbot.api.shared.entity.details.Locatable;
import com.epicbot.api.shared.event.ChatMessageEvent;
import com.epicbot.api.shared.methods.IBankAPI;
import com.epicbot.api.shared.methods.IEquipmentAPI;
import com.epicbot.api.shared.methods.IMouseAPI;
import com.epicbot.api.shared.methods.ITabsAPI;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Skill;
import com.epicbot.api.shared.model.Spell;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.model.ge.GrandExchangeOffer;
import com.epicbot.api.shared.model.ge.GrandExchangeSlot;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.Random;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.SleepTimer;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.webwalking.model.RSBank;
import com.sun.org.apache.bcel.internal.generic.GETFIELD;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import javafx.util.Pair;
import sun.security.action.GetBooleanAction;

import com.epicbot.api.shared.entity.SceneObject;


import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@ScriptManifest(name = "Farmer", gameType = GameType.OS)
public class main extends LoopScript {

    private final Area CATHERBY_AREA = new Area(new Tile(2804, 3469, 0), new Tile(2815, 3458, 0));
    private final Area ARDOUGNE_AREA = new Area(new Tile(2660, 3381, 0), new Tile(2674, 3368, 0));
    private final Area FALADOR_AREA = new Area(new Tile(3049, 3313, 0), new Tile(3061, 3301, 0));
    private final Area HOSIDIUS_AREA = new Area(new Tile(3049, 3313, 0), new Tile(3061, 3301, 0));
    private final Area LUMBRIDGE_AREA = new Area(new Tile(3232, 3318, 0), new Tile(3225, 3311, 0));
    private final Area MCGRUBOR_AREA = new Area(new Tile(2672, 3520, 0), new Tile(2662, 3530, 0));

    private final Area FISHING_GUILD_TELEPORT_AREA = new Area(new Tile(2599, 3393, 0), new Tile(2619, 3383, 0));
    private final Area CAMELOT_TELEPORT_AREA = new Area(new Tile(2760, 3475, 0), new Tile(2752, 3481, 0));
    private final Area CRAFTING_GUILD_TELEPORT_AREA = new Area(new Tile(2927, 3298, 0), new Tile(2940, 3285, 0));
    private final Area WOODCUTTING_GUILD_TELEPORT_AREA = new Area(new Tile(2927, 3298, 0), new Tile(2940, 3285, 0));
    private final Area CHRONICLE_TELEPORT_AREA = new Area(new Tile(3195, 3349, 0), new Tile(3207, 3363, 0));

    private final Area GRAND_EXCHANGE_AREA = new Area(new Tile(3157, 3482, 0), new Tile(3172, 3495, 0));


    private boolean GE_OFFERS_COLLECTED = false;
    private boolean GE_OFFERS_MADE = false;
    private boolean BUY_STARTING_ITEMS = false;
    private boolean BANK_DONE = false;
    private boolean CATHERBY_DONE = false;
    private boolean MCGRUBOR_DONE = false;
    private boolean ARDOUGNE_DONE = false;
    private boolean FALADOR_DONE = false;
    private boolean HOSIDIUS_DONE = false;
    private boolean LUMBRIDGE_DONE = false;


    private List<String> trash = Arrays.asList("Weeds", "Bucket", "Vial");
    private List<String> patchTypes = Arrays.asList("Flower Patch", "Allotment", "Herb patch", "Hops patch");
    java.util.List < String > listObject = Arrays.asList();
    private java.util.ArrayList < String > shoppingList = new ArrayList < > (listObject);
    private List<Items> startingItems = Arrays.asList(Items.SPADE, Items.RAKE, Items.SEED_DIBBER, Items.SKILLS_NECKLACE, Items.CAMELOT_TELEPORT);

    private StoreItems compostType = StoreItems.COMPOST;
    private StoreItems cure = StoreItems.PLANT_CURE;
    private Crops allotmentCrop = Crops.TOMATO;
    private Crops flowerCrop = Crops.MARIGOLD;
    private Crops herbCrop = Crops.GUAM;
    private Crops hopsCrop = Crops.BARLEY;

    private boolean harvesting = false;
    private boolean clearing = false;

    public enum Items {
        SPADE ("Spade", 952),
        RAKE ("Rake", 5341),
        SEED_DIBBER ("Seed dibber", 5343),
        SKILLS_NECKLACE ("Skills necklace(6)", 11968),
        CAMELOT_TELEPORT ("Camelot teleport", 8010);


        private final String name;
        private final int itemID;

        Items(String name, int itemID) {
            this.name = name;
            this.itemID = itemID;
        }
    }

    public enum StoreItems {
        COMPOST ("Compost", 17, 10),
        PLANT_CURE ("Plant cure", 14, 7);

        private final String name;
        private final int widgetID;
        private final int inventoryWidgetID;

        StoreItems(String name, int widgetID, int inventoryWidgetID) {
            this.name = name;
            this.widgetID = widgetID;
            this.inventoryWidgetID = inventoryWidgetID;
        }
    }

    public enum Crops {
        //Allotments
        POTATO ("Potato seed", "Potato", "Diseased potatoes", "Dead potatoes", "Allotment", 1942, 1943),
        CABBAGE ("Cabbage seed", "Cabbage", "Diseased cabbage", "Dead cabbage", "Allotment", 1942, 1943),
        TOMATO ("Tomato seed", "Tomato", "Diseased tomatoes", "Dead tomatoes", "Allotment",1982, 1983),
        //Flowers
        MARIGOLD ("Marigold seed", "Marigold", "Diseased marigold", "Dead marigold", "Flower Patch", 6010, 6011),
        //Herbs
        GUAM ("Guam seed", "Herbs", "Diseased herbs", "Dead herbs", "Herb patch", 199, 200),
        //Hops
        BARLEY ("Barley seed", "Barley", "Diseased Barley", "Dead Barley", "Hops patch", 6006, 6007);

        private final String seedName;
        private final String cropName;
        private final String diseasedName;
        private final String deadName;
        private final String patchType;
        private final int unNotedID;
        private final int notedID;

        Crops(String seedName, String cropName, String diseasedName, String deadName, String patchType, int unNotedID, int notedID) {
            this.seedName = seedName;
            this.cropName = cropName;
            this.diseasedName = diseasedName;
            this.deadName = deadName;
            this.patchType = patchType;
            this.unNotedID = unNotedID;
            this.notedID = notedID;
        }

        public List<Integer> getUnNotedIDs() {
            List<Integer> unNotedIDs = new ArrayList<Integer>();
            for(Crops crop : Crops.values()) {
                unNotedIDs.add(crop.unNotedID);
            }
            return unNotedIDs;
        }

    }

    public boolean inventoryContainsTrash() {
        for(String item : trash) {
            if(getAPIContext().inventory().contains(item)) {
                return true;
            }
        } return false;
    }

    public void dropTrash() {
        for(String item : trash) {
            getAPIContext().inventory().dropAll(item);
        }
    }

    public void turnCameraTowards(SceneObject patch) {
        getAPIContext().camera().turnTo(patch.getLocation());
    }

    public void turnCameraTowards(NPC npc) {
        getAPIContext().camera().turnTo(npc.getLocation());
    }

    public void goToGEwithTeleportsFromBank() {
        if(!getAPIContext().bank().isOpen()) {
            getAPIContext().bank().open();
            Time.sleep(5_000, () -> getAPIContext().bank().isOpen());
        }
        if(!getAPIContext().equipment().contains("Chronicle", "Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)",
                "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)")) {
            closeBank();
            getAPIContext().webWalking().walkTo(RSBank.GRAND_EXCHANGE.getTile());
        } else if (getAPIContext().bank().containsAll("Air rune", "Law rune", "Fire rune") && getAPIContext().skills().magic().getCurrentLevel() > 24) {
            getAPIContext().bank().withdrawAll("Air rune", "Law rune", "Fire rune");
            closeBank();
            getAPIContext().webWalking().walkTo(RSBank.GRAND_EXCHANGE.getTile());
        } else if (getAPIContext().bank().contains("Varrock teleport", "Ring of wealth")) {
            getAPIContext().bank().withdraw(1, "Varrock teleport", "Ring of wealth");
            closeBank();
            getAPIContext().webWalking().walkTo(RSBank.GRAND_EXCHANGE.getTile());
        } else {
            closeBank();
            getAPIContext().webWalking().walkTo(RSBank.GRAND_EXCHANGE.getTile());
        }
    }

    public void getMore(StoreItems item) {
        goToClosestBank();
        openBank();
        Time.sleep(5_000, () -> getAPIContext().bank().isOpen());
        if(getAPIContext().bank().contains(item.name)) {
            getAPIContext().bank().selectWithdrawMode(IBankAPI.WithdrawMode.NOTE);
            getAPIContext().bank().withdraw(1000, item.name);
            closeBank();
        } else {
            goToGEwithTeleportsFromBank();
            Time.sleep(5_000, () -> GRAND_EXCHANGE_AREA.contains(getAPIContext().localPlayer().getLocation()));
            openBank();
            Time.sleep(5_000, () -> getAPIContext().bank().isOpen());
            getAPIContext().bank().withdrawAll("Coins");
            closeBank();
            Time.sleep(5_000, () -> !getAPIContext().bank().isOpen());
            buyItemFromGE(item.name, 100);
            waitAndCollectOfferFromGE(item.name);
            openBank();
            Time.sleep(5_000, () -> getAPIContext().bank().isOpen());
            getAPIContext().bank().depositAll("Coins");
            getAPIContext().bank().withdraw(10_000, "Coins");
            getAPIContext().bank().selectWithdrawMode(IBankAPI.WithdrawMode.NOTE);
            getAPIContext().bank().withdraw(1000, item.name);
            closeBank();
        }
    }

    public void getItemFromLeprechaun(StoreItems item, int numberOfItems) {
        NPC leprechaun = getAPIContext().npcs().query().named("Tool Leprechaun").results().nearest();
        turnCameraTowards(leprechaun);
        leprechaun.interact("Exchange");
        Time.sleep(10_000, () -> getAPIContext().widgets().isInterfaceOpen());
        WidgetChild itemWidget = getAPIContext().widgets().get(125).getChild(item.widgetID);
        if(getAPIContext().widgets().get(125).getChild(item.widgetID).getChild(10).getText().contains("0/1000")) {
            getAPIContext().widgets().closeInterface();
            Time.sleep(5_000, () -> !getAPIContext().widgets().isInterfaceOpen());
            System.out.println("Going to get more " + item.name);
            getMore(item);
            Area area = new Area( new Tile(leprechaun.getX() + 4, leprechaun.getY() + 4), new Tile(leprechaun.getX() - 4, leprechaun.getY() - 4));
            getAPIContext().webWalking().walkTo(area.getNearestTile(APIContext.get()));
            leprechaun.interact("Exchange");
            Time.sleep(10_000, () -> getAPIContext().widgets().get(126).isVisible());
            WidgetChild inventoryWidget = getAPIContext().widgets().get(126).getChild(item.inventoryWidgetID);
            inventoryWidget.interact("Store-All");
        }
        for(int i = 1; i <= numberOfItems; i++) {
            itemWidget.interact("Remove-1");
            Time.sleep(1_000);
        }
        getAPIContext().widgets().closeInterface();
        Time.sleep(5_000, () -> !getAPIContext().widgets().isInterfaceOpen());
    }

    public void compost(SceneObject patch) {
        clearing = false;
        harvesting = false;
        if(!getAPIContext().inventory().contains(compostType.name)) {
            getItemFromLeprechaun(compostType, 4);
        }
        turnCameraTowards(patch);
        getAPIContext().inventory().selectItem(compostType.name);
        patch.interact();
        System.out.println("Composting " + patch.getName());
        Time.sleep(7_000, () -> getAPIContext().localPlayer().isAnimating());
        Time.sleep(1_000);
        //if(getAPIContext().localPlayer().getMessage())
    }

    public void rake(SceneObject patch) {
        clearing = false;
        harvesting = false;
        turnCameraTowards(patch);
        patch.interact("Rake");
        System.out.println("Raking " + patch.getName());
        Time.sleep(7_000, () -> getAPIContext().localPlayer().isAnimating());
    }

    public void plant(String seed, SceneObject patch) {
        clearing = false;
        harvesting = false;
        turnCameraTowards(patch);
        getAPIContext().inventory().selectItem(seed);
        patch.interact();
        System.out.println("Planting " + seed + " in " + patch.getName());
        Time.sleep(10_000, () -> getAPIContext().localPlayer().isAnimating());
    }

    public int harvest(SceneObject produce) {
        clearing = false;
        turnCameraTowards(produce);
        if(produce.hasAction("Harvest")) {
            produce.interact("Harvest");
            System.out.println("Harvesting " + produce.getName());
            harvesting = true;
        } else if(produce.hasAction("Pick")) {
            produce.interact("Pick");
            System.out.println("Picking " + produce.getName());
            harvesting = true;
        }
        return 500;
    }

    public void clear(SceneObject deadCrops) {
        harvesting = false;
        turnCameraTowards(deadCrops);
        deadCrops.interact("Clear");
        System.out.println("Clearing " + deadCrops.getName());
        clearing = true;
    }

    public void cure(SceneObject diseasedCrops) {
        clearing = false;
        harvesting = false;
        if(!getAPIContext().inventory().contains("Plant cure")) {
            getItemFromLeprechaun(cure, 1);
        }
        turnCameraTowards(diseasedCrops);
        diseasedCrops.interact("Cure");
        System.out.println("Cured " + diseasedCrops.getName());
        Time.sleep(10_000, () -> getAPIContext().localPlayer().isAnimating());
    }

    public void noteProduce(List<Integer> unNotedIDs) {
        clearing = false;
        harvesting = false;
        NPC leprechaun = getAPIContext().npcs().query().named("Tool Leprechaun").results().nearest();
        turnCameraTowards(leprechaun);
        for(int unNotedID : unNotedIDs) {
            if(getAPIContext().inventory().contains(unNotedID)) {
                getAPIContext().inventory().selectItem(unNotedID);
                leprechaun.interact();
                System.out.println("Noting Crops");
                Time.sleep(2_000, () -> !getAPIContext().inventory().contains(unNotedID));
            }
        }
    }


    public void farm(Crops crop, String area) {
        List<SceneObject> allPatches = getAPIContext().objects().query().actions("Cure", "Pick", "Harvest", "Rake", "Clear").notNamed("Wheat", "Cabbage", "Potato").results().toList();
        SceneObject patch = getAPIContext().objects().query().named(crop.patchType).results().nearest();
        SceneObject produce = getAPIContext().objects().query().actions("Harvest", "Pick").notNamed("Wheat", "Cabbage", "Potato").results().nearest();
        SceneObject diseasedProduce = getAPIContext().objects().query().actions("Cure").results().nearest();
        SceneObject deadProduce = getAPIContext().objects().query().actions("Clear").results().nearest();
        if(!this.isFarming()) {
            if(getAPIContext().inventory().isFull()) {
                noteProduce(crop.getUnNotedIDs());
            } else if(patch != null && patch.hasAction("Rake")) {
                rake(patch);
            } else if(patch != null && !patch.hasAction("Rake")) {
                compost(patch);
                plant(crop.seedName, patch);
            } else if(produce != null && produce.hasAction("Harvest", "Pick") && harvesting == false) {
                harvest(produce);
            } else if(diseasedProduce != null && diseasedProduce.hasAction("Cure")) {
                cure(diseasedProduce);
            } else if(deadProduce != null && deadProduce.hasAction("Clear") && clearing == false) {
                clear(deadProduce);
            } else if(allPatches.isEmpty()) {
                done(area);
            }
        }
    }

    public void goToClosestBank() {
        getAPIContext().webWalking().walkToBank();
    }

    public void goToBank(RSBank bank) {
        getAPIContext().webWalking().walkToBank(bank);
    }

    public void openBank() {
        getAPIContext().bank().open();
    }

    public void closeBank() {
        getAPIContext().bank().close();
    }

    public void depositInventory() {
        getAPIContext().bank().depositInventory();
    }


    public void withdrawStartingItems() {
        System.out.println("Depositing all items");
        getAPIContext().bank().depositInventory();
        getAPIContext().bank().depositEquipment();
        System.out.println("Withdrawing starting items");
        ItemWidget necklace =  getAPIContext().bank().getItem("Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)",
                "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)");
        getAPIContext().bank().withdraw(1, necklace.getName());
        getAPIContext().inventory().interactItem("Wear", necklace.getName());
        getAPIContext().bank().withdraw(50, allotmentCrop.seedName);
        getAPIContext().bank().withdraw(10, flowerCrop.seedName);
        getAPIContext().bank().withdraw(10, herbCrop.seedName);
        getAPIContext().bank().withdraw(50, hopsCrop.seedName);
        getAPIContext().bank().withdraw(1,"Camelot teleport");
        getAPIContext().bank().withdraw(1, "Seed dibber");
        getAPIContext().bank().withdraw(1, "Rake");
        getAPIContext().bank().withdraw(1, "Spade");
        getAPIContext().bank().withdraw(10000, "Coins");
    }

    public boolean doesBankHaveStartingItems() {
        if (getAPIContext().bank().getCount(allotmentCrop.seedName) > 50 && getAPIContext().bank().getCount(flowerCrop.seedName) > 10 &&
                getAPIContext().bank().getCount(herbCrop.seedName) > 10 && getAPIContext().bank().getCount(hopsCrop.seedName) > 50 &&
                getAPIContext().bank().containsAll(allotmentCrop.seedName, flowerCrop.seedName, herbCrop.seedName, hopsCrop.seedName, "Camelot teleport",
                        "Seed dibber", "Rake", "Spade", "Coins") &&
                getAPIContext().bank().contains("Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)",
                                                "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)")) {
            return true;
        } else {
            if(getAPIContext().bank().getCount(allotmentCrop.seedName) < 50) {
                shoppingList.add(allotmentCrop.seedName);
            }
            if(getAPIContext().bank().getCount(flowerCrop.seedName) < 10) {
                shoppingList.add(flowerCrop.seedName);
            }
            if(getAPIContext().bank().getCount(herbCrop.seedName) < 10) {
                shoppingList.add(herbCrop.seedName);
            }
            if(getAPIContext().bank().getCount(hopsCrop.seedName) < 50) {
                shoppingList.add(hopsCrop.seedName);
            }
            if(!getAPIContext().bank().contains("Camelot teleport")) {
                shoppingList.add("Camelot teleport");
            }
            if(!getAPIContext().bank().contains("Seed dibber")) {
                shoppingList.add("Seed dibber");
            }
            if(!getAPIContext().bank().contains("Rake")) {
                shoppingList.add("Rake");
            }
            if(!getAPIContext().bank().contains("Spade")) {
                shoppingList.add(0, "Spade");
            }
            if(!getAPIContext().bank().contains("Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)",
                    "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)")) {
                shoppingList.add("Skills necklace(6)");
            }
            return false;
        }
    }

    public void buyItemFromGE(String item, int quantity) {
        if(!getAPIContext().grandExchange().isOpen()) {
            getAPIContext().grandExchange().open();
            Time.sleep(5_000, () -> getAPIContext().grandExchange().isOpen());
        }
        if(getAPIContext().grandExchange().isOpen()) {
            getAPIContext().grandExchange().newBuyOffer(item);
            getAPIContext().grandExchange().increasePriceBy5Percent();
            getAPIContext().grandExchange().setQuantity(quantity);
            getAPIContext().grandExchange().confirmOffer();
        }
        waitAndCollectOfferFromGE(item);
        getAPIContext().grandExchange().close();
    }

    public void buyStartingItemsFromGE() {
        for(String item : shoppingList ) {
            getAPIContext().grandExchange().newBuyOffer(item);
            getAPIContext().grandExchange().increasePriceBy5Percent();
            if(item == allotmentCrop.seedName) {
                getAPIContext().grandExchange().setQuantity(500);
            } else if(item == herbCrop.seedName) {
                getAPIContext().grandExchange().setQuantity(50);
            } else if(item == flowerCrop.seedName) {
                getAPIContext().grandExchange().setQuantity(100);
            } else if(item == hopsCrop.seedName) {
                getAPIContext().grandExchange().setQuantity(350);
            } else if(item == "Rake" || item == "Seed dibber" || item == "Spade") {
                getAPIContext().grandExchange().setQuantity(1);
            } else if(item == "Camelot teleport") {
                getAPIContext().grandExchange().setQuantity(100);
            } else if(item == "Skills necklace(6)") {
                getAPIContext().grandExchange().setQuantity(10);
            }
            getAPIContext().grandExchange().confirmOffer();
        }
    }

    public void waitAndCollectOfferFromGE(String item) {
        List<GrandExchangeSlot> slots = getAPIContext().grandExchange().getSlots();
        for(GrandExchangeSlot slot : slots) {
            WidgetChild slotWidget = getAPIContext().widgets().get(465).getChild(slot.getIndex() + 7).getChild(19);
            if(slotWidget.getText().contains(item) && !slot.isCompleted()) {
                while(!slot.isCompleted()) {
                    System.out.println("Waiting for GE purchase completion ");
                    if(slot.isCompleted()) {
                        getAPIContext().grandExchange().collectToBank();
                    }
                    if(slot.getState() == GrandExchangeOffer.OfferState.EMPTY) {
                        break;
                    }
                    Time.sleep(5_000);
                }
            } else if(slotWidget.getText().contains(item) && slot.isCompleted()) {
                getAPIContext().grandExchange().collectToBank();
            }
        }
    }

    public void waitAndCollectStartingOffersFromGE() {
        List<GrandExchangeSlot> slots = getAPIContext().grandExchange().getSlots();
        for(GrandExchangeSlot slot : slots) {
            WidgetChild slotWidget = getAPIContext().widgets().get(465).getChild(slot.getIndex() + 7).getChild(19);
            for(Items item : startingItems) {
                if(slotWidget.getText().contains(item.name) && slot.isCompleted()) {
                    getAPIContext().grandExchange().collectToBank();
                }
            }
        }
        Time.sleep(5_000);
    }

    public void done(String area) {
        if(area == "Bank") {
            BANK_DONE = true;
        } else if(area == "Catherby") {
            CATHERBY_DONE = true;
        } else if(area == "Ardougne") {
            ARDOUGNE_DONE = true;
        } else if(area == "Falador") {
            FALADOR_DONE = true;
        } else if(area == "Hosidius") {
            HOSIDIUS_DONE = true;
        } else if(area == "Lumbridge") {
            LUMBRIDGE_DONE = true;
        } else if(area == "McGrubor") {
            MCGRUBOR_DONE = true;
        }
    }

    public void teleportWithSkillsNecklace(String destination) {
        ItemWidget necklace = getAPIContext().equipment().getItem(IEquipmentAPI.Slot.NECK);
        if(necklace.getName().contains("Skills necklace(")) {
            if(!FISHING_GUILD_TELEPORT_AREA.contains(getAPIContext().localPlayer().getLocation()) && destination == "Fishing Guild") {
                necklace.interact("Fishing Guild");
                Time.sleep(10_000, () -> FISHING_GUILD_TELEPORT_AREA.contains(getAPIContext().localPlayer().getLocation()));
            } else if(!CRAFTING_GUILD_TELEPORT_AREA.contains(getAPIContext().localPlayer().getLocation()) && destination == "Crafting Guild") {
                necklace.interact("Crafting Guild");
                Time.sleep(10_000, () -> CRAFTING_GUILD_TELEPORT_AREA.contains(getAPIContext().localPlayer().getLocation()));
            } else if(!WOODCUTTING_GUILD_TELEPORT_AREA.contains(getAPIContext().localPlayer().getLocation()) && destination == "Woodcutting Guild") {
                necklace.interact("Woodcutting Guild");
                Time.sleep(10_000, () -> WOODCUTTING_GUILD_TELEPORT_AREA.contains(getAPIContext().localPlayer().getLocation()));
            }
        }
    }

    public void teleportWithChronicle() {
        ItemWidget shield = getAPIContext().equipment().getItem(IEquipmentAPI.Slot.SHIELD);
        if(shield.getName().contains("Chronicle")) {
            shield.interact("Teleport");
            Time.sleep(10_000, () -> CHRONICLE_TELEPORT_AREA.contains(getAPIContext().localPlayer().getLocation()));
        }
    }

    public void logOut() {
        if (this.getAPIContext().bank().isOpen()) {
            this.getAPIContext().bank().close();
        }

        this.getAPIContext().game().logout();
        this.getAPIContext().script().stop("Done :)");
    }

    public boolean isFarming() {
        return getAPIContext().localPlayer().isAnimating() || getAPIContext().localPlayer().isMoving();
    }

    @Override
    public boolean onStart(String... strings) {
        System.out.println("Lets Farm!");
        return true;
    }

    @Override
    protected int loop() {
        if (!getAPIContext().client().isLoggedIn()) {
            System.out.println("Waiting til log in :)");
            return 300;
        }

        if(getAPIContext().walking().getRunEnergy() > 50 && !getAPIContext().walking().isRunEnabled()) {
            getAPIContext().walking().setRun(true);
        }

        if(inventoryContainsTrash() && !isFarming()) {
            dropTrash();
        }

        if(getAPIContext().dialogues().canContinue()) {
            getAPIContext().dialogues().selectContinue();
            harvesting = false;
            return 1000;
        }

        if(BUY_STARTING_ITEMS == true) {
            if(!GRAND_EXCHANGE_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                if(!getAPIContext().bank().isOpen()) {
                    openBank();
                    Time.sleep(5_000, () -> getAPIContext().bank().isOpen());
                }
                if (getAPIContext().bank().containsAll("Air rune", "Law rune", "Fire rune") && getAPIContext().skills().magic().getCurrentLevel() > 24) {
                    getAPIContext().bank().withdrawAll("Air rune", "Law rune", "Fire rune");
                } else if (getAPIContext().bank().contains("Varrock teleport", "Ring of wealth")) {
                    getAPIContext().bank().withdraw(1, "Varrock teleport", "Ring of wealth");
                }
                closeBank();
                getAPIContext().webWalking().walkTo(RSBank.GRAND_EXCHANGE.getTile());
            } else if(!getAPIContext().bank().isOpen() && !getAPIContext().inventory().contains("Coins")) {
                openBank();
                Time.sleep(5_000, () -> getAPIContext().bank().isOpen());
                getAPIContext().bank().withdrawAll("Coins");
                closeBank();
            } else if(!getAPIContext().grandExchange().isOpen()) {
                getAPIContext().grandExchange().open();
            }
            if(getAPIContext().grandExchange().isOpen()) {
                buyStartingItemsFromGE();
                BUY_STARTING_ITEMS = false;
                GE_OFFERS_MADE = true;
            }
        }

        if(GE_OFFERS_MADE == true) {
            if(getAPIContext().grandExchange().isOpen()) {
                waitAndCollectStartingOffersFromGE();
                getAPIContext().grandExchange().close();
                Time.sleep(5_000, () -> !getAPIContext().grandExchange().isOpen());
                getAPIContext().bank().open();
                Time.sleep(5_000, () -> !getAPIContext().bank().isOpen());
            } else if(getAPIContext().bank().isOpen()) {
                if(doesBankHaveStartingItems()) {
                    GE_OFFERS_MADE = false;
                    GE_OFFERS_COLLECTED = true;
                } else {
                    Time.sleep(2_000);
                    getAPIContext().bank().close();
                    Time.sleep(5_000, () -> !getAPIContext().bank().isOpen());
                    getAPIContext().grandExchange().open();
                    Time.sleep(5_000, () -> getAPIContext().grandExchange().isOpen());
                }
            }
        }

        if((BANK_DONE == false || GE_OFFERS_COLLECTED == true) && BUY_STARTING_ITEMS == false && GE_OFFERS_MADE == false) {
            getAPIContext().camera().setPitch(71);
            if(!getAPIContext().bank().isOpen()) {
                goToClosestBank();
                openBank();
                Time.sleep(5_000, () -> getAPIContext().bank().isOpen());
                if(GE_OFFERS_MADE == false) {
                    depositInventory();
                }
            } else if(doesBankHaveStartingItems()) {
                withdrawStartingItems();
                closeBank();
                BANK_DONE = true;
            } else if(GE_OFFERS_MADE == false) {
                BUY_STARTING_ITEMS = true;
            }
        }

        if (!CATHERBY_AREA.contains(getAPIContext().localPlayer().getLocation()) && CATHERBY_DONE == false && BANK_DONE == true) {
            System.out.println("Traveling to Catherby Farming Area");
            getAPIContext().webWalking().walkTo(CATHERBY_AREA.getCentralTile());
        }

        if (CATHERBY_AREA.contains(getAPIContext().localPlayer().getLocation()) && CATHERBY_DONE == false && BANK_DONE == true) {
            farm(allotmentCrop, "Catherby");
            farm(flowerCrop, "Catherby");
            farm(herbCrop, "Catherby");
        }

        if (!MCGRUBOR_AREA.contains(getAPIContext().localPlayer().getLocation()) && CATHERBY_DONE == true && MCGRUBOR_DONE == false) {
            System.out.println("Walking to McGrubor Farming Area");
            getAPIContext().webWalking().walkTo(MCGRUBOR_AREA.getNearestTile(APIContext.get()));
        }

        if (MCGRUBOR_AREA.contains(getAPIContext().localPlayer().getLocation()) && CATHERBY_DONE == true && MCGRUBOR_DONE == false) {
            farm(hopsCrop, "McGrubor");
        }

        if (!ARDOUGNE_AREA.contains(getAPIContext().localPlayer().getLocation()) && MCGRUBOR_DONE == true && ARDOUGNE_DONE == false) {
            //teleportWithSkillsNecklace("Fishing Guild");
            System.out.println("Walking to Ardougne Farming Area");
            getAPIContext().webWalking().walkTo(ARDOUGNE_AREA.getCentralTile());
        }

        if (ARDOUGNE_AREA.contains(getAPIContext().localPlayer().getLocation()) && MCGRUBOR_DONE == true && ARDOUGNE_DONE == false) {
            farm(allotmentCrop, "Ardougne");
            farm(flowerCrop, "Ardougne");
            farm(herbCrop, "Ardougne");
        }

        if (!FALADOR_AREA.contains(getAPIContext().localPlayer().getLocation()) && ARDOUGNE_DONE == true && FALADOR_DONE == false) {
            //teleportWithSkillsNecklace("Crafting Guild");
            System.out.println("Walking to Falador Farming Area");
            getAPIContext().webWalking().walkTo(FALADOR_AREA.getCentralTile());
        }

        if (FALADOR_AREA.contains(getAPIContext().localPlayer().getLocation()) && ARDOUGNE_DONE == true && FALADOR_DONE == false) {
            farm(allotmentCrop, "Falador");
            farm(flowerCrop, "Falador");
            farm(herbCrop, "Falador");
        }

//        if (!HOSIDIUS_AREA.contains(getAPIContext().localPlayer().getLocation()) && FALADOR_DONE == true && HOSIDIUS_DONE == false) {
//            teleportWithSkillsNecklace("Woodcutting Guild");
//            System.out.println("Walking to Hosidius Farming Area");
//            getAPIContext().webWalking().walkTo(HOSIDIUS_AREA.getCentralTile());
//        }
//
//        if (HOSIDIUS_AREA.contains(getAPIContext().localPlayer().getLocation()) && FALADOR_DONE == true && HOSIDIUS_DONE == false) {
//            farm("Allotment", allotmentCrop, "Hosidius");
//            farm("Flower Patch", flowerCrop, "Hosidius");
//            farm("Herb patch", herbCrop, "Hosidius");
//        }

        if (!LUMBRIDGE_AREA.contains(getAPIContext().localPlayer().getLocation()) && FALADOR_DONE == true && LUMBRIDGE_DONE == false) {
            //teleportWithChronicle();
            System.out.println("Walking to Lumbridge Farming Area");
            getAPIContext().webWalking().walkTo(LUMBRIDGE_AREA.getNearestTile(APIContext.get()));
        }

        if (LUMBRIDGE_AREA.contains(getAPIContext().localPlayer().getLocation()) && FALADOR_DONE == true && LUMBRIDGE_DONE == false) {
            farm(hopsCrop, "Lumbridge");
        }

        if(LUMBRIDGE_DONE == true) {
            if(!getAPIContext().bank().isOpen()) {
                goToClosestBank();
                openBank();
            } else {
                depositInventory();
                closeBank();
                logOut();
            }
        }



        return 1000;
    }


}