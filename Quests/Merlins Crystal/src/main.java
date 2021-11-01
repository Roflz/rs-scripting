import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.methods.IQuestAPI;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.time.Time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static utils.BankUtils.*;
import static utils.GenUtils.*;
import static utils.Constants.*;
import static utils.GEUtils.*;
import static utils.InventoryUtils.*;

@ScriptManifest(name = "Merlins Crystal", gameType = GameType.OS)
public class main extends LoopScript {

    private List<String> startingItemsList = Arrays.asList("Bread", "Tinderbox", "Bucket of wax", "Camelot teleport", "Falador teleport", "Varrock teleport", "Games necklace(8)", "Amulet of glory(6)");
    private List<Integer> startingQuantitiesList = Arrays.asList(1, 1, 1, 4, 1, 1, 1, 1);
    public static List<String> shoppingListItems = new ArrayList();
    public static List<Integer> shoppingListQuantities = new ArrayList();
    private int geBuyCounter = 0;
    private int gePriceIncreaseCounter = 1;

    @Override
    protected int loop() {

        System.out.println(getBottomChatMessage());

        System.out.println(watDo);
        switch (watDo) {
            case "":
                getAPIContext().camera().setPitch(98);
                getAPIContext().mouse().move(getAPIContext().game().getCenterSceneTile().getCentralPoint());
                getAPIContext().mouse().scroll(false, 20);
                watDo = "Bank";
                return 500;
            case "Bank":
                if (!getAPIContext().bank().isOpen() && !InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                    System.out.println("Going to pick up starting items");
                    BankUtils.goToClosestBank();
                    BankUtils.openBank();
                    BankUtils.depositInventoryIfNotEmpty();
                    return 500;
                } else if (InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                    watDo = "Start Quest";
                    return 1000;
                }
                if (getAPIContext().bank().isOpen()) {
                    if (BankUtils.doesBankHaveItemsWithQuantity(startingItemsList, startingQuantitiesList) || InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                        System.out.println("Withdrawing starting items from Bank");
                        BankUtils.depositInventoryIfNotEmpty();
                        getAPIContext().bank().depositEquipment();
                        BankUtils.withdrawItemsFromListWithQuantity(startingItemsList, startingQuantitiesList);
                        getAPIContext().inventory().interactItem("Wear", "Amulet of glory(6)");
                        if(getAPIContext().skills().attack().getCurrentLevel() >= 20) {
                            getAPIContext().bank().withdraw(1, "Mithril scimitar");
                            getAPIContext().inventory().interactItem("Wield", "Mithril scimitar");
                        } else {
                            getAPIContext().bank().withdraw(1, "Steel scimitar");
                            getAPIContext().inventory().interactItem("Wield", "Steel scimitar");
                        }
                        getAPIContext().bank().withdraw(20, "Trout");
                        BankUtils.closeBank();
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
                        GEUtils.buyItemsFromShoppingList(shoppingListItems, shoppingListQuantities);
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
                if(getAPIContext().quests().isStarted(IQuestAPI.Quest.MERLINS_CRYSTAL)) {
                    watDo = "Talk to Gawain";
                    return 1000;
                }
                if(!Constants.ROUND_TABLE.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.ROUND_TABLE.getRandomTile());
                    return 1000;
                } else {
                    NPC kingArthur = getAPIContext().npcs().query().named("King Arthur").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        kingArthur.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 500;
                        } else if(getAPIContext().dialogues().selectOption("I want to become a knight of the round table!")) {
                            return 1000;
                        }
                    }
                }
                break;

            case "Talk to Gawain":
                if(!Constants.ROUND_TABLE.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.ROUND_TABLE.getRandomTile());
                    return 1000;
                } else {
                    NPC gawain = getAPIContext().npcs().query().named("Sir Gawain").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        gawain.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 500;
                        } else if(getAPIContext().dialogues().selectOption(2)) {
                            return 1000;
                        } else {
                            watDo = "Talk to Lancelot";
                            return 1000;
                        }
                    }
                }
                break;

            case "Talk to Lancelot":
                if(!Constants.LANCELOT.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.LANCELOT.getRandomTile());
                    return 1000;
                } else {
                    NPC lancelot = getAPIContext().npcs().query().named("Sir Lancelot").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        lancelot.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 500;
                        } else if(getAPIContext().dialogues().selectOption(2)) {
                            return 1000;
                        } else {
                            watDo = "Hide in crate";
                            return 1000;
                        }
                    }
                }
                break;

            case "Hide in crate":
                if(Constants.KEEP_DOCK.contains(getAPIContext().localPlayer().getLocation())) {
                    watDo = "Kill Sir Mordred";
                    return 1000;
                }
                if(!Constants.CANDLE_SHOP.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().setUseTeleports(false);
                    getAPIContext().webWalking().walkTo(Constants.CANDLE_SHOP.getRandomTile());
                    getAPIContext().webWalking().setUseTeleports(true);
                    return 1000;
                } else {
                    SceneObject crate = getAPIContext().objects().query().named("Crate").actions("Hide-in").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        crate.interact("Hide-in");
                        Time.sleep( 10_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 500;
                        } else if(getAPIContext().dialogues().selectOption("Yes.")) {
                            Time.sleep(5_000, () -> getAPIContext().dialogues().canContinue());
                            return 1000;
                        }
                    }
                }
                break;

            case "Kill Sir Mordred":
                if(getAPIContext().npcs().query().named("Sir Mordred").results().nearest() != null) {
                    if(getAPIContext().npcs().query().named("Sir Mordred").results().nearest().getHealthPercent() == 0) {
                        watDo = "Next";
                        return 1000;
                    }
                }
                if(!Constants.SIR_MORDRED.contains(getAPIContext().localPlayer().getLocation())) {
                    if(getAPIContext().objects().query().named("Large door").results().nearest() != null) {
                        if (getAPIContext().objects().query().named("Large door").results().nearest().hasAction("Open")) {
                            getAPIContext().objects().query().named("Large door").results().nearest().interact("Open");
                            return 1000;
                        }
                    }
                    if(getAPIContext().objects().query().named("Staircase").actions("Climb-up").results().nearest().interact("Climb-up")) {
                        return 1000;
                    }
                    return 1000;
                } else {
                    NPC mordred = getAPIContext().npcs().query().named("Sir Mordred").results().nearest();
                    if(getAPIContext().localPlayer().getHealthPercent() < 50) {
                        getAPIContext().inventory().interactItem("Eat", "Trout");
                        return 1000;
                    }
                    if(!getAPIContext().localPlayer().isAttacking()) {
                        mordred.interact("Attack");
                        return 1000;
                    }
                }
                break;

            case "Sir Mordred dead":
                if(getAPIContext().dialogues().isDialogueOpen()) {
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        return 500;
                    } else if(getAPIContext().dialogues().selectOption("Tell me how to untrap Merlin and I might."));
                    else if (getAPIContext().dialogues().selectOption(1));
                    else {
                        watDo = "Leave Castle";
                        return 1000;
                    }
                }

            case "Leave Castle":
                if(Constants.OUTSIDE_CASTLE.contains(getAPIContext().localPlayer().getLocation())) {
                    watDo = "Get bat bones";
                    return 1000;
                }
                if(getAPIContext().objects().query().named("Staircase").actions("Climb-down").results().nearest() != null) {
                    if(getAPIContext().objects().query().named("Staircase").actions("Climb-down").results().nearest().interact("Climb-down")) {
                        return 1000;
                    }
                }
                if(getAPIContext().objects().query().named("Large door").results().nearest() != null) {
                    if (getAPIContext().objects().query().named("Large door").actions("Knock-at").results().nearest().hasAction("Open")) {
                        getAPIContext().objects().query().named("Large door").results().nearest().interact("Open");
                        return 1000;
                    }
                }

            case "Get bat bones":
                if(getAPIContext().inventory().contains("Bat bones")) {
                    watDo = "Get black candle";
                    return 1000;
                }
                if(getAPIContext().groundItems().query().named("Bat bones").results().nearest() != null) {
                    getAPIContext().groundItems().query().named("Bat bones").results().nearest().interact("Take");
                    return 1000;
                }
                if(getAPIContext().npcs().query().named("Giant bat").results().nearest() != null && !getAPIContext().localPlayer().isAttacking()) {
                    getAPIContext().npcs().query().named("Giant bat").results().nearest().interact("Attack");
                    return 1000;
                }

            case "Get black candle":
                if(getAPIContext().inventory().contains("Black candle")) {
                    watDo = "Talk to lady of the lake";
                    return 1000;
                }
                if(!Constants.CANDLE_SHOP.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().setUseTeleports(false);
                    getAPIContext().webWalking().walkTo(Constants.CANDLE_SHOP.getRandomTile());
                    getAPIContext().webWalking().setUseTeleports(true);
                    return 1000;
                } else {
                    NPC candleMaker = getAPIContext().npcs().query().named("Candle maker").results().nearest();
                    if (!getAPIContext().dialogues().isDialogueOpen()) {
                        getAPIContext().camera().turnTo(candleMaker.getLocation());
                        candleMaker.interact("Talk-to");
                        Time.sleep(10_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if (getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 500;
                        } else if (getAPIContext().dialogues().selectOption(0)) {
                            return 1000;
                        }
                    }
                }

            case "Talk to lady of the lake":
                if(getAPIContext().dialogues().getText().contains("Ok. That seems easy")) {
                    getAPIContext().dialogues().selectContinue();
                    watDo = "Give bread to beggar";
                    return 1000;
                }
                walkToAreaCentralTile(LADY_OF_THE_LAKE);
                talkToNPCWithOptions("The Lady of the Lake", 2);

            case "Give bread to beggar":
                if(getAPIContext().inventory().contains("Excalibur")) {
                    getAPIContext().dialogues().selectContinue();
                    watDo = "Check inscription";
                    return 1000;
                }
                walkToAreaCentralTile(PORT_SARIM_RING_SHOP);
                talkToNPCWithOptions("Beggar", 0);

            case "Check inscription":
                walkToAreaCentralTile(ZAMORAKIAN_TEMPLE_VARROCK);
                if(!dialogueIsOpen()) { return interactWithObjectUntilDialogue("Chaos altar", "Check"); }
                else {
                    watDo = "Summon Thrantax";
                    return selectContinue();
                }

            case "Summon Thrantax":
                if(playerLocationIs(RITUAL_SITE_CAMELOT)) {
                    useItemOnInventoryItem("Tinderbox", "Black candle");
                    if(inventoryContains("Lit black candle")) {
                        return drop("Bat bones");
                    }
                } else {
                    return walkToTile(RITUAL_SITE_CAMELOT);
                }
                if(dialogueIsOpen()) {
                    proceedThroughDialogue("Snarthon Candtrick Termanto");
                } else {
                    watDo = "Free Merlin";
                    return 1000;
                }

            case "Free Merlin":
                if(!playerisInArea(MERLINS_CRYSTAL)) {
                    walkToAreaCentralTile(MERLINS_CRYSTAL);
                    if (!(getAPIContext().localPlayer().getLocation() == new Tile(2769, 3494, 1))) {
                        interactWithObject("Ladder", "Climb-up");
                        return 1000;
                    }
                } else {
                    useItemOnObjectAndWaitForDialogue("Excalibur", "Giant Crystal");
                    proceedThroughDialogue();
                    watDo = "Finish Quest";
                }

            case "Finish Quest":
                if(!playerisInArea(ROUND_TABLE)) {
                    walkToAreaRandomTile(ROUND_TABLE);
                } else if(!getAPIContext().widgets().get(153).isVisible()) {
                    talkToNPC("King Arthur");
                } else {
                    getAPIContext().widgets().get(153).getChild(16).interact("Close");
                    watDo = "Done";
                    return 1000;
                }

            case "Done":
                goToClosestBank();
                openBank();
                depositInventory();
                closeBank();
                GenUtils.logOut();

        }
        return 1000;
    }

    public static String watDo = "";

    @Override
    public boolean onStart(String... strings) {
        return true;
    }

    public int getItemQuantity(String item) {
        int i = 0;
        for (String startingItem : startingItemsList) {
            if (startingItem.contains(item)) {
                return startingQuantitiesList.get(i);
            }
            i += 1;
        }
        return 0;
    }
}
