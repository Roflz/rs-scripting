import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.methods.IQuestAPI;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.time.Time;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static utils.BankUtils.*;
import static utils.Constants.*;
import static utils.GenUtils.*;
import static utils.InventoryUtils.*;
import static utils.GEUtils.*;

@ScriptManifest(name = "Bone Voyage", gameType = GameType.OS)
public class main extends LoopScript {

    private List<String> startingItemsList = Arrays.asList("Vodka", "Marrentill potion (unf)", "Skills necklace(6)", "Varrock teleport", "Amulet of glory(6)");
    private List<Integer> startingQuantitiesList = Arrays.asList(2, 1, 1, 2, 1);
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
                if (!getAPIContext().bank().isOpen() && !doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                    System.out.println("Going to pick up starting items");
                    goToClosestBank();
                    openBank();
                    depositInventoryIfNotEmpty();
                    depositEquipment();
                    return 500;
                } else if (doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                    watDo = "Start Quest";
                    return 1000;
                }
                if (getAPIContext().bank().isOpen()) {
                    if (doesBankHaveItemsWithQuantity(startingItemsList, startingQuantitiesList) || doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                        System.out.println("Withdrawing starting items from Bank");
                        depositInventoryIfNotEmpty();
                        getAPIContext().bank().depositEquipment();
                        withdrawItemsFromListWithQuantity(startingItemsList, startingQuantitiesList);
                        watDo = "Start Quest";
                        return 500;
                    } else {
                        depositInventoryIfNotEmpty();
                        getAPIContext().bank().withdrawAll("Coins");
                        shoppingListItems = createShoppingListItems(startingItemsList, startingQuantitiesList);
                        shoppingListQuantities = createShoppingListQuantities(startingItemsList, startingQuantitiesList);
                        getAPIContext().bank().close();
                        System.out.println("Bank does not have items, so going to buy them from the GE");
                        watDo = "Buy items from GE";
                        return 1000;
                    }
                }
                break;
            case "Buy items from GE":
                if (!getAPIContext().grandExchange().isOpen()) {
                    if (!GRAND_EXCHANGE_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                        teleportWithChronicle();
                        goToGE();
                        return 500;
                    } else if (!getAPIContext().inventory().contains("Coins")) {
                        getAPIContext().bank().open();
                        withdrawAllCoins();
                        getAPIContext().bank().close();
                        return 500;
                    } else {
                        getAPIContext().grandExchange().open();
                        return 500;
                    }
                }
                if (getAPIContext().grandExchange().isOpen()) {
                    if (!allSalesComplete(shoppingListItems)) {
                        buyItemsFromShoppingList(shoppingListItems, shoppingListQuantities);
                        watDo = "Wait for sales from GE";
                        return 500;
                    }
                }
                break;
            case "Wait for sales from GE":
                if (anySaleNotYetComplete(shoppingListItems) && shoppingListItems.size() > 0 && gePriceIncreaseCounter <= 10) {
                    if (geBuyCounter >= 10) {
                        int i = 0;
                        for (String item : getSalesNotYetCompleteWithNames(shoppingListItems)) {
                            abortOffer(getGESlotWithItem(item));
                            makeBuyOffer(item, getItemQuantity(item), gePriceIncreaseCounter);
                            i += 1;
                        }
                        geBuyCounter = 0;
                        gePriceIncreaseCounter += 1;
                        return 1000;
                    }
                    doesInventoryHaveItemsWithQuantity(shoppingListItems, shoppingListQuantities);
                    geBuyCounter += 1;
                    return 1000;
                }
                if (allSalesComplete(shoppingListItems) || shoppingListItems.size() == 0) {
                    getAPIContext().grandExchange().collectToBank();
                    getAPIContext().grandExchange().close();
                    watDo = "Bank";
                    return 500;
                }
                break;

            case "Start Quest":
                if(getAPIContext().quests().isStarted(IQuestAPI.Quest.BONE_VOYAGE)) {
                    watDo = "Talk to barge foreman";
                    return 1000;
                }
                walkToAreaCentralTile(VARROCK_MUSEUM);
                talkToNPC("Curator Haig Halen", 0);
                break;

            case "Talk to barge foreman":
                walkToAreaCentralTile(CANAL_BARGE);
                talkToNPC("Barge foreman");
                watDo = "Talk to sawmill operator";
                return 1000;

            case "Talk to sawmill operator":
                if(inventoryContains("Sawmill proposal")) {
                    watDo = "Bring proposal to woodcutting guild";
                    return 1000;
                }
                walkToAreaCentralTile(SAWMILL_OPERATOR_VARROCK);
                talkToNPC("Sawmill operator", 3);
                return 1000;

            case "Bring proposal to woodcutting guild":
                if(inventoryContains("Sawmill agreement")) {
                    watDo = "Bring agreement back to sawmill operator";
                    return 1000;
                }
                walkToAreaCentralTile(WOODCUTTING_GUILD_ENTRANCE);
                interactWithObjectUntilDialogue("Gate", "Open");
                proceedThroughDialogue(3);
                return 1000;

            case "Bring agreement back to sawmill operator":
                if(!inventoryContains("Sawmill agreement")) {
                    watDo = "Talk to barge foreman again";
                    return 1000;
                }
                walkToAreaCentralTile(SAWMILL_OPERATOR_VARROCK);
                talkToNPC("Sawmill operator", 3);
                return 1000;

            case "Talk to barge foreman again":
                walkToAreaCentralTile(CANAL_BARGE);
                talkToNPC("Barge foreman");
                watDo = "Board barge";
                return 1000;


            case "Board barge":
                if(getAPIContext().localPlayer().getY() > 3450) {
                    watDo = "Talk to Lead Navigator";
                    return 1000;
                }
                interactWithNPC("Barge guard", "Board");
                Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() > 3450);
                return 1000;

            case "Talk to Lead Navigator":
                talkToNPC("Lead Navigator", "Yep, that would be me.", "I'm aware, no need to tell me about them.");
                if(!dialogueIsOpen()) {
                    watDo = "Talk to Jack Seagull";
                    return 1000;
                }
                return 1000;

            case "Talk to Jack Seagull":
                if(!playerisInArea(RUSTY_ANCHOR_INN)) {
                    interactWithNPC("Barge guard", "Deboard");
                    Time.sleep(4_000, 5_000, () -> getAPIContext().localPlayer().getY() < 3450);
                    walkToAreaRandomTile(RUSTY_ANCHOR_INN);
                    return 1000;
                }
                if(dialogueIsOpen()) {
                    proceedThroughDialogue();
                    watDo = "Return to barge and talk to Lead Navigator";
                    return 1000;
                }
                talkToNPC("Jack Seagull", "Ever made any cursed voyages?");
                return 1000;

            case "Return to barge and talk to Lead Navigator":
                if(getAPIContext().localPlayer().getY() > 3450) {
                    talkToNPC("Lead Navigator");
                    watDo = "Talk to odd old man";
                    return 1000;
                } else {
                    walkToAreaCentralTile(CANAL_BARGE);
                    interactWithNPC("Barge guard", "Board");
                    Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() > 3450);
                }
                return 1000;

            case "Talk to odd old man":
                if(!playerisInArea(ODD_OLD_MAN)) {
                    interactWithNPC("Barge guard", "Deboard");
                    Time.sleep(4_000, 5_000, () -> getAPIContext().localPlayer().getY() < 3450);
                    walkToAreaCentralTileNoTeleport(ODD_OLD_MAN);
                    return 1000;
                } else {
                    talkToNPC("Odd Old Man", "Talk about lucky charms.", "I'm making a cursed voyage.");
                    if(!dialogueIsOpen()) {
                        watDo = "Speak to Apothecary";
                        return 1000;
                    }
                }

            case "Speak to Apothecary":
                if(!inventoryContains("Vodka")) {
                    watDo = "Return to barge and talk to Lead Navigator again";
                    return 1000;
                }
                if(!playerisInArea(APOTHECARY)) {
                    walkToAreaCentralTile(APOTHECARY);
                    return 1000;
                } else {
                    talkToNPC("Apothecary", "Talk about something else.", "Talk about Bone Voyage.");
                    return 1000;
                }

            case "Return to barge and talk to Lead Navigator again":
                if(!inventoryContains("Potion of sealegs")) {
                    watDo = "Speak to Junior Navigator";
                    return 1000;
                }
                if(getAPIContext().localPlayer().getY() > 3450) {
                    talkToNPC("Lead Navigator");
                } else {
                    walkToAreaCentralTile(CANAL_BARGE);
                    interactWithNPC("Barge guard", "Board");
                    Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() > 3450);
                }
                return 1000;

            case "Talk to Junior Navigator":
                if(!inventoryContains("Bone charm")) {
                    watDo = "Take Cursed Voyage";
                } else {
                    if(playerLocationIs(new Tile(3363, 3453, 1))) {
                        new Tile(3364, 3453, 1).interact("Walk here");
                    } else {
                        talkToNPC("Junior Navigator", 0);
                    }
                }
                return 1000;

            case "Take Cursed Voyage":
                if(dialogueIsOpen()) {
                    selectContinueUntilDialogueIsGone();
                }
                if(getAPIContext().widgets().get(604).isVisible()) {
                    watDo = "Steer ship";
                    return 1000;
                }

            case "Steer ship":
                if(getAPIContext().widgets().get(153).isVisible()) {
                    getAPIContext().widgets().get(153).getChild(16).interact("Close");
                    watDo = "Done";
                    return 1000;
                }
                if(!getAPIContext().widgets().get(604).isVisible()) {
                    watDo = "Try again";
                    return 1000;
                }

                if(getAPIContext().widgets().get(604).getChild(8).getSpriteId() > 35000 && getAPIContext().widgets().get(604).getChild(37).getSpriteId() != 53722) {
                    getAPIContext().widgets().get(604).getChild(38).interact();
                    Time.sleep(1_000);
                    return 1000;
                }
                if(getAPIContext().widgets().get(604).getChild(8).getSpriteId() < 33000 && getAPIContext().widgets().get(604).getChild(37).getSpriteId() != 44721) {
                    getAPIContext().widgets().get(604).getChild(39).interact();
                    Time.sleep(1_000);
                    return 1000;
                }

                if(getAPIContext().widgets().get(604).getChild(8).getSpriteId() > 33000 && getAPIContext().widgets().get(604).getChild(8).getSpriteId() < 35000 && getAPIContext().widgets().get(604).getChild(37).getSpriteId() == 53722) {
                    getAPIContext().widgets().get(604).getChild(39).interact();
                }
                if(getAPIContext().widgets().get(604).getChild(8).getSpriteId() > 33000 && getAPIContext().widgets().get(604).getChild(8).getSpriteId() < 35000 && getAPIContext().widgets().get(604).getChild(37).getSpriteId() == 44721) {
                    getAPIContext().widgets().get(604).getChild(38).interact();
                }

                return 1000;

            case "Try again":
                talkToNPC("Lead Navigator", 0);
                if(getAPIContext().dialogues().canContinue()) {
                    watDo = "Take Cursed Voyage";
                    return 1000;
                }
                return 1000;

            case "Done":
                interactWithNPC("Lead Navigator", "Travel");
                Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() < 3700);
                interactWithNPC("Barge guard", "Deboard");
                Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() < 3450);
                logOut();

        }

        return 1000;
    }

    @Override
    public boolean onStart(String... strings) {
        return true;
    }

    public static Color progressBefore;
    public static Color progressAfter = Color.BLACK;
    public static String watDo = "Try again";

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
