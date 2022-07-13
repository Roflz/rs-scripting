import com.epicbot.api.os.model.game.WidgetID;
import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.methods.IBankAPI;
import com.epicbot.api.shared.methods.ITabsAPI;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Skill;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;
import utils.BankUtils;
import utils.GenUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static utils.BankUtils.*;
import static utils.Constants.*;
import static utils.GenUtils.*;
import static utils.InventoryUtils.*;
import static utils.GEUtils.*;

@ScriptManifest(name = "Hosidius Favor", gameType = GameType.OS)
public class main extends LoopScript {

    private List<String> startingItemsList = Arrays.asList("Saltpetre", "Compost", "Hammer");
    private List<Integer> startingQuantitiesList = Arrays.asList(550, 550, 1);
    public static List<String> shoppingListItems = new ArrayList();
    public static List<Integer> shoppingListQuantities = new ArrayList();
    private int geBuyCounter = 0;
    private int gePriceIncreaseCounter = 1;
    private int ploughCount = 0;

    private int farmStartXp;
    private int farmStartLvl;
    private int timer = 0;

    private long startTime;

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
                    getAPIContext().webWalking().setUseTeleports(false);
                    goToClosestBank();
                    getAPIContext().webWalking().setUseTeleports(true);
                    openBank();
                    depositInventoryIfNotEmpty();
                    depositEquipment();
                    return 1000;
                } else if (doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                    //equip items here
                    watDo = "Go to ploughing field";
                    return 1000;
                }
                if (getAPIContext().bank().isOpen()) {
                    System.out.println("banks open");
                    if (doesBankHaveItemsWithQuantity(startingItemsList, startingQuantitiesList) || doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                        System.out.println("Withdrawing starting items from Bank");
                        depositInventoryIfNotEmpty();
                        getAPIContext().bank().depositEquipment();
                        //Withdraw and equip starting items here
                        getAPIContext().bank().withdraw(1, "Hammer");
                        watDo = "Go to ploughing field";
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
                        buyItemsFromShoppingList(shoppingListItems, shoppingListQuantities, 100);
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

            case "Go to ploughing field":
                if(!new Area(new Tile(1784, 3546, 0), new Tile(1780, 3554, 0)).contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(new Tile(1782, 3550, 0));
                    return 1000;
                } else {
                    watDo = "Position in front of plough";
                    return 1000;
                }

            case "Position in front of plough":
                SceneObject plough = getAPIContext().objects().query().id(27437).results().nearest();
                System.out.println(plough);
                if(new Area(new Tile(plough.getX() - 1, plough.getY() + 1, 0), new Tile(plough.getX() - 3, plough.getY() - 1, 0)).contains(getAPIContext().localPlayer().getLocation()) || new Area(new Tile(plough.getX() + 1, plough.getY() + 1, 0), new Tile(plough.getX() + 3, plough.getY() - 1, 0)).contains(getAPIContext().localPlayer().getLocation())) {
                    watDo = "Plough fields";
                    return 1000;
                }
                if(plough.getX() == 1778) {
                    getAPIContext().webWalking().walkTo(new Tile(plough.getX() + 2, plough.getY(), 0));
                    return 1000;
                } else if(plough.getX() == 1764) {
                    getAPIContext().webWalking().walkTo(new Tile(plough.getX() - 2, plough.getY(), 0));
                    return 1000;
                } else {
                    getAPIContext().webWalking().walkTo(new Tile(plough.getX() - 2, plough.getY(), 0));
                }
                break;

            case "Plough fields":
                if(ploughCount == 100) {
                    watDo = "Go to bank for fertiliser";
                    return 1000;
                }
                SceneObject plough2 = getAPIContext().objects().query().id(27437).results().nearest();
                System.out.println("PloughCount: " + ploughCount);
                    if(plough2.getX() == 1778) {
                        getAPIContext().webWalking().walkTo(new Tile(plough2.getX() + 2, plough2.getY(), 0));
                        getAPIContext().mouse().click(plough2.getLocation().getCentralPoint());
                        while (plough2.getX() != 1764) {
                            try {
                                Thread.sleep(1_000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(timer > Math.floor(Math.random()*(11-7+1)+7)) {
                                timer = 0;
                                break;
                            }
                            System.out.println(timer);
                            timer += 1;
                        }
                        ploughCount += 1;
                        return 1000;
                    } else if(plough2.getX() == 1764) {
                        getAPIContext().webWalking().walkTo(new Tile(plough2.getX() - 2, plough2.getY(), 0));
                        getAPIContext().mouse().click(plough2.getLocation().getCentralPoint());
                        while (plough2.getX() != 1778) {
                            try {
                                Thread.sleep(1_000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (timer > Math.floor(Math.random()*(11-7+1)+7)) {
                                timer = 0;
                                break;
                            }
                            System.out.println(Math.floor(Math.random()*(11-7+1)+7));
                            System.out.println(timer);
                            timer += 1;
                        }
                        ploughCount += 1;
                        return 1000;
                    } else if (getAPIContext().localPlayer().getX() < plough2.getX()){
                        getAPIContext().webWalking().walkTo(new Tile(plough2.getX() - 2, plough2.getY(), 0));
                        getAPIContext().mouse().click(plough2.getLocation().getCentralPoint());
                        while (plough2.getX() != 1778) {
                            try {
                                Thread.sleep(1_000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (timer > Math.floor(Math.random()*(11-7+1)+7)) {
                                timer = 0;
                                break;
                            }
                            System.out.println(Math.floor(Math.random()*(11-7+1)+7));
                            System.out.println(timer);
                            timer += 1;
                        }
                        ploughCount += 1;
                        return 1000;
                    } else if (getAPIContext().localPlayer().getX() > plough2.getX()){
                        getAPIContext().webWalking().walkTo(new Tile(plough2.getX() + 2, plough2.getY(), 0));
                        getAPIContext().mouse().click(plough2.getLocation().getCentralPoint());
                        while (plough2.getX() != 1778) {
                            try {
                                Thread.sleep(1_000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (timer > Math.floor(Math.random()*(11-7+1)+7)) {
                                timer = 0;
                                break;
                            }
                            System.out.println(Math.floor(Math.random()*(11-7+1)+7));
                            System.out.println(timer);
                            timer += 1;
                        }
                        ploughCount += 1;
                        return 1000;
                    }
                    break;

            case "Go to bank for fertiliser":
                BankUtils.goToClosestBank();
                BankUtils.openBank();
                BankUtils.depositInventory();
                watDo = "Make fertiliser";
                break;

            case "Make fertiliser":
                if(!getAPIContext().bank().isOpen() && getAPIContext().inventory().contains("Compost") && getAPIContext().inventory().contains("Saltpetre")) {
                    getAPIContext().inventory().interactItem("Use", "Compost");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getAPIContext().inventory().interactItem("Use", "Saltpetre");
                    return 1200;
                }
                if(!getAPIContext().bank().isOpen()) {
                    if(getAPIContext().inventory().isItemSelected()) {
                        getAPIContext().inventory().deselectItem();
                    }
                    BankUtils.openBank();
                    return 1000;
                }
                if(getAPIContext().bank().isOpen()) {
                    BankUtils.depositInventory();
                    if(getAPIContext().bank().getCount("Sulphurous fertiliser") >= 550) {
                        BankUtils.withdrawAllAsNote("Sulphurous fertiliser");
                        watDo = "Bring fertiliser to Clerk";
                        return 1000;
                    }
                    getAPIContext().bank().withdraw(14, "Compost");
                    getAPIContext().bank().withdraw(14, "Saltpetre");
                    BankUtils.closeBank();
                }
                break;

            case "Bring fertiliser to Clerk":
                if(!(new Area(new Tile(1703, 3529, 0), new Tile(1701, 3526, 0)).contains(getAPIContext().localPlayer().getLocation())) && getAPIContext().npcs().query().named("Clerk").results().nearest() == null) {
                    getAPIContext().webWalking().walkTo(new Tile(1702, 3528, 0));
                    return 1000;
                } else {
                    watDo = "Give fertiliser to Clerk";
                    return 1000;
                }

            case "Give fertiliser to Clerk":
                NPC clerk = getAPIContext().npcs().query().named("Clerk").results().nearest();
                if(getAPIContext().dialogues().canContinue()) {
                    getAPIContext().dialogues().selectContinue();
                    return 1000;
                }
                if(getAPIContext().dialogues().selectOption("Yes")) {
                    return 1000;
                }
                if(getAPIContext().inventory().contains("Sulphurous fertiliser")) {
                    getAPIContext().inventory().interactItem("Use", "Sulphurous fertiliser");
                    getAPIContext().camera().turnTo(clerk.getLocation());
                    clerk.interact();
                    return 1200;
                } else {
                    watDo = "Done";
                    return 1000;
                }

            case "Done":
                GenUtils.logOut();
        }

        return 1000;
    }

    @Override
    public boolean onStart(String... strings) {
        farmStartLvl = getAPIContext().skills().farming().getCurrentLevel();
        farmStartXp = getAPIContext().skills().farming().getExperience();
        startTime = System.currentTimeMillis();
        return true;
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        super.onPaint(g, ctx);
        if (getAPIContext().client().isLoggedIn()) {
            PaintFrame pf = new PaintFrame("Hosidius Favor");
            pf.addLine("Runtime: ", Time.getFormattedRuntime(startTime));
            pf.addLine("Status: ", watDo);
            pf.addLine("Farming: ", getAPIContext().skills().farming().getCurrentLevel() + " +(" + getFarmLvls() + ")");
            pf.addLine("Xp/Hr: ", getXpHr() + " +(" + (getAPIContext().skills().farming().getExperience() - farmStartXp) + ")");
            pf.draw(g,0,90,ctx);
        }
    }

    public static String watDo = "";

    private int getFarmLvls() {
        return getAPIContext().skills().get(Skill.Skills.FARMING).getCurrentLevel() - farmStartLvl;
    }
    private int getXpHr() {
        return (int) ((getAPIContext().skills().farming().getExperience() - farmStartXp) / (System.currentTimeMillis() - startTime));
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
