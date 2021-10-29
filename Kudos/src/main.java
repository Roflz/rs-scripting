import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.model.SceneOffset;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.time.Time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static utils.BankUtils.*;
import static utils.Constants.*;
import static utils.GenUtils.*;
import static utils.InventoryUtils.*;
import static utils.GEUtils.*;

@ScriptManifest(name = "Kudos", gameType = GameType.OS)
public class main extends LoopScript {



    private List<String> startingItemsList = Arrays.asList("Leather boots", "Leather gloves", "Trowel", "Rock pick", "Specimen brush");
    private List<Integer> startingQuantitiesList = Arrays.asList(1, 1, 1, 1, 1);
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
                    wear("Leather boots", "Leather gloves");
                    watDo = "Go to Varrock Museum";
                    return 1000;
                }
                if (getAPIContext().bank().isOpen()) {
                    System.out.println("banks open");
                    if (doesBankHaveItemsWithQuantity(startingItemsList, startingQuantitiesList) || doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                        System.out.println("Withdrawing starting items from Bank");
                        depositInventoryIfNotEmpty();
                        getAPIContext().bank().depositEquipment();
                        withdrawItemsFromListWithQuantity(startingItemsList, startingQuantitiesList);
                        wear("Leather boots", "Leather gloves");
                        watDo = "Go to Varrock Museum";
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
                        buyItemsFromShoppingList(shoppingListItems, shoppingListQuantities, 1000);
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

            case "Go to Varrock Museum":
                if(!playerisInArea(VARROCK_MUSEUM)) {
                    walkToAreaCentralTile(VARROCK_MUSEUM);
                } else {
                    watDo = "Go into cleaning area";
                    return 1000;
                }

            case "Go into cleaning area":
                if(getAPIContext().widgets().get(532).getChild(2).getChild(1).getText().contains("50/230")) {
                    watDo = "Start quiz";
                    return 1000;
                }
                if(!playerLocationIs(new Tile(3261, 3446, 0))) {
                    interactWithObject("Gate", "Open");
                    Time.sleep(5_000, () -> playerLocationIs(new Tile(3261, 3446, 0)));
                } else {
                    watDo = "Gather and clean finds";
                    return 1000;
                }
                break;

            case "Gather and clean finds":
                if(inventoryContainsAny("Ancient symbol", "Old symbol", "Pottery", "Ancient coin", "Old coin", "Clean necklace")) {
                    watDo = "Drop or show finds";
                    return 1000;
                }
                if(!inventoryIsFull() && !inventoryContains("Uncleaned find")) {
                    interactWithObject("Dig Site specimen rocks", "Take");
                    Time.sleep(30_000, ()-> inventoryIsFull());
                    return 1000;
                } else if(inventoryContains("Uncleaned find")){
                    interactWithObject("Specimen table", "Clean");
                    Time.sleep(60_000, () -> !inventoryContains("Uncleaned find"));
                    return 1000;
                } else {
                    watDo = "Drop or show finds";
                    return 1000;
                }

            case "Drop or show finds":
                dropAllExcept("Trowel", "Rock pick", "Specimen brush", "Ancient symbol", "Old symbol", "Pottery", "Ancient coin", "Old coin", "Clean necklace");
                if(inventoryContainsAny("Ancient symbol", "Old symbol", "Pottery", "Ancient coin", "Old coin", "Clean necklace")) {
                    if(dialogueText().contains("45")) {
                        selectContinueUntilDialogueIsGone();
                        watDo = "Put old coin in display case 45";
                        return 1000;
                    } else if(dialogueText().contains("44")) {
                        selectContinueUntilDialogueIsGone();
                        watDo = "Put ancient coin in display case 44";
                        return 1000;
                    } else if(dialogueText().contains("37")) {
                        selectContinueUntilDialogueIsGone();
                        watDo = "Put ancient symbol in display case 37";
                        return 1000;
                    } else if(dialogueText().contains("36")) {
                        selectContinueUntilDialogueIsGone();
                        watDo = "Put old symbol in display case 36";
                        return 1000;
                    } else if(dialogueText().contains("22")) {
                        selectContinueUntilDialogueIsGone();
                        watDo = "Put pottery in display case 22";
                        return 1000;
                    }
                    talkToNPC("Thias Leacke", "I found something interesting.");
                    return 1000;
                } else {
                    watDo = "Gather and clean finds";
                    return 1000;
                }

            case "Put old coin in display case 45":
                if(!playerLocationIs(new Tile(3260, 3448, 0))) {
                    if(!playerLocationIs(new Tile(3261, 3447, 0))) {
                        interactWithObject("Gate", "Open");
                        Time.sleep(5_000, () -> playerLocationIs(new Tile(3261, 3447, 0)));
                    }
                    walkToTile(new Tile(3260, 3448, 0));
                } else {
                    useItemOnObjectAndWaitForDialogue("Old coin", "Display case");
                    selectContinue();
                    watDo = "Go into cleaning area";
                    return 1000;
                }
                break;

            case "Put ancient coin in display case 44":
                if(!playerLocationIs(new Tile(3260, 3450, 0))) {
                    if(!playerLocationIs(new Tile(3261, 3447, 0))) {
                        interactWithObject("Gate", "Open");
                        Time.sleep(5_000, () -> playerLocationIs(new Tile(3261, 3447, 0)));
                    }
                    walkToTile(new Tile(3260, 3450, 0));
                } else {
                    useItemOnObjectAndWaitForDialogue("Ancient coin", "Display case");
                    selectContinue();
                    watDo = "Go into cleaning area";
                    return 1000;
                }
                break;

            case "Put ancient symbol in display case 37":
                if(!playerLocationIs(new Tile(3263, 3450, 0))) {
                    if(!playerLocationIs(new Tile(3261, 3447, 0))) {
                        interactWithObject("Gate", "Open");
                        Time.sleep(5_000, () -> playerLocationIs(new Tile(3261, 3447, 0)));
                    }
                    walkToTile(new Tile(3263, 3450, 0));
                } else {
                    useItemOnObjectAndWaitForDialogue("Ancient symbol", "Display case");
                    selectContinue();
                    watDo = "Go into cleaning area";
                    return 1000;
                }
                break;

            case "Put old symbol in display case 36":
                if(!playerLocationIs(new Tile(3263, 3448, 0))) {
                    if(!playerLocationIs(new Tile(3261, 3447, 0))) {
                        interactWithObject("Gate", "Open");
                        Time.sleep(5_000, () -> playerLocationIs(new Tile(3261, 3447, 0)));
                    }
                    walkToTile(new Tile(3263, 3448, 0));
                } else {
                    useItemOnObjectAndWaitForDialogue("Old symbol", "Display case");
                    selectContinue();
                    watDo = "Go into cleaning area";
                    return 1000;
                }
                break;

            case "Put pottery in display case 22":
                if(!playerLocationIs(new Tile(3260, 3452, 0))) {
                    if(!playerLocationIs(new Tile(3261, 3447, 0))) {
                        interactWithObject("Gate", "Open");
                        Time.sleep(5_000, () -> playerLocationIs(new Tile(3261, 3447, 0)));
                    }
                    walkToTile(new Tile(3260, 3452, 0));
                } else {
                    useItemOnObjectAndWaitForDialogue("Pottery", "Display case");
                    selectContinue();
                    watDo = "Go into cleaning area";
                    return 1000;
                }
                break;

            case "Start quiz":
                if(!playerLocationIs(new Tile(1759, 4954, 0))) {
                    interactWithObject("Stairs", "Walk-down");
                    Time.sleep(10_000, () -> getAPIContext().localPlayer().getY() > 4000);
                    walkToTile(new Tile(1759, 4954, 0));
                    return 1000;
                } else if(dialogueIsOpen()) {
                    talkToNPC("Orlando Smith", "Sure thing.");
                    return 1000;
                } else {
                    watDo = "South room";
                    return 1000;
                }

            case "South room":
                walkToAreaCentralTile(VARROCK_MUSEUM_BASEMENT_SOUTH_ROOM);
                List<SceneObject> plaques = getAPIContext().objects().query().named("Plaque").results().nearestList();
                for(SceneObject plaque : plaques) {
                    interactWithObject(plaque, "Study");
                    Time.sleep(10_000, () -> getAPIContext().widgets().get(533).isVisible());
                    if(getAPIContext().widgets().get(533).isVisible()) {
                        answerQuizQuestions("10",
                                "Are kalphites carnivores, herbivores, or omnivores?",
                                "Kalphites are ruled by a...?",
                                "What is the lowest caste in kalphite society?",
                                "What are the armoured plates on a kalphite called?",
                                "What are kalphites assumed to have evolved from?",
                                "Name the prominent figure in kalphite mythology?",
                                31, 30, 31, 30, 29, 30);
                        answerQuizQuestions("9",
                                "What is a terrorbird's preferred food?",
                                "Who use terrorbirds as mounts?",
                                "Where do terrorbirds get most of their water?",
                                "How many claws do terrorbirds have?",
                                "What do terrorbirds eat to aid digestion?",
                                "How many teeth do terrorbirds have?",
                                29,30,31,30,30,31);
                    }
                }
                watDo = "East room";
                return 1000;

            case "East room":
                walkToAreaCentralTile(VARROCK_MUSEUM_BASEMENT_EAST_ROOM);
                plaques = getAPIContext().objects().query().named("Plaque").results().nearestList();
                for(SceneObject plaque : plaques) {
                    interactWithObject(plaque, "Study");
                    Time.sleep(10_000, () -> getAPIContext().widgets().get(533).isVisible());
                    if(getAPIContext().widgets().get(533).isVisible()) {
                        answerQuizQuestions("10",
                                "What is special about the shell of the giant Morytanian snail?",
                                "How do Morytanian snails capture their prey?",
                                "Which of these is a snail byproduct?",
                                "What does 'Achatina Acidia' mean?",
                                "How do snails move?",
                                "What is the 'trapdoor', which snails use to cover the entrance to their shells called?",
                                31, 30, 29, 30, 30, 29);
                        answerQuizQuestions("9",
                                "What is snake venom adapted from?",
                                "Aside from their noses, what do snakes use to smell?",
                                "If a snake sticks its tongue out at you, what is it doing?",
                                "If some snakes use venom to kill their prey, what do other snakes use?",
                                "Lizards and snakes belong to the same order - what is it?",
                                "Which habitat do snakes prefer?",
                                30,29,31,30,29,31);
                    }
                }
                watDo = "East room";
                return 1000;

            case "North room":
                walkToAreaCentralTile(VARROCK_MUSEUM_BASEMENT_EAST_ROOM);
                plaques = getAPIContext().objects().query().named("Plaque").results().nearestList();
                for(SceneObject plaque : plaques) {
                    interactWithObject(plaque, "Study");
                    Time.sleep(10_000, () -> getAPIContext().widgets().get(533).isVisible());
                    if(getAPIContext().widgets().get(533).isVisible()) {
                        answerQuizQuestions("10",
                                "Are kalphites carnivores, herbivores, or omnivores?",
                                "Kalphites are ruled by a...?",
                                "What is the lowest caste in kalphite society?",
                                "What are the armoured plates on a kalphite called?",
                                "What are kalphites assumed to have evolved from?",
                                "Name the prominent figure in kalphite mythology?",
                                31, 30, 31, 30, 29, 30);
                        answerQuizQuestions("9",
                                "What is a terrorbird's preferred food?",
                                "Who use terrorbirds as mounts?",
                                "Where do terrorbirds get most of their water?",
                                "How many claws do terrorbirds have?",
                                "What do terrorbirds eat to aid digestion?",
                                "How many teeth do terrorbirds have?",
                                29,30,31,30,30,31);
                    }
                }
                watDo = "East room";
                return 1000;

            case "West room":
                walkToAreaCentralTile(VARROCK_MUSEUM_BASEMENT_EAST_ROOM);
                plaques = getAPIContext().objects().query().named("Plaque").results().nearestList();
                for(SceneObject plaque : plaques) {
                    interactWithObject(plaque, "Study");
                    Time.sleep(10_000, () -> getAPIContext().widgets().get(533).isVisible());
                    if(getAPIContext().widgets().get(533).isVisible()) {
                        answerQuizQuestions("10",
                                "Are kalphites carnivores, herbivores, or omnivores?",
                                "Kalphites are ruled by a...?",
                                "What is the lowest caste in kalphite society?",
                                "What are the armoured plates on a kalphite called?",
                                "What are kalphites assumed to have evolved from?",
                                "Name the prominent figure in kalphite mythology?",
                                31, 30, 31, 30, 29, 30);
                        answerQuizQuestions("9",
                                "What is a terrorbird's preferred food?",
                                "Who use terrorbirds as mounts?",
                                "Where do terrorbirds get most of their water?",
                                "How many claws do terrorbirds have?",
                                "What do terrorbirds eat to aid digestion?",
                                "How many teeth do terrorbirds have?",
                                29,30,31,30,30,31);
                    }
                }
                watDo = "East room";
                return 1000;

        }
        return 1000;
    }

    public static String watDo = "South room";

    @Override
    public boolean onStart(String... strings) {
        return true;
    }

    public void answerQuizQuestions(String displayNumber, String q1, String q2, String q3, String q4, String q5, String q6, int a1, int a2, int a3, int a4, int a5, int a6) {
        if(getAPIContext().widgets().get(533).getChild(25).getText().contains(displayNumber)) {
            for(int i = 0 ; i < 3 ; i++) {
                System.out.println("in loop");
                if(getAPIContext().widgets().get(533).isVisible()) {
                    if(getAPIContext().widgets().get(533).getChild(28).getText().contains(q1)) {
                        getAPIContext().widgets().get(533).getChild(a1).interact();
                        Time.sleep(5_000, () -> dialogueIsOpen());
                        selectContinueUntilDialogueIsGone();
                    } else if(getAPIContext().widgets().get(533).getChild(28).getText().contains(q2)) {
                        getAPIContext().widgets().get(533).getChild(a2).interact();
                        Time.sleep(5_000, () -> dialogueIsOpen());
                        selectContinueUntilDialogueIsGone();
                    } else if(getAPIContext().widgets().get(533).getChild(28).getText().contains(q3)) {
                        getAPIContext().widgets().get(533).getChild(a3).interact();
                        Time.sleep(5_000, () -> dialogueIsOpen());
                        selectContinueUntilDialogueIsGone();
                    } else if(getAPIContext().widgets().get(533).getChild(28).getText().contains(q4)) {
                        getAPIContext().widgets().get(533).getChild(a4).interact();
                        Time.sleep(5_000, () -> dialogueIsOpen());
                        selectContinueUntilDialogueIsGone();
                    } else if(getAPIContext().widgets().get(533).getChild(28).getText().contains(q5)) {
                        getAPIContext().widgets().get(533).getChild(a5).interact();
                        Time.sleep(5_000, () -> dialogueIsOpen());
                        selectContinueUntilDialogueIsGone();
                    } else if(getAPIContext().widgets().get(533).getChild(28).getText().contains(q6)) {
                        getAPIContext().widgets().get(533).getChild(a6).interact();
                        Time.sleep(5_000, () -> dialogueIsOpen());
                        selectContinueUntilDialogueIsGone();
                    }
                }
            }
        }
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
