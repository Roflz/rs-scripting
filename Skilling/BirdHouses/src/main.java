import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static utils.BankUtils.*;
import static utils.Constants.*;
import static utils.GenUtils.*;
import static utils.InventoryUtils.*;
import static utils.GEUtils.*;

@ScriptManifest(name = "BirdHouses", gameType = GameType.OS)
public class main extends LoopScript {

    private List<String> startingItemsList = Arrays.asList();
    private List<Integer> startingQuantitiesList = Arrays.asList();
    public static List<String> shoppingListItems = new ArrayList();
    public static List<Integer> shoppingListQuantities = new ArrayList();
    private int geBuyCounter = 0;
    private int gePriceIncreaseCounter = 1;

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
                    System.out.println("banks open");
                    if (doesBankHaveItemsWithQuantity(startingItemsList, startingQuantitiesList) || doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                        System.out.println("Withdrawing starting items from Bank");
                        depositInventoryIfNotEmpty();
                        getAPIContext().bank().depositEquipment();
                        //Withdraw and equip starting items here
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
                        //If not using wait for sales, i.e. buying way overpriced, use below method
                        //buyItemsFromShoppingList(shoppingListItems, shoppingListQuantities, 1000);
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
                break;
        }

        return 1000;
    }

    @Override
    public boolean onStart(String... strings) {
        return true;
    }

    public static String watDo = "Done";

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
