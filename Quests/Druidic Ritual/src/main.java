import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.methods.IQuestAPI;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.time.Time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ScriptManifest(name = "Druidic Ritual", gameType = GameType.OS)
public class main extends LoopScript {

    public static String watDo = "Put meats in Cauldron";

    private final List<String> startingItemsList = Arrays.asList("Raw bear meat", "Raw rat meat", "Raw beef", "Raw chicken");
    private final List<Integer> startingQuantitiesList = Arrays.asList(1, 1, 1, 1);
    public static List<String> shoppingListItems = new ArrayList();
    public static List<Integer> shoppingListQuantities = new ArrayList();
    private int geBuyCounter = 0;
    private int gePriceIncreaseCounter = 1;

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

    @Override
    protected int loop() {
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
                } else if(InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)){
                    watDo = "Start Quest";
                    return 1000;
                }
                if(getAPIContext().bank().isOpen()) {
                    System.out.println("banks open");
                    if(BankUtils.doesBankHaveItemsWithQuantity(startingItemsList, startingQuantitiesList) || InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                        System.out.println("Withdrawing starting items from Bank");
                        BankUtils.depositInventoryIfNotEmpty();
                        BankUtils.withdrawItemsFromListWithQuantity(startingItemsList, startingQuantitiesList);
                        watDo = "Start Quest";
                        return 500;
                    } else {
                        BankUtils.depositInventoryIfNotEmpty();
                        getAPIContext().bank().withdrawAll("Coins");
                        shoppingListItems = GEUtils.createShoppingListItems(startingItemsList, startingQuantitiesList);
                        shoppingListQuantities = GEUtils.createShoppingListQuantities(startingItemsList, startingQuantitiesList);
                        BankUtils.closeBank();
                        System.out.println("Bank does not have items, so going to buy them from the GE");
                        watDo = "Buy items from GE";
                        return 1000;
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
                        GEUtils.buyItemsFromShoppingList(shoppingListItems, shoppingListQuantities, 1_000);
                        watDo = "Wait for sales from GE";
                        return 500;
                    }
                }
                break;
            case "Wait for sales from GE":
                if(GEUtils.anySaleNotYetComplete(shoppingListItems) && shoppingListItems.size() > 0 &&  gePriceIncreaseCounter <= 10) {
                    if(geBuyCounter >= 10) {
                        int i = 0;
                        for(String item : GEUtils.getSalesNotYetCompleteWithNames(shoppingListItems)) {
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
                if(GEUtils.allSalesComplete(shoppingListItems) || shoppingListItems.size() == 0) {
                    getAPIContext().grandExchange().collectToBank();
                    getAPIContext().grandExchange().close();
                    watDo = "Bank";
                    return 500;
                }
                break;

            case "Start Quest":
                if(getAPIContext().quests().isStarted(IQuestAPI.Quest.DRUIDIC_RITUAL)) {
                    watDo = "Go to Sanfew";
                    return 1000;
                }
                if(!Constants.STONE_CIRCLE.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.STONE_CIRCLE.getCentralTile());
                    return 1000;
                } else {
                    NPC kaqemeex = getAPIContext().npcs().query().named("Kaqemeex").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        kaqemeex.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("I'm in search of a quest.")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("Okay, I will try and help.")) {
                            return 1000;
                        }
                    }
                }
                break;

            case "Go to Sanfew":
                if(!Constants.SANFEW.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.SANFEW.getCentralTile());
                    return 1000;
                } else {
                    NPC sanfew = getAPIContext().npcs().query().named("Sanfew").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        sanfew.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("I've been sent to help purify the Varrock stone circle.")) {
                            return 1000;
                        } else {
                            watDo = "Go to Cauldron";
                            return 1000;
                        }
                    }
                }
                break;

            case "Go to Cauldron":
                if(!Constants.CAULDRON.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.CAULDRON.getCentralTile());
                } else {
                    watDo = "Put meats in Cauldron";
                }
                return 1000;

            case "Put meats in Cauldron":
                SceneObject cauldron = getAPIContext().objects().query().named("Cauldron of Thunder").results().nearest();
                for(String item : startingItemsList) {
                    getAPIContext().inventory().selectItem(item);
                    cauldron.interact();
                    Time.sleep(3_000, () -> !getAPIContext().inventory().contains(item));
                }
                watDo = "Return to Sanfew";
                return 1000;
            case "Return to Sanfew":
                if(!Constants.SANFEW.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.SANFEW.getCentralTile());
                    return 1000;
                } else {
                    NPC sanfew = getAPIContext().npcs().query().named("Sanfew").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        sanfew.interact("Talk-to");
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            Time.sleep(2_000, () -> getAPIContext().dialogues().isDialogueOpen());
                        } else {
                            watDo = "Return to Kaqemeex";
                        }
                        return 1000;
                    }
                }
                break;

            case "Return to Kaqemeex":
                if(!Constants.STONE_CIRCLE.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.STONE_CIRCLE.getCentralTile());
                    return 1000;
                } else {
                    NPC kaqemeex = getAPIContext().npcs().query().named("Kaqemeex").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        kaqemeex.interact("Talk-to");
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                        } else {
                            watDo = "Done";
                        }
                    }
                    return 1000;
                }
            case "Done":
                if(getAPIContext().widgets().get(153).isVisible()) {
                    getAPIContext().widgets().get(153).getChild(16).interact("Close");
                    return 1000;
                }
                GenUtils.logOut();
        }


        return 1000;
    }
    @Override
    public boolean onStart(String... strings) {
        return true;
    }
}
