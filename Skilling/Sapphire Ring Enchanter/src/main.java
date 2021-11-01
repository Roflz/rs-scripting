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

@ScriptManifest(name = "Recoil FACTORY", gameType = GameType.OS)
public class main extends LoopScript {

    private int geBuyCounter = 0;
    private int gePriceIncreaseCounter = 1;

    private boolean BUY_MORE_RINGS = false;

    public boolean needsToBank() {
        return !getAPIContext().inventory().contains("Cosmic rune") || !getAPIContext().inventory().contains("Sapphire ring") || !getAPIContext().equipment().contains("Staff of water");
    }

    public boolean needsToOpenBank() {
        return !getAPIContext().bank().isOpen();
    }

    public void openBank() {

        if (getAPIContext().magic().isSpellSelected()) {
            getAPIContext().mouse().click(450, 265);

        } else {
            getAPIContext().bank().open();
        }

    }
    public boolean shouldLogOut() {
        return getAPIContext().bank().getCount("Sapphire ring") == 0 ||
                (getAPIContext().inventory().getCount("Cosmic rune") == 0 && getAPIContext().bank().getCount("Cosmic rune") == 0);
    }

    public void logOut() {
        if (getAPIContext().bank().isOpen()) {
            getAPIContext().bank().close(); }
        getAPIContext().game().logout();
        getAPIContext().script().stop("Das it bro, was it worth it?");



    }

    public boolean doesNeedToDeposit() {
        return getAPIContext().inventory().getCount("Ring of recoil") == 27;
    }

    public void depositRings() {
        getAPIContext().bank().depositAllExcept("Cosmic rune");
    }

    public boolean needsWithdrawSapphirerings() {
        return getAPIContext().inventory().getCount("Ring of recoil") == 0 && getAPIContext().inventory().getCount("Sapphire ring") == 0 &&
                getAPIContext().inventory().contains("Cosmic rune") && getAPIContext().equipment().contains("Staff of water");
    }

    public boolean needsWithdrawCosmicRunes() {
        return getAPIContext().inventory().getCount("Cosmic rune") == 0;
    }

    public boolean needsMoreSapphireRings() {
        return getAPIContext().bank().getCount("Sapphire ring") == 0;
    }

    public boolean needsEquipWaterStaff() {
        return !getAPIContext().equipment().contains("Staff of water");
    }

    public void equipWaterStaff() {
        getAPIContext().inventory().interactItem("Wield", "Staff of water");
    }

    public void withdrawSapphireRing() {
        getAPIContext().bank().withdrawAll("Sapphire ring");
    }

    public void withdrawCosmicRunes() {
        getAPIContext().bank().withdrawAll("Cosmic rune");
    }

    public void sellRingsOfRecoil() {
        getAPIContext().bank().selectWithdrawMode(IBankAPI.WithdrawMode.NOTE);
        getAPIContext().bank().withdrawAll("Ring of recoil");
        getAPIContext().bank().selectWithdrawMode(IBankAPI.WithdrawMode.ITEM);
        getAPIContext().bank().close();
        sellAllItemToGE("Ring of recoil");
        getAPIContext().grandExchange().close();
    }

    public void buyStaffOfWater() {
        buyItemFromGE("Staff of water", 1);
    }







    public void waitAndCollectOfferFromGE(String item, int quantity) {
        GrandExchangeSlot itemSlot = getGESlotWithItem(item);
        int buyCounter = 0;
        int increaseCounter = 1;
        while(!itemSlot.isCompleted()) {
            itemSlot = getGESlotWithItem(item);
            if(itemSlot.isCompleted()) {
                getAPIContext().grandExchange().collectToBank();
                break;
            }
            if(buyCounter == 6) {
                abortOffer(itemSlot);
                makeBuyOffer(item, quantity, increaseCounter);
                increaseCounter += 1;
                buyCounter = 0;
            }
            Time.sleep(5000);
            buyCounter += 1;
        }
//        List<GrandExchangeSlot> slots = getAPIContext().grandExchange().getSlots();
//        for(GrandExchangeSlot slot : slots) {
//            WidgetChild slotWidget = getAPIContext().widgets().get(465).getChild(slot.getIndex() + 7).getChild(19);
//
//            if(slotWidget.getText().contains(item) && !slot.isCompleted()) {
//                int buyCounter = 0;
//                int increaseCounter = 1;
//                while(!slot.isCompleted()) {
//                    System.out.println("Waiting for GE purchase completion ");
//                    if(slot.isCompleted()) {
//                        getAPIContext().grandExchange().collectToBank();
////                        break;
//                    }
//                    if(slot.getState() == GrandExchangeOffer.OfferState.EMPTY) {
//                        //break;
//                    }
//                    if(buyCounter == 6) {
//                        abortOffer(slot);
//                        makeBuyOffer(item, quantity, increaseCounter);
//                        increaseCounter += 1;
//                    }
//                    Time.sleep(5_000);
//                    buyCounter += 1;
//                }
//            } else if(slotWidget.getText().contains(item) && slot.isCompleted()) {
//                getAPIContext().grandExchange().collectToBank();
//                Time.sleep(500, 2000, () -> slot.getState() == GrandExchangeOffer.OfferState.EMPTY);
//            }
//        }
    }

    public boolean needsToEnchant() {
        return getAPIContext().inventory().contains("Cosmic Rune") && getAPIContext().inventory().contains("Sapphire ring");

    }

    public void enchantsRings() {
        int count = getAPIContext().inventory().getCount(true, "Ring of recoil");
        if (!getAPIContext().magic().isSpellSelected()) {
            if (getAPIContext().magic().cast(Spell.Modern.LEVEL_1_ENCHANT)) {
                Time.sleep(500, 1000, () -> getAPIContext().magic().isSpellSelected());
            }
        } else {
            ItemWidget ring = getAPIContext().inventory().getItem("Sapphire ring");
            if (ring != null) {
                if (ring.interact()) {
                    Time.sleep(500, 1000, () -> getAPIContext().inventory().getCount(true, "Ring of recoil") > count);
                }
            }
        }
    }

    public final String formatTime(long ms) {
        long s = ms / 1000, m = s / 60, h = m / 60, d = h / 24;
        s %= 60;
        m %= 60;
        h %= 24;

        return d > 0 ? String.format("%02d:%02d:%02d:%02d", d, h, m, s) :
                h > 0 ? String.format("%02d:%02d:%02d", h, m, s) :
                        String.format("%02d:%02d", m, s);
    }


    @Override
    public boolean onStart(String... strings) {
        System.out.println("Lets enchant some rings");
        return true;


    }




    @Override

    protected int loop() {
        if (!getAPIContext().client().isLoggedIn()) {
            System.out.println("hold up till you are logged in mayne");
            return 300;
        }
        if (needsToBank() && needsToOpenBank() && !getAPIContext().grandExchange().isOpen()) {
            System.out.println("Opening bank");
            openBank();
            return 300;
        }
        if (getAPIContext().grandExchange().isOpen()) {
            if(BUY_MORE_RINGS == true) {
                if(getGESlotWithItem("Sapphire ring") == null) {
                    buyItemFromGE("Sapphire ring", 500);
                    return 1000;
                }
                if(getGESlotWithItem("Sapphire ring").getState() == GrandExchangeOffer.OfferState.BOUGHT) {
                    getAPIContext().grandExchange().collectToBank();
                    getAPIContext().grandExchange().close();
                    gePriceIncreaseCounter = 1;
                    return 1000;
                } else {
                    System.out.println(geBuyCounter);
                    if(geBuyCounter >= 30) {
                        getAPIContext().grandExchange().collectToBank();
                        abortOffer(getGESlotWithItem("Sapphire ring"));
                        makeBuyOffer("Sapphire ring", 500, gePriceIncreaseCounter);
                        geBuyCounter = 0;
                        gePriceIncreaseCounter += 1;
                        return 1000;
                    }
                    geBuyCounter += 1;
                    return 1000;
                }
            }
            if (needsEquipWaterStaff() && getGESlotWithItem("Staff of water").getState() == null) {
                buyItemFromGE("Staff of water", 1);
                return 1000;
            }
            if(getGESlotWithItem("Staff of water").getState() == GrandExchangeOffer.OfferState.BOUGHT) {
                getAPIContext().grandExchange().collectToBank();
                getAPIContext().grandExchange().close();
                gePriceIncreaseCounter = 1;
                return 1000;
            }
            else {
                System.out.println(geBuyCounter);
                if(geBuyCounter >= 30) {
                    abortOffer(getGESlotWithItem("Staff of water"));
                    makeBuyOffer("Staff of water", 1, gePriceIncreaseCounter);
                    geBuyCounter = 0;
                    gePriceIncreaseCounter += 1;
                    return 1000;
                }
                geBuyCounter += 1;
                return 1000;
            }
        }
        if (getAPIContext().bank().isOpen()) {
            if (doesNeedToDeposit()) {
                System.out.println("Depositing rings");
                depositRings();
                return 200;
            }
            if (needsMoreSapphireRings()) {
                getAPIContext().bank().depositInventory();
                getAPIContext().bank().withdrawAll("Coins");
                getAPIContext().bank().close();
                getAPIContext().grandExchange().open();
                BUY_MORE_RINGS = true;
                return 1000;
            }
            if (shouldLogOut()) {
                sellRingsOfRecoil();
                logOut();
            }
            if (needsEquipWaterStaff()) {
                if(getAPIContext().inventory().contains("Staff of water")) {
                    equipWaterStaff();
                } else if (getAPIContext().bank().contains(("Staff of water"))) {
                    getAPIContext().bank().depositInventory();
                    getAPIContext().bank().withdraw(1, "Staff of water");
                    equipWaterStaff();
                } else {
                    getAPIContext().bank().depositInventory();
                    getAPIContext().bank().withdrawAll("Coins");
                    getAPIContext().bank().close();
                    getAPIContext().grandExchange().open();
                    return 1000;
                }
            }

            if (needsWithdrawCosmicRunes()) {
                System.out.println("Withdrawing Cosmic runes");
                withdrawCosmicRunes();
                return 1000;
            }
            if (needsWithdrawSapphirerings()) {
                System.out.println("Withdrawing Sapphire rings");
                withdrawSapphireRing();
                return 1000;
            }
            else {
                getAPIContext().bank().close();
                return 200;
            }
        }

        if (needsToEnchant()) {
            System.out.println("Enchanting rings");
            enchantsRings();
            return 200;
        }
        if (needsToBank()) {
            System.out.println("Bankin");
            openBank();
            return 200;
        }
        if (doesNeedToDeposit()) {
            depositRings();
            return 200;
        }
        return 200;
    }


}