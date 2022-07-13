import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.GroundItem;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.methods.IQuestAPI;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.time.Time;
import com.sun.org.apache.xerces.internal.xinclude.MultipleScopeNamespaceSupport;
import javafx.scene.Scene;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ScriptManifest(name = "Digsite", gameType = GameType.OS)
public class main extends LoopScript {

    public static String watDo = "";

    private List<String> startingItemsList = Arrays.asList("Pestle and mortar", "Vial", "Tinderbox", "Cup of tea", "Rope", "Uncut opal", "Charcoal", "Varrock teleport");
    private List<Integer> startingQuantitiesList = Arrays.asList(1, 1, 1, 1, 2, 1, 1, 2);
    public static List<String> shoppingListItems = new ArrayList();
    public static List<Integer> shoppingListQuantities = new ArrayList();
    private int geBuyCounter = 0;
    private int gePriceIncreaseCounter = 1;

    @Override
    protected int loop() {

        System.out.println(watDo);
        switch (watDo) {
            case "":
                getAPIContext().camera().setPitch(98);
                getAPIContext().mouse().move(getAPIContext().game().getCenterSceneTile().getCentralPoint());
                getAPIContext().mouse().scroll(false, 20);
                watDo = "Bank";
                return 500;
            case "Bank":
                if (!getAPIContext().bank().isOpen()  && !InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                    System.out.println("Going to pick up starting items");
                    BankUtils.goToClosestBank();
                    BankUtils.openBank();
                    BankUtils.depositInventoryIfNotEmpty();
                    return 500;
                } else if(InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)){
                    watDo = "Start Quest";
                    return 1000;
                }
                if (getAPIContext().bank().isOpen()) {
                    System.out.println("banks open");
                    if (BankUtils.doesBankHaveItemsWithQuantity(startingItemsList, startingQuantitiesList) || InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                        System.out.println("Withdrawing starting items from Bank");
                        BankUtils.depositInventoryIfNotEmpty();
                        getAPIContext().bank().depositEquipment();
                        BankUtils.withdrawItemsFromListWithQuantity(startingItemsList, startingQuantitiesList);
                        watDo = "Start Quest";
                        return 500;
                    } else {
                        BankUtils.depositInventoryIfNotEmpty();
                        getAPIContext().bank().withdrawAll("Coins");
                        shoppingListItems = GEUtils.createShoppingListItems(startingItemsList, startingQuantitiesList);
                        shoppingListQuantities = GEUtils.createShoppingListQuantities(startingItemsList, startingQuantitiesList);
                        getAPIContext().bank().close();
                        System.out.println("Bank does not have items, so going to buy them from the GE");
                        watDo = "Buy items from GE";
                        return 1000;
                    }
                }
                break;
            case "Buy items from GE":
                if (!getAPIContext().grandExchange().isOpen()) {
                    if (!Constants.GRAND_EXCHANGE_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                        GenUtils.teleportWithChronicle();
                        GEUtils.goToGE();
                        return 500;
                    } else if (!getAPIContext().inventory().contains("Coins")) {
                        getAPIContext().bank().open();
                        BankUtils.withdrawAllCoins();
                        getAPIContext().bank().close();
                        return 500;
                    } else {
                        getAPIContext().grandExchange().open();
                        return 500;
                    }
                }
                if (getAPIContext().grandExchange().isOpen()) {
                    if (!GEUtils.allSalesComplete(shoppingListItems)) {
                        GEUtils.buyItemsFromShoppingList(shoppingListItems, shoppingListQuantities, 1000);
                        watDo = "Wait for sales from GE";
                        return 500;
                    }
                }
                break;
            case "Wait for sales from GE":
                if (GEUtils.anySaleNotYetComplete(shoppingListItems) && shoppingListItems.size() > 0 && gePriceIncreaseCounter <= 10) {
                    if (geBuyCounter >= 10) {
                        int i = 0;
                        for (String item : GEUtils.getSalesNotYetCompleteWithNames(shoppingListItems)) {
                            GEUtils.abortOffer(GEUtils.getGESlotWithItem(item));
                            GEUtils.makeBuyOffer(item, getItemQuantity(item), gePriceIncreaseCounter);
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
                if (GEUtils.allSalesComplete(shoppingListItems) || shoppingListItems.size() == 0) {
                    getAPIContext().grandExchange().collectToBank();
                    getAPIContext().grandExchange().close();
                    watDo = "Bank";
                    return 500;
                }
                break;

            case "Start Quest":
                if(getAPIContext().quests().isStarted(IQuestAPI.Quest.THE_DIG_SITE)) {
                    watDo = "Go to Varrock Museum";
                    return 1000;
                }
                if(!Constants.EXAM_CENTRE.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.EXAM_CENTRE.getCentralTile());
                    return 1000;
                } else {
                    NPC examiner = getAPIContext().npcs().query().named("Examiner").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        examiner.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption(0)) {
                            return 1000;
                        }
                    }
                }
                break;

            case "Go to Varrock Museum":
                if(!Constants.VARROCK_MUSEUM.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().setUseTeleports(false);
                    getAPIContext().webWalking().walkTo(Constants.VARROCK_MUSEUM.getCentralTile());
                    getAPIContext().webWalking().setUseTeleports(true);
                    return 1000;
                } else {
                    NPC curator = getAPIContext().npcs().query().named("Curator Haig Halen").results().nearest();
                    if (!getAPIContext().dialogues().isDialogueOpen() && !getAPIContext().inventory().contains("Sealed letter")) {
                        curator.interact("Talk-to");
                        Time.sleep(7_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else if(!getAPIContext().inventory().contains("Sealed letter")) {
                        if (getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        } else if (getAPIContext().dialogues().selectOption(0)) {
                            return 1000;
                        }
                    }
                    if(getAPIContext().inventory().contains("Sealed letter")) {
                        watDo = "Take exam";
                    }
                }
                break;

            case "Take exam":
                if(getAPIContext().dialogues().getText().contains("Why don't you use the resources here?")) {
                    watDo = "Get Teddy Bear";
                    return 1000;
                }
                if(!Constants.EXAM_CENTRE.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.EXAM_CENTRE.getCentralTile());
                    return 1000;
                } else {
                    NPC examiner = getAPIContext().npcs().query().named("Examiner").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        examiner.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption(0)) {
                            return 1000;
                        }
                    }
                }
                break;

            case "Get Teddy Bear":
                if(getAPIContext().inventory().contains("Teddy")) {
                    watDo = "Get panning tray";
                    return 1000;
                }
                if(!Constants.BLUE_URN.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.BLUE_URN.getCentralTile());
                    return 1000;
                } else if(!getAPIContext().inventory().contains("Teddy")){
                    SceneObject bush = getAPIContext().objects().query().named("Bush").results().nearest();
                    bush.interact("Search");
                    Time.sleep(5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                }
                break;

            case "Get panning tray":
                if(getAPIContext().inventory().contains("Panning tray")) {
                    watDo = "Talk to panning guide";
                    return 1000;
                }
                if(!Constants.PANNING_TRAY.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.PANNING_TRAY.getCentralTile());
                    return 1000;
                } else if(!getAPIContext().inventory().contains("Panning tray")){
                    GroundItem tray = getAPIContext().groundItems().query().named("Panning tray").results().nearest();
                    tray.interact("Take");
                    Time.sleep(5_000, () -> getAPIContext().inventory().contains("Panning tray"));
                    return 1000;
                }
                break;

            case "Talk to panning guide":
                if(!getAPIContext().inventory().contains("Cup of tea")) {
                    watDo = "Pan for shiny cup";
                    return 1000;
                }
                NPC panningGuide = getAPIContext().npcs().query().named("Panning guide").results().nearest();
                if(!getAPIContext().dialogues().isDialogueOpen()) {
                    panningGuide.interact("Talk-to");
                    Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                } else {
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    }
                    return 1000;
                }
                break;

            case "Pan for shiny cup":
                if(!getAPIContext().localPlayer().getLocation().contains(Constants.PANNING_SPOT.getCentralPoint())) {
                    getAPIContext().webWalking().walkTo(Constants.PANNING_SPOT);
                    return 1000;
                }
                SceneObject panningSpot = getAPIContext().objects().query().named("Panning point").results().nearest();
                if(!getAPIContext().inventory().contains("Special cup")) {
                    panningSpot.interact("Pan");
                    Time.sleep(10_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    getAPIContext().inventory().interactItem("Search", "Panning tray");
                    Time.sleep(3_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    return 1000;
                } else {
                    watDo = "Steal from workman";
                    return 1000;
                }

            case "Steal from workman":
                if(getAPIContext().inventory().contains("Animal skull") && getAPIContext().inventory().contains("Specimen brush")) {
                    watDo = "Give Teddy Bear to girl";
                    return 1000;
                }
                if(!Constants.ANIMAL_SKULL_WORKMAN.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.ANIMAL_SKULL_WORKMAN.getCentralTile());
                    return 1000;
                }
                NPC workman = getAPIContext().npcs().query().named("Digsite workman").results().nearest();
                if(!getAPIContext().inventory().contains("Animal skull") || !getAPIContext().inventory().contains("Specimen brush")) {
                    workman.interact("Steal-from");
                    return 1000;
                }
                break;

            case "Give Teddy Bear to girl":
                if(!Constants.STUDENT1.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().walkTo(Constants.STUDENT1.getCentralTile());
                    return 1000;
                }
                NPC student1 = getAPIContext().npcs().query().named("Student").results().nearest();
                getAPIContext().camera().turnTo(student1.getLocation());
                if(getAPIContext().inventory().contains("Teddy")) {
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        student1.interact("Talk-to");
                        Time.sleep( 10_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                        }
                        return 1000;
                    }
                } else if(getAPIContext().dialogues().isDialogueOpen()) {
                    if (getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    }
                    return 1000;
                } else { watDo = "Give cup to student"; }
                return 1000;

            case "Give cup to student":
                if(!Constants.STUDENT2.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().walkTo(Constants.STUDENT2.getCentralTile());
                    return 1000;
                }
                NPC student2 = getAPIContext().npcs().query().named("Student").results().nearest();
                getAPIContext().camera().turnTo(student2.getLocation());
                if(getAPIContext().inventory().contains("Special cup")) {
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        student2.interact("Talk-to");
                        Time.sleep( 10_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                        }
                        return 1000;
                    }
                } else if(getAPIContext().dialogues().isDialogueOpen()) {
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    }
                    return 1000;
                } else { watDo = "Give skull to student"; }
                return 1000;

            case "Give skull to student":
                if(!Constants.STUDENT3.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().walkTo(Constants.STUDENT3.getCentralTile());
                    return 1000;
                }
                NPC student3 = getAPIContext().npcs().query().named("Student").results().nearest();
                getAPIContext().camera().turnTo(student3.getLocation());
                if(getAPIContext().inventory().contains("Animal skull")) {
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        student3.interact("Talk-to");
                        Time.sleep( 10_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                        }
                        return 1000;
                    }
                } else if(getAPIContext().dialogues().isDialogueOpen()) {
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    }
                    return 1000;
                } else { watDo = "Take lvl 1 exam"; }
                return 1000;

            case "Take lvl 1 exam":
                if (getAPIContext().inventory().contains("Level 1 certificate")){
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        return 1000;
                    } else {
                        watDo = "Talk to student1 again";
                        return 1000;
                    }
                }
                if(!Constants.EXAM_CENTRE.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.EXAM_CENTRE.getCentralTile());
                    return 1000;
                } else {
                    NPC examiner = getAPIContext().npcs().query().named("Examiner").results().nearest();
                    if (!getAPIContext().dialogues().isDialogueOpen()) {
                        examiner.interact("Talk-to");
                        Time.sleep(5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if (getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        } else if (getAPIContext().dialogues().selectOption("Yes, I certainly am.")) {
                            return 1000;
                        } else if (getAPIContext().dialogues().selectOption("The study of the earth, its contents and history.")) {
                            return 1000;
                        } else if (getAPIContext().dialogues().selectOption("All that have passed the appropriate Earth Sciences exam.")) {
                            return 1000;
                        } else if (getAPIContext().dialogues().selectOption("Gloves and boots to be worn at all times; proper tools must be used.")) {
                            return 1000;
                        }
                    }
                }

            case "Talk to student1 again":
                if(getAPIContext().dialogues().getText().contains("thanks for your advice")) {
                    getAPIContext().dialogues().selectContinue();
                    watDo = "Talk to student2 again";
                    return 1000;
                }
                if(!Constants.STUDENT1.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().walkTo(Constants.STUDENT1.getCentralTile());
                    return 1000;
                }
                student1 = getAPIContext().npcs().query().named("Student").results().nearest();
                getAPIContext().camera().turnTo(student1.getLocation());
                if(!getAPIContext().dialogues().isDialogueOpen()) {
                    student1.interact("Talk-to");
                    Time.sleep( 15_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    return 1000;
                }
                if(getAPIContext().dialogues().canContinue()) {
                    getAPIContext().dialogues().selectContinue();
                    Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                }
                return 1000;

            case "Talk to student2 again":
                if(getAPIContext().dialogues().getText().contains("Thanks for the info")) {
                    getAPIContext().dialogues().selectContinue();
                    watDo = "Talk to student3 again";
                    return 1000;
                }
                if(!Constants.STUDENT2.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().walkTo(Constants.STUDENT2.getCentralTile());
                    return 1000;
                }
                student2 = getAPIContext().npcs().query().named("Student").results().nearest();
                getAPIContext().camera().turnTo(student2.getLocation());
                if(!getAPIContext().dialogues().isDialogueOpen()) {
                    student2.interact("Talk-to");
                    Time.sleep( 15_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    return 1000;
                }
                if(getAPIContext().dialogues().canContinue()) {
                    getAPIContext().dialogues().selectContinue();
                    Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                }
                return 1000;

            case "Talk to student3 again":
                if(getAPIContext().dialogues().getText().contains("remember that")) {
                    getAPIContext().dialogues().selectContinue();
                    watDo = "Take lvl 2 exam";
                    return 1000;
                }
                if(!Constants.STUDENT3.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().walkTo(Constants.STUDENT3.getCentralTile());
                    return 1000;
                }
                student3 = getAPIContext().npcs().query().named("Student").results().nearest();
                getAPIContext().camera().turnTo(student3.getLocation());
                if(!getAPIContext().dialogues().isDialogueOpen()) {
                    student3.interact("Talk-to");
                    Time.sleep( 15_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    return 1000;
                }
                if(getAPIContext().dialogues().canContinue()) {
                    getAPIContext().dialogues().selectContinue();
                    Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                }
                return 1000;

            case "Take lvl 2 exam":
                if (getAPIContext().inventory().contains("Level 2 certificate")){
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        return 1000;
                    } else {
                        watDo = "Talk to student1 one more time";
                        return 1000;
                    }
                }
                if(!Constants.EXAM_CENTRE.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.EXAM_CENTRE.getCentralTile());
                    return 1000;
                } else {
                    NPC examiner = getAPIContext().npcs().query().named("Examiner").results().nearest();
                    if (!getAPIContext().dialogues().isDialogueOpen()) {
                        examiner.interact("Talk-to");
                        Time.sleep(5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if (getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        } else if (getAPIContext().dialogues().selectOption("I am ready for the next exam.")) {
                            return 1000;
                        } else if (getAPIContext().dialogues().selectOption("Samples taken in rough form; kept only in sealed containers.")) {
                            return 1000;
                        } else if (getAPIContext().dialogues().selectOption("Finds must be carefully handled, and gloves worn.")) {
                            return 1000;
                        } else if (getAPIContext().dialogues().selectOption("Always handle with care; strike cleanly on its cleaving point.")) {
                            return 1000;
                        }
                    }
                }
                break;

            case "Talk to student1 one more time":
                if(getAPIContext().dialogues().getText().contains("thanks for your advice")) {
                    getAPIContext().dialogues().selectContinue();
                    watDo = "Talk to student2 one more time";
                    return 1000;
                }
                if(!Constants.STUDENT1.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().walkTo(Constants.STUDENT1.getCentralTile());
                    return 1000;
                }
                student1 = getAPIContext().npcs().query().named("Student").results().nearest();
                getAPIContext().camera().turnTo(student1.getLocation());
                if(!getAPIContext().dialogues().isDialogueOpen()) {
                    student1.interact("Talk-to");
                    Time.sleep( 15_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    return 1000;
                }
                if(getAPIContext().dialogues().canContinue()) {
                    getAPIContext().dialogues().selectContinue();
                    Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                }
                return 1000;

            case "Talk to student2 one more time":
                if(getAPIContext().dialogues().getText().contains("Thanks for the info")) {
                    getAPIContext().dialogues().selectContinue();
                    watDo = "Talk to student3 one more time";
                    return 1000;
                }
                if(!Constants.STUDENT2.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().walkTo(Constants.STUDENT2.getCentralTile());
                    return 1000;
                }
                student2 = getAPIContext().npcs().query().named("Student").results().nearest();
                getAPIContext().camera().turnTo(student2.getLocation());
                if(!getAPIContext().dialogues().isDialogueOpen()) {
                    student2.interact("Talk-to");
                    Time.sleep( 15_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    return 1000;
                }
                if(getAPIContext().dialogues().canContinue()) {
                    getAPIContext().dialogues().selectContinue();
                    Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                }
                return 1000;

            case "Talk to student3 one more time":
                if(getAPIContext().dialogues().getText().contains("remember that")) {
                    getAPIContext().dialogues().selectContinue();
                    watDo = "Take lvl 3 exam";
                    return 1000;
                }
                if(!Constants.STUDENT3.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().walkTo(Constants.STUDENT3.getCentralTile());
                    return 1000;
                }
                student3 = getAPIContext().npcs().query().named("Student").results().nearest();
                getAPIContext().camera().turnTo(student3.getLocation());
                if(!getAPIContext().dialogues().isDialogueOpen()) {
                    student3.interact("Talk-to");
                    Time.sleep( 15_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    return 1000;
                }
                if(getAPIContext().dialogues().canContinue()) {
                    getAPIContext().dialogues().selectContinue();
                    Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                }
                return 1000;

            case "Take lvl 3 exam":
                if (getAPIContext().inventory().contains("Level 3 certificate")){
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        return 1000;
                    } else {
                        watDo = "Get rock pick and specimen jar";
                        return 1000;
                    }
                }
                if(!Constants.EXAM_CENTRE.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.EXAM_CENTRE.getCentralTile());
                    return 1000;
                } else {
                    NPC examiner = getAPIContext().npcs().query().named("Examiner").results().nearest();
                    if (!getAPIContext().dialogues().isDialogueOpen()) {
                        examiner.interact("Talk-to");
                        Time.sleep(5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if (getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        } else if (getAPIContext().dialogues().selectOption("I am ready for the last exam...")) {
                            return 1000;
                        } else if (getAPIContext().dialogues().selectOption("Samples cleaned, and carried only in specimen jars.")) {
                            return 1000;
                        } else if (getAPIContext().dialogues().selectOption("Brush carefully and slowly using short strokes.")) {
                            return 1000;
                        } else if (getAPIContext().dialogues().selectOption("Handle bones very carefully and keep them away from other samples.")) {
                            return 1000;
                        }
                    }
                }
                break;

            case "Get rock pick and specimen jar":
                if(getAPIContext().inventory().contains("Rock pick") && getAPIContext().inventory().contains("Specimen jar")) {
                    watDo = "Dig for ancient talisman";
                    return 1000;
                }
                if(!Constants.WESTERN_EXAM_ROOM.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.WESTERN_EXAM_ROOM.getCentralTile());
                    return 1000;
                }
                List<SceneObject> cupboards = getAPIContext().objects().query().named("Cupboard").results().nearestList();
                if(!getAPIContext().inventory().contains("Rock pick") || !getAPIContext().inventory().contains("Specimen jar")) {
                    for(SceneObject cupboard : cupboards) {
                        cupboard.interact("Open");
                        Time.sleep(5_000, () -> getAPIContext().objects().query().named("Cupboard").results().nearest().hasAction("Search"));
                        cupboard.interact("Search");
                        Time.sleep(3_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    }
                    return 1000;
                }
                break;

            case "Dig for ancient talisman":
                if(getAPIContext().inventory().contains("Ancient talisman")) {
                    watDo = "Talk to archaeologist";
                    return 1000;
                }
                if(!Constants.NORTHEAST_DIG.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.NORTHEAST_DIG.getCentralTile());
                    return 1000;
                }
                else {
                    getAPIContext().inventory().dropAllExcept("Ancient talisman", "Pestle and mortar", "Vial", "Tinderbox", "Cup of tea", "Rope", "Uncut opal", "Charcoal", "Varrock teleport", "Trowel", "Level 1 certificate", "Level 2 certificate", "Level 3 certificate", "Specimen brush", "Bucket", "Specimen jar", "Rock pick", "Panning tray");
                }
                SceneObject soil = getAPIContext().objects().query().named("Soil").results().nearest();
                if(!getAPIContext().inventory().contains("Ancient talisman")) {
                    getAPIContext().inventory().selectItem("Trowel");
                    soil.interact("Use");
                    Time.sleep(5_000, () -> getAPIContext().localPlayer().isAnimating());
                    Time.sleep(5_000, () -> !getAPIContext().localPlayer().isAnimating());
                    return 1000;
                }
                break;

            case "Talk to archaeologist":
                if(getAPIContext().inventory().contains("Invitation letter")) {
                    watDo = "Use invitation on workman";
                    return 1000;
                }
                if(!Constants.EXAM_CENTRE.contains(getAPIContext().localPlayer().getLocation()) && !Constants.WESTERN_EXAM_ROOM.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.EXAM_CENTRE.getCentralTile());
                    return 1000;
                } else {
                    NPC archaeologist = getAPIContext().npcs().query().named("Archaeological expert").results().nearest();
                    getAPIContext().camera().turnTo(archaeologist.getLocation());
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        archaeologist.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                        }
                        return 1000;
                    }
                }
                break;

            case "Use invitation on workman":
                if(!getAPIContext().inventory().contains("Invitation letter")) {
                    watDo = "Use rope on western winch";
                    return 1000;
                }
                if(!Constants.STUDENT3.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().walkTo(Constants.STUDENT3.getCentralTile());
                    NPC workman2 = getAPIContext().npcs().query().named("Digsite workman").results().nearest();
                    getAPIContext().inventory().selectItem("Invitation letter");
                    getAPIContext().camera().turnTo(workman2.getLocation());
                    workman2.interact();
                    Time.sleep(10_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    return 1000;
                }
                if(getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().dialogues().selectContinue();
                    return 1000;
                }
                break;

            case "Use rope on western winch":
                if(getAPIContext().inventory().getCount("Rope") == 1) {
                    watDo = "Get arcenia root";
                    return 1000;
                }
                if(!Constants.WESTERN_WINCH.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.WESTERN_WINCH.getCentralTile());
                    return 1000;
                }
                SceneObject winch = getAPIContext().objects().query().named("Winch").results().nearest();
                if(getAPIContext().inventory().getCount("Rope") == 2) {
                    getAPIContext().inventory().selectItem("Rope");
                    winch.interact();
                    return 1000;
                }
                break;

            case "Get arcenia root":
                if(Constants.WESTERN_WINCH.contains(getAPIContext().localPlayer().getLocation())) {
                    winch = getAPIContext().objects().query().named("Winch").results().nearest();
                    winch.interact("Operate");
                    Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() > 9000);
                    return 1000;
                }
                if(!getAPIContext().inventory().contains("Arcenia root")) {
                    getAPIContext().groundItems().query().named("Arcenia root").results().nearest().interact("Take");
                    Time.sleep(5_000, () -> getAPIContext().inventory().contains("Arcenia root"));
                } else {
                    SceneObject brick = getAPIContext().objects().query().named("Brick").results().nearest();
                    getAPIContext().camera().turnTo(brick.getLocation());
                    brick.interact("Search");
                    Time.sleep(5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                }
                if(getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().dialogues().selectContinue();
                    SceneObject rope = getAPIContext().objects().query().named("Rope").results().nearest();
                    getAPIContext().camera().turnTo(rope);
                    rope.interact("Climb-up");
                    Time.sleep(7_000, () -> getAPIContext().localPlayer().getY() < 9000);
                    watDo = "Use rope on Northeast winch";
                    return 1000;
                }
                break;

            case "Use rope on Northeast winch":
                if(!Constants.NORTHEAST_WINCH.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.NORTHEAST_WINCH.getCentralTile());
                    return 1000;
                }
                SceneObject winch2 = getAPIContext().objects().query().named("Winch").results().nearest();
                if(getAPIContext().inventory().getCount("Rope") == 1) {
                    getAPIContext().inventory().selectItem("Rope");
                    winch2.interact();
                    return 1000;
                } else {
                    watDo = "Talk to Doug";
                    return 1000;
                }

            case "Talk to Doug":
                if(getAPIContext().inventory().contains("Chest key")) {
                    SceneObject rope = getAPIContext().objects().query().named("Rope").results().nearest();
                    getAPIContext().camera().turnTo(rope);
                    rope.interact("Climb-up");
                    Time.sleep(7_000, () -> getAPIContext().localPlayer().getY() < 9000);
                    watDo = "Get Chemical powder";
                    return 1000;
                }
                if(Constants.NORTHEAST_WINCH.contains(getAPIContext().localPlayer().getLocation())) {
                    winch2 = getAPIContext().objects().query().named("Winch").results().nearest();
                    winch2.interact("Operate");
                    Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() > 9000);
                    return 1000;
                }
                NPC doug = getAPIContext().npcs().query().named("Doug Deeping").results().nearest();
                if(!getAPIContext().dialogues().isDialogueOpen()) {
                    doug.interact("Talk-to");
                    Time.sleep(5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                }
                if(getAPIContext().dialogues().isDialogueOpen()) {
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        return 1000;
                    } else {
                        getAPIContext().dialogues().selectOption(3);
                        return 1000;
                    }
                }
                break;

            case "Get Chemical powder":
                if(!Constants.PANNING_TRAY.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.PANNING_TRAY.getCentralTile());
                    return 1000;
                } else if(!getAPIContext().inventory().contains("Chemical powder")){
                    getAPIContext().inventory().selectItem("Chest key");
                    SceneObject chest = getAPIContext().objects().query().named("Chest").results().nearest();
                    chest.interact("Use");
                    Time.sleep(5_000, () -> getAPIContext().objects().query().named("Chest").results().nearest().hasAction("Search"));
                    chest.interact("Search");
                    return 1000;
                } else {
                        getAPIContext().dialogues().selectContinue();
                        watDo = "Trowel the barrel";
                        return 1000;
                }

            case "Trowel the barrel":
                if(!getAPIContext().dialogues().isDialogueOpen()) {
                    SceneObject barrel = getAPIContext().objects().query().named("Barrel").actions("Search").results().nearest();
                    getAPIContext().inventory().selectItem("Trowel");
                    getAPIContext().camera().turnTo(barrel.getLocation());
                    barrel.interact();
                    return 3000;
                } else {
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        watDo = "Collect liquid and grind charcoal";
                        return 1000;
                    }
                }
                break;

            case "Collect liquid and grind charcoal":
                SceneObject barrel = getAPIContext().objects().query().named("Barrel").actions("Search").results().nearest();
                if(!getAPIContext().inventory().contains("Unidentified liquid")) {
                    getAPIContext().inventory().selectItem("Vial");
                    barrel.interact();
                    Time.sleep(5_000, () -> getAPIContext().inventory().contains("Unidentified liquid"));
                    return 1000;
                } else if(!getAPIContext().inventory().contains("Ground charcoal")) {
                    getAPIContext().inventory().selectItem("Pestle and mortar");
                    getAPIContext().inventory().getItem("Charcoal").interact();
                    Time.sleep(3_000, () -> getAPIContext().inventory().contains("Ground charcoal"));
                    watDo = "Identify items";
                    return 1000;
                }
                break;

            case "Identify items":
                if(getAPIContext().inventory().contains("Nitroglycerin") && getAPIContext().inventory().contains("Ammonium nitrate")) {
                    watDo = "Mix chemicals";
                    return 1000;
                }
                if(!Constants.EXAM_CENTRE.contains(getAPIContext().localPlayer().getLocation()) && !Constants.WESTERN_EXAM_ROOM.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.EXAM_CENTRE.getCentralTile());
                    return 1000;
                } else {
                    NPC archaeologist = getAPIContext().npcs().query().named("Archaeological expert").results().nearest();
                    if(getAPIContext().inventory().contains("Unidentified liquid") && !getAPIContext().dialogues().isDialogueOpen()) {
                        getAPIContext().camera().turnTo(archaeologist.getLocation());
                        getAPIContext().inventory().selectItem("Unidentified liquid");
                        archaeologist.interact();
                        Time.sleep(10_000, () -> getAPIContext().dialogues().isDialogueOpen());
                        return 1000;
                    }
                    if(getAPIContext().inventory().contains("Chemical powder") && !getAPIContext().dialogues().isDialogueOpen()) {
                        getAPIContext().camera().turnTo(archaeologist.getLocation());
                        getAPIContext().inventory().selectItem("Chemical powder");
                        archaeologist.interact();
                        Time.sleep(10_000, () -> getAPIContext().dialogues().isDialogueOpen());
                        return 1000;
                    }
                    if(getAPIContext().dialogues().isDialogueOpen()) {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                        }
                    }
                }
                break;

            case "Mix chemicals":
                if(getAPIContext().inventory().contains("Chemical compound")) {
                    watDo = "Climb down western winch";
                    return 1000;
                }
                if(!getAPIContext().inventory().contains("Mixed chemicals")) {
                    getAPIContext().inventory().selectItem("Nitroglycerin");
                    getAPIContext().inventory().getItem("Ammonium nitrate").interact();
                    Time.sleep(3_000, () -> getAPIContext().inventory().contains("Mixed chemicals"));
                    return 1000;
                } else if(getAPIContext().inventory().contains("Ground charcoal")){
                    getAPIContext().inventory().selectItem("Ground charcoal");
                    getAPIContext().inventory().getItem("Mixed chemicals").interact();
                    Time.sleep(3_000, () -> !getAPIContext().inventory().contains("Ground charcoal"));
                    return 1000;
                } else if(getAPIContext().inventory().contains("Arcenia root")) {
                    getAPIContext().inventory().selectItem("Arcenia root");
                    getAPIContext().inventory().getItem("Mixed chemicals").interact();
                    Time.sleep(3_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    getAPIContext().dialogues().selectContinue();
                    Time.sleep(3_000, () -> !getAPIContext().inventory().contains("Arcenia root"));
                    return 1000;
                }
                break;

            case "Climb down western winch":
                if(getAPIContext().localPlayer().getY() > 9000) {
                    watDo = "Blow up bricks and take stone tablet";
                    return 1000;
                }
                if(!Constants.WESTERN_WINCH.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.WESTERN_WINCH.getCentralTile());
                    return 1000;
                }
                if(Constants.WESTERN_WINCH.contains(getAPIContext().localPlayer().getLocation())) {
                    winch = getAPIContext().objects().query().named("Winch").results().nearest();
                    winch.interact("Operate");
                    Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() > 9000);
                    return 1000;
                }
                break;

            case "Blow up bricks and take stone tablet":
                if(getAPIContext().inventory().contains("Stone tablet")) {
                    SceneObject rope = getAPIContext().objects().query().named("Rope").results().nearest();
                    getAPIContext().camera().turnTo(rope);
                    rope.interact("Climb-up");
                    Time.sleep(7_000, () -> getAPIContext().localPlayer().getY() < 9000);
                    watDo = "Show tablet to archaeological expert";
                    return 1000;
                }
                SceneObject brick = getAPIContext().objects().query().named("Brick").results().nearest();
                if(getAPIContext().inventory().contains("Chemical compound")) {
                    getAPIContext().camera().turnTo(brick.getLocation());
                    getAPIContext().inventory().selectItem("Chemical compound");
                    brick.interact();
                    Time.sleep(5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    getAPIContext().dialogues().selectContinue();
                    return 1000;
                } else if(getAPIContext().objects().query().named("Brick").results().nearest() != null){
                    getAPIContext().camera().turnTo(brick.getLocation());
                    getAPIContext().inventory().selectItem("Tinderbox");
                    brick.interact();
                    Time.sleep(5_000, () -> getAPIContext().objects().query().named("Brick").results().nearest() == null);
                    return 1000;
                }
                if(getAPIContext().objects().query().named("Brick").results().nearest() == null && !getAPIContext().inventory().contains("Stone tablet")) {
                    Point key = new Tile(3378, 9761, 0).getCentralPoint();
                    getAPIContext().mouse().click(key);
                    SceneObject tablet = getAPIContext().objects().query().named("Stone Tablet").results().nearest();
                    getAPIContext().camera().turnTo(tablet.getLocation());
                    tablet.interact("Take");
                    Time.sleep(5_000, () -> getAPIContext().inventory().contains("Stone tablet"));
                    return 1000;
                }
                break;

            case "Show tablet to archaeological expert":
                if(getAPIContext().widgets().get(153).isVisible()) {
                    watDo = "Done";
                    return 1000;
                }
                if(!Constants.EXAM_CENTRE.contains(getAPIContext().localPlayer().getLocation()) && !Constants.WESTERN_EXAM_ROOM.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.EXAM_CENTRE.getCentralTile());
                    return 1000;
                } else {
                    NPC archaeologist = getAPIContext().npcs().query().named("Archaeological expert").results().nearest();
                    if (getAPIContext().inventory().contains("Stone tablet") && !getAPIContext().dialogues().isDialogueOpen()) {
                        getAPIContext().camera().turnTo(archaeologist.getLocation());
                        getAPIContext().inventory().selectItem("Stone tablet");
                        archaeologist.interact();
                        Time.sleep(10_000, () -> getAPIContext().dialogues().isDialogueOpen());
                        return 1000;
                    }
                    if (getAPIContext().dialogues().isDialogueOpen()) {
                        if (getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                        }
                    }
                }
                break;

            case "Done":
                if(getAPIContext().widgets().get(153).isVisible()) {
                    getAPIContext().widgets().get(153).getChild(16).interact("Close");
                    return 1000;
                }
                BankUtils.goToClosestBank();
                BankUtils.openBank();
                BankUtils.depositInventory();
                BankUtils.closeBank();
                GenUtils.logOut();
                break;
        }
        return 1000;
    }

    @Override
    public boolean onStart(String... strings) {
        return true;
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
}
