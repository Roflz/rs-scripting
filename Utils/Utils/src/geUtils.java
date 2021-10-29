import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.entity.WidgetChild;
import com.epicbot.api.shared.methods.IGrandExchangeAPI;
import com.epicbot.api.shared.model.ge.GrandExchangeOffer;
import com.epicbot.api.shared.model.ge.GrandExchangeSlot;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.webwalking.model.RSBank;

import javax.annotation.processing.SupportedSourceVersion;
import java.util.List;
import java.util.Objects;

public class geUtils {

    private static APIContext ctx = APIContext.get();

    public static void openGE() {
        while(!ctx.grandExchange().isOpen()) {
            ctx.grandExchange().open();
            utils.sleep(1000);
            if(ctx.grandExchange().isOpen()) {
                break;
            }
        }
    }

    public static void closeGE() {
        while(ctx.grandExchange().isOpen()) {
            ctx.grandExchange().close();
            if(!ctx.grandExchange().isOpen()) {
                break;
            }
        }
    }

    public static void goToGE() {
        ctx.webWalking().walkTo(RSBank.GRAND_EXCHANGE.getTile());
    }

    public static void makeBuyOffer(String item, int quantity) {
        ctx.grandExchange().newBuyOffer(item);
        ctx.grandExchange().setQuantity(quantity);
        ctx.grandExchange().confirmOffer();
        System.out.println("buy offer");
    }

    public static void makeBuyOfferWithPriceIncrease(String item, int quantity, int priceIncreases) {
        ctx.grandExchange().newBuyOffer(item);
        for(int i = 0 ; i < priceIncreases ; i++) {
            ctx.grandExchange().increasePriceBy5Percent();
        }
        ctx.grandExchange().setQuantity(quantity);
        ctx.grandExchange().confirmOffer();
    }

    public static void makeSellOffer(String item, int quantity) {
        ctx.grandExchange().newSellOffer(item);
        ctx.grandExchange().setQuantity(quantity);
        ctx.grandExchange().confirmOffer();
    }

    public static void makeSellOfferWithPriceDecrease(String item, int quantity, int priceDecreases) {
        ctx.grandExchange().newSellOffer(item);
        for(int i = 0 ; i < priceDecreases ; i++) {
            ctx.grandExchange().increasePriceBy5Percent();
        }
        ctx.grandExchange().setQuantity(quantity);
        ctx.grandExchange().confirmOffer();
    }

    public static void makeBuyOfferAndIncreasePriceUntilCompleted(String item, int quantity, int reOfferTime) {
        makeBuyOffer(item, quantity);
        int i = 1;
        while(!isBuyOfferCompleted(item)) {
            System.out.println("in loop");
            if(isBuyOfferCompleted(item)) {
                System.out.println("Buy offer completed");
                viewOffer(item);
//                collectBuyOfferToInventoryItem();
                break;
            }
            System.out.println("Waiting");
            waitUntilBuyOfferCompleted(item, reOfferTime);
            if(isBuyOfferCompleted(item)) {
                System.out.println("Buy offer completed");
                viewOffer(item);
//                collectBuyOfferToInventoryItem();
                break;
            }
            System.out.println("Aborting");
            abortOffer(item);
            viewAndCollectOfferItem(Objects.requireNonNull(getGESlotWithItem(item)));
//            utils.sleepUntilMax(5000, isBuyOfferCanceled(item));
//            collectSellOfferToInventory();
            makeBuyOfferWithPriceIncrease(item, quantity, i);
//            utils.sleepUntilMax(5000, isBuyOfferPending(item));
            i += 1;
        }

    }

    public static void abortOffer(String item) {
        getGESlotWithItem(item).abortOffer();
        utils.sleepUntil(3000, 5000, isSellOfferCanceled(item) || isBuyOfferCanceled(item));
    }

    public static void viewOffer(String item) {
        getGESlotWithItem(item).viewOffer();
        utils.sleepUntil(ctx.grandExchange().getCurrentScreen() == IGrandExchangeAPI.GrandExchangeScreen.ACTIVE_BUY_OFFER || ctx.grandExchange().getCurrentScreen() == IGrandExchangeAPI.GrandExchangeScreen.ACTIVE_SELL_OFFER);
    }

    public static void viewAndCollectOfferItem(GrandExchangeSlot slot) {
        slot.viewOffer();
        collectOfferToInventoryItem();
        utils.sleepUntil(slot.getState() == GrandExchangeOffer.OfferState.EMPTY);
    }

    public static void collectOfferToInventoryItem() {
        WidgetChild offerSlot1 = ctx.widgets().get(465).getChild(23).getChild(2);
        WidgetChild offerSlot2 = ctx.widgets().get(465).getChild(23).getChild(3);
        offerSlot1.interact("Collect-item");
        offerSlot1.interact("Collect-items");
        offerSlot2.interact("Collect");
    }

    public static void collectOfferToInventoryNote() {
        WidgetChild offerSlot1 = ctx.widgets().get(465).getChild(23).getChild(2);
        WidgetChild offerSlot2 = ctx.widgets().get(465).getChild(23).getChild(3);
        offerSlot1.interact("Collect-note");
        offerSlot1.interact("Collect-notes");
        offerSlot2.interact("Collect");
    }

    public static void collectOfferToBank() {
        WidgetChild offerSlot1 = ctx.widgets().get(465).getChild(23).getChild(2);
        WidgetChild offerSlot2 = ctx.widgets().get(465).getChild(23).getChild(3);
        offerSlot1.interact("Bank");
        offerSlot2.interact("Bank");
    }

    public static void collectToInventory() { ctx.grandExchange().collectToInventory(); }

    public static void collectToBank() { ctx.grandExchange().collectToBank(); }

    public static void waitUntilBuyOfferCompleted(String item, int maxTime) {
        utils.sleepUntilMax(maxTime, isBuyOfferCompleted(item));
    }

    public static void waitUntilSellOfferCompleted(String item, int maxTime) {
        utils.sleepUntilMax(maxTime, isSellOfferCompleted(item));
    }

    //for offer booleans, true if in overview screen or in view offer screen
    public static boolean isBuyOfferCompleted(String item) { return getGESlotWithItem(item).getState() == GrandExchangeOffer.OfferState.BOUGHT; }

    public static boolean isBuyOfferPending(String item) { return getGESlotWithItem(item).getState() == GrandExchangeOffer.OfferState.BUYING; }

    public static boolean isBuyOfferCanceled(String item) { return getGESlotWithItem(item).getState() == GrandExchangeOffer.OfferState.CANCELLED_BUY; }

    public static boolean isSellOfferCompleted(String item) { return getGESlotWithItem(item).getState() == GrandExchangeOffer.OfferState.SOLD; }

    public static boolean isSellOfferPending(String item) { return getGESlotWithItem(item).getState() == GrandExchangeOffer.OfferState.SELLING; }

    public static boolean isSellOfferCanceled(String item) { return getGESlotWithItem(item).getState() == GrandExchangeOffer.OfferState.CANCELLED_SELL; }

    public static boolean isOfferSlotEmpty(GrandExchangeSlot slot) { return slot.getState() == GrandExchangeOffer.OfferState.EMPTY; }

    public static boolean isOpen() { return ctx.grandExchange().isOpen(); }

    public static GrandExchangeSlot getGESlotWithItem(String item) {
        if(isOpen()) {
            List<GrandExchangeSlot> slots = ctx.grandExchange().getSlots();
            for(GrandExchangeSlot slot : slots) {
                WidgetChild slotWidget = ctx.widgets().get(465).getChild(slot.getIndex() + 7).getChild(19);
                if(slotWidget.getText().contains(item)) {
                    return slot;
                }
            }
        }
        System.out.println("Could not find GE slot with " + item);
        return null;
    }

}
