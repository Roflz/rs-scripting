import com.epicbot.api.shared.util.time.Time;

import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.*;
import com.epicbot.api.shared.event.ChatMessageEvent;
import com.epicbot.api.shared.methods.IBankAPI;
import com.epicbot.api.shared.methods.IGrandExchangeAPI;
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
import com.epicbot.api.shared.util.time.Time;
import sun.security.action.GetBooleanAction;

import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.time.Time;

import javax.imageio.ImageIO;
import javax.sound.midi.SysexMessage;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public abstract class GE extends LoopScript {

    public void openGEIfNotOpen() {
        if(!getAPIContext().grandExchange().isOpen()) {
            getAPIContext().grandExchange().open();
            Time.sleep(5_000, () -> getAPIContext().grandExchange().isOpen());
        }
    }

    public void makeBuyOffer(String item, int quantity, int priceIncreases) {
        getAPIContext().grandExchange().newBuyOffer(item);
        for(int i = 0 ; i < priceIncreases ; i++) {
            getAPIContext().grandExchange().increasePriceBy5Percent();
        }
        getAPIContext().grandExchange().setQuantity(quantity);
        getAPIContext().grandExchange().confirmOffer();
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

    public void abortOffer(GrandExchangeSlot slot) {
        getAPIContext().grandExchange().getSlot(slot.getIndex()).abortOffer();
        getAPIContext().grandExchange().collectToBank();
    }

    public GrandExchangeSlot getGESlotWithItem(String item) {
        List<GrandExchangeSlot> slots = getAPIContext().grandExchange().getSlots();
        for(GrandExchangeSlot slot : slots) {
            WidgetChild slotWidget = getAPIContext().widgets().get(465).getChild(slot.getIndex() + 7).getChild(19);
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
