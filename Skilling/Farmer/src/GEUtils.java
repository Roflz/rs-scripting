import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.entity.*;
import com.epicbot.api.shared.model.ge.GrandExchangeOffer;
import com.epicbot.api.shared.model.ge.GrandExchangeSlot;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.webwalking.model.RSBank;

import java.util.ArrayList;
import java.util.List;

public abstract class GEUtils extends LoopScript {

    private static APIContext stx = APIContext.get();

    public void openGEIfNotOpen() {
        if(!getAPIContext().grandExchange().isOpen()) {
            getAPIContext().grandExchange().open();
            Time.sleep(5_000, () -> getAPIContext().grandExchange().isOpen());
        }
    }

    public static List<String> createShoppingListItems(List<String> items, List<Integer> quantities) {
        List<String> shoppingList = new ArrayList();
        int i = 0;
        for(String item : items) {
            if(!BankUtils.doesBankHaveItemWithQuantity(item, quantities.get(i)) && !item.contains("Coins")) {
                shoppingList.add(item);
            }
            i += 1;
        }
        if(!stx.bank().contains("Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)", "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)")) {
            shoppingList.add("Skills necklace(6)");
        }
        return shoppingList;
    }

    public static List<Integer> createShoppingListQuantities(List<String> items, List<Integer> quantities) {
        List<Integer> shoppingList = new ArrayList();
        int i = 0;
        for(String item : items) {
            if(!BankUtils.doesBankHaveItemWithQuantity(item, quantities.get(i)) && !item.contains("Coins")) {
                shoppingList.add(quantities.get(i));
            }
            i += 1;
        }
        if(!stx.bank().contains("Skills necklace(1)", "Skills necklace(2)", "Skills necklace(3)", "Skills necklace(4)", "Skills necklace(5)", "Skills necklace(6)")) {
            shoppingList.add(5);
        }
        return shoppingList;
    }

    public static void goToGEAndOpen() {
        if(!stx.bank().isOpen()) {
            stx.webWalking().walkTo(RSBank.GRAND_EXCHANGE.getTile());
            stx.grandExchange().open();
            Time.sleep(5_000, () -> stx.grandExchange().isOpen());
        } else {
            System.out.println("GE is already open!");
        }
    }

    public static void openGE() {
        stx.grandExchange().open();
        Time.sleep(5_000, () -> stx.grandExchange().isOpen());
    }

    public static void goToGE() {

        stx.webWalking().walkTo(RSBank.GRAND_EXCHANGE.getTile());
    }

    public static void buyItemsFromShoppingList(List<String> items, List<Integer> quantities) {
        int i = 0;
        for(String item : items) {
            if(item.contains("Rake") || item.contains("Seed dibber") || item.contains("Spade")) {
                makeBuyOffer(item, quantities.get(i));
            } else {
                makeBuyOffer(item, quantities.get(i) * 10);
            }
            stx.grandExchange().confirmOffer();
            i += 1;
        }
    }

    public static boolean anySaleComplete(List<String> items) {
        for(String item : items) {
            if(saleComplete(item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean anySaleComplete() {
        List<GrandExchangeSlot> slots = stx.grandExchange().getSlots();
        for(GrandExchangeSlot slot : slots) {
            if (slot.isCompleted()) {
                return true;
            }
        }
        return false;
    }

    public static boolean anySaleNotYetComplete(List<String> items) {
        for(String item : items) {
            if(saleNotYetComplete(item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean saleNotYetComplete(String item) {
        List<GrandExchangeSlot> slots = stx.grandExchange().getSlots();
        for(GrandExchangeSlot slot : slots) {
            WidgetChild slotWidget = stx.widgets().get(465).getChild(slot.getIndex() + 7).getChild(19);
            if(slotWidget.getText().contains(item)) {
                if(!slot.isCompleted()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<String> getSalesNotYetCompleteWithNames(List<String> itemNames) {
        List<String> notYetComplete = new ArrayList();
        List<GrandExchangeSlot> slots = stx.grandExchange().getSlots();
        for(GrandExchangeSlot slot : slots) {
            WidgetChild slotWidget = stx.widgets().get(465).getChild(slot.getIndex() + 7).getChild(19);
            for(String item : itemNames) {
                if(!slot.isCompleted() && slotWidget.getText().contains(item)) {
                    notYetComplete.add(slotWidget.getText());
                }
            }
        }
        return notYetComplete;
    }

    public static boolean saleComplete(String item) {
        List<GrandExchangeSlot> slots = stx.grandExchange().getSlots();
        for(GrandExchangeSlot slot : slots) {
            WidgetChild slotWidget = stx.widgets().get(465).getChild(slot.getIndex() + 7).getChild(19);
            if(slotWidget.getText().contains(item)) {
                if(!slot.isCompleted()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean allSalesComplete(List<String> items) {
        for(String item : items) {
            if(!saleComplete(item)) {
                return false;
            }
        }
        return true;
    }

    public static boolean doesInventoryHaveItemsWithQuantity(List<String> items, List<Integer> quantities) {
        int i = 0;
        System.out.println("in removal loop");
        for(String item : items) {
            if(stx.inventory().getItem(item) != null) {
                if(stx.inventory().getItem(item).getStackSize() >= quantities.get(i)) {
                    System.out.println("removing " + item + " from " + items);
                    System.out.println("removing " + quantities.get(i) + " from " + quantities);
                    main.shoppingListItems.remove(i);
                    main.shoppingListQuantities.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    public static void makeIncreasingBuyOffer(String item, int quantity, int priceIncreases) {
        stx.grandExchange().newBuyOffer(item);
        for(int i = 0 ; i < priceIncreases ; i++) {
            stx.grandExchange().increasePriceBy5Percent();
        }
        stx.grandExchange().setQuantity(quantity);
        stx.grandExchange().confirmOffer();
    }

    public static void makeBuyOffer(String item, int quantity) {
        stx.grandExchange().newBuyOffer(item);
        stx.grandExchange().setQuantity(quantity);
        stx.grandExchange().confirmOffer();
    }

    public static void makeBuyOffer(String item, int quantity, int price) {
        stx.grandExchange().newBuyOffer(item);
        stx.grandExchange().setQuantity(quantity);
        stx.grandExchange().setPrice(price);
        stx.grandExchange().confirmOffer();
    }

    public void makeSellOffer(String item, int quantity) {
        getAPIContext().grandExchange().newSellOffer(item);
        getAPIContext().grandExchange().setQuantity(quantity);
        getAPIContext().grandExchange().confirmOffer();
    }

    public void makeSellOffer(String item) {
        getAPIContext().grandExchange().newSellOffer(item);
        getAPIContext().grandExchange().confirmOffer();
    }

    public static void abortOffer(GrandExchangeSlot slot) {
        stx.grandExchange().getSlot(slot.getIndex()).abortOffer();
        stx.grandExchange().collectToInventory();
    }

    public static GrandExchangeSlot getGESlotWithItem(String item) {
        List<GrandExchangeSlot> slots = stx.grandExchange().getSlots();
        for(GrandExchangeSlot slot : slots) {
            WidgetChild slotWidget = stx.widgets().get(465).getChild(slot.getIndex() + 7).getChild(19);
            if(slotWidget.getText().contains(item)) {
                return slot;
            }
        }
        System.out.println("Could not find GE slot with " + item);
        return null;
    }

    public boolean buyOfferCompleted(GrandExchangeSlot slot) {
        return slot.getState() == GrandExchangeOffer.OfferState.BOUGHT;
    }

//    public boolean buyOfferPartiallyCompleted(GrandExchangeSlot slot) {
//        return slot.getState() == GrandExchangeOffer.OfferState.;
//    }

    public boolean sellOfferCompleted(GrandExchangeSlot slot) {
        return slot.getState() == GrandExchangeOffer.OfferState.SOLD;
    }

//    public int getAmountSold(GrandExchangeSlot slot) {
//        if(slot.getState() == GrandExchangeOffer.OfferState.SELLING) {
//            slot.viewOffer();
//        } else if(slot.getState() == GrandExchangeOffer.OfferState.)
//    }

}
