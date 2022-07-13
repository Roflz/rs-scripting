import com.epicbot.api.os.model.game.WidgetID;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.GroundItem;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.methods.IQuestAPI;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.time.Time;

import java.awt.*;
import java.awt.image.AreaAveragingScaleFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ScriptManifest(name = "Demon Slayer", gameType = GameType.OS)
public class main extends LoopScript {

    public static String watDo = "";

    public static List<String> incantation = Arrays.asList();

    private List<String> startingItemsList = Arrays.asList("Bucket of water", "Bones", "Coins");
    private List<Integer> startingQuantitiesList = Arrays.asList(1, 25, 1);
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
                    System.out.println("banks open");
                    if (BankUtils.doesBankHaveItemsWithQuantity(startingItemsList, startingQuantitiesList) || InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                        System.out.println("Withdrawing starting items from Bank");
                        BankUtils.depositInventoryIfNotEmpty();
                        getAPIContext().bank().depositEquipment();
                        getAPIContext().bank().withdraw(1, "Bucket of water");
                        getAPIContext().bank().withdraw(1, "Coins");
//                        getAPIContext().bank().withdraw(1, "Necklace of passage(5)");
//                        getAPIContext().inventory().interactItem("Wear", "Necklace of passage(5)");
//                        getAPIContext().bank().withdraw(1, "Varrock teleport");
//                        if(getAPIContext().skills().attack().getCurrentLevel() >= 20) {
//                            getAPIContext().bank().withdraw(1, "Mithril scimitar");
//                            getAPIContext().inventory().interactItem("Wield", "Mithril scimitar");
//                        } else if(getAPIContext().skills().attack().getCurrentLevel() >= 20){
//                            getAPIContext().bank().withdraw(1, "Steel scimitar");
//                            getAPIContext().inventory().interactItem("Wield", "Steel scimitar");
//                        }
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
                        GEUtils.buyItemsFromShoppingList(shoppingListItems, shoppingListQuantities, 1000);
                        watDo = "Wait for sales from GE";
                        return 500;
                    } else {
                        getAPIContext().grandExchange().collectToBank();
                        getAPIContext().grandExchange().close();
                        watDo = "Bank";
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
                if(getAPIContext().quests().isStarted(IQuestAPI.Quest.DEMON_SLAYER)) {
                    watDo = "Get incantation";
                    return 1000;
                }
                System.out.println(getAPIContext().localPlayer().getLocation());
                if(getAPIContext().dialogues().canContinue()) {
                    getAPIContext().dialogues().selectContinue();
                    return 500;
                }
                if(!Constants.GYPSY.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.GYPSY.getCentralTile());
                    return 1000;
                } else {
                    NPC gypsy = getAPIContext().npcs().query().named("Gypsy Aris").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        gypsy.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 500;
                        } else if(getAPIContext().dialogues().selectOption("Yes.")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("Okay, where is he? I'll kill him for you!")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("So how did Wally kill Delrith?")) {
                            return 1000;
                        }
                    }
                }
                break;

            case "Get incantation":
                if(getAPIContext().dialogues().isDialogueOpen()) {
                    System.out.println(getAPIContext().dialogues().getText());
                    if(getAPIContext().dialogues().getText().contains("Alright, I think I've got it now")) {
//                    if(getAPIContext().dialogues().getText().contains("Have you got that?")) {
                        System.out.println(getAPIContext().dialogues().getText());
                        String incant =  getAPIContext().dialogues().getText().replace("Have", "");
                        incant =  incant.replace("you", "");
                        incant =  incant.replace("got", "");
                        incant =  incant.replace("that?", "");
                        incant = incant.replace("Alright, I think I've  it now, it goes....", "");
                        incant = incant.replace(".", " ");
                        incantation = Arrays.asList(incant.split(" "));
                        System.out.println(incantation);
                        System.out.println(incant);
//                        for (int i = 0; i < incantation.size(); i++){
//                            System.out.println(incantation.get(i));
//                            if(incantation.get(i).equals("") || incantation.get(i) == null) {
//                                System.out.println("hi");
//                                incantation.
//                                incantation.remove(i);
//                            }
//                        }
//                        System.out.println(incantation);
                        watDo = "Speak to Sir Prysin";
                        return 1000;
                    }
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        return 3000;
                    } else if(getAPIContext().dialogues().selectOption("What is the magical incantation?")) {
                        return 1000;
                    }
                }
                break;

            case "Speak to Sir Prysin":
                if(!Constants.SIR_PRYSIN.contains(getAPIContext().localPlayer().getLocation()) && getAPIContext().npcs().query().named("Sir Prysin").results().nearest() == null) {
                    getAPIContext().webWalking().walkTo(Constants.SIR_PRYSIN.getCentralTile());
                    return 1000;
                } else {
                    NPC prysin = getAPIContext().npcs().query().named("Sir Prysin").results().nearest();
                    getAPIContext().webWalking().walkTo(prysin.getLocation());
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        prysin.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                        return 500;
                    } else  {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("Gypsy Aris said I should come and talk to you.")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("I need to find Silverlight.")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("He's back and unfortunately I've got to deal with him.")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("And why is this a problem?")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().hasOption("Can you give me your key?")) {
                            watDo = "Key 1.1";
                            return 1000;
                        }
                    }
                }

            case "Key 1.1":
                if(getAPIContext().inventory().contains(2400)) {
                    watDo = "Key 2.1";
                    return 1000;
                }
                if(!Constants.CAPTAIN_ROVIN.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.CAPTAIN_ROVIN.getCentralTile());
                    if (!(getAPIContext().localPlayer().getLocation() == new Tile(3200, 3500, 1))) {
                        getAPIContext().objects().query().named("Staircase").results().nearest().interact("Climb-up");
                        return 1000;
                    }
                    return 1000;
                } else {
                    NPC rovin = getAPIContext().npcs().query().named("Captain Rovin").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        rovin.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                        return 500;
                    } else  {
//                        3•1•2•2•3•1
                        System.out.println(getAPIContext().dialogues().getText());
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("Yes I know, but this is important.")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("There's a demon who wants to invade this city.")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("Yes, very.")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("It's not them who are going to fight the demon, it's me.")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("Sir Prysin said you would give me the key.")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("Why did he give you one of the keys then?")) {
                            return 1000;
                        } else {
                            getAPIContext().dialogues().selectOption(0);
                        }
                    }
                }
                break;

            case "Key 2.1":
                if(!getAPIContext().inventory().contains("Bucket of water")) {
                    watDo = "Key 2.2";
                    return 1000;
                }
                if(!Constants.CASTLE_KITCHEN.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.CASTLE_KITCHEN.getCentralTile());
                    return 1000;
                } else {
                    SceneObject drain = getAPIContext().objects().query().named("Drain").results().nearest();
                    getAPIContext().camera().turnTo(drain.getLocation());
                    getAPIContext().inventory().selectItem("Bucket of water");
                    drain.interact();
                    Time.sleep( 5_000, () -> !getAPIContext().inventory().contains("Bucket of water"));
                    return 1000;
                }

            case "Key 2.2":
                if(getAPIContext().inventory().contains(2401)) {
                    watDo = "Key 3.1";
                    return 1000;
                }
                if(!Constants.SEWER_DRAIN.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.SEWER_DRAIN.getCentralTile());
                } else {
                    Point key = new Tile(3225, 9897, 0).getCentralPoint();
                    getAPIContext().mouse().click(key);
                    Time.sleep( 5_000, () -> getAPIContext().inventory().contains(2401));
                    return 1000;
                }
                break;

            case "Key 3.1":
                if(getAPIContext().inventory().contains(2399)) {
                    watDo = "Give keys to Sir Prysin";
                    return 1000;
                }
                if(getAPIContext().inventory().getCount("Bones") < 25 && !getAPIContext().inventory().contains(2399)) {
                    if(!getAPIContext().bank().isOpen()) {
                        getAPIContext().webWalking().setUseTeleports(false);
                        BankUtils.goToClosestBank();
                        getAPIContext().webWalking().setUseTeleports(true);
                        BankUtils.openBank();
                        return 500;
                    }
                    if(getAPIContext().bank().isOpen()) {
                        getAPIContext().bank().deposit(1, "Bucket");
                        getAPIContext().bank().withdraw(25, "Bones");
                        BankUtils.closeBank();
                        return 500;
                    }
                }
                if(!Constants.TRAIBORN.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.TRAIBORN.getCentralTile());
                } else {
                    NPC traiborn = getAPIContext().npcs().query().named("Traiborn").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        traiborn.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                        return 500;
                    } else  {
//                        3•3 2
                        System.out.println(getAPIContext().dialogues().getText());
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("I need to get a key given to you by Sir Prysin.")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("Well, have you got any keys knocking around?")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("I'll get the bones for you.")) {
                            return 1000;
                        }
                    }
                }
                break;

            case "Give keys to Sir Prysin":
                if(getAPIContext().inventory().contains(2402) || getAPIContext().equipment().contains(2402)) {
                    watDo = "Go to Delrith";
                    return 1000;
                }
                if(!Constants.SIR_PRYSIN.contains(getAPIContext().localPlayer().getLocation()) && getAPIContext().npcs().query().named("Sir Prysin").results().nearest() == null) {
                    getAPIContext().webWalking().walkTo(Constants.SIR_PRYSIN.getCentralTile());
                    if (!(getAPIContext().localPlayer().getLocation() == new Tile(3104, 3161, 1))) {
                        getAPIContext().objects().query().named("Staircase").results().nearest().interact("Climb-down");
                        return 1000;
                    }
                    return 1000;
                } else {
                    NPC prysin = getAPIContext().npcs().query().named("Sir Prysin").results().nearest();
                    getAPIContext().webWalking().walkTo(prysin.getLocation());
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        prysin.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                        return 500;
                    } else  {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 500;
                        }
                    }
                }

            case "Go to Delrith":
                if(!getAPIContext().equipment().contains("Silverlight")) {
                    getAPIContext().inventory().interactItem("Wield", "Silverlight");
                    return 500;
                }
                if(getAPIContext().inventory().getCount("Trout") < 25) {
                    if(!getAPIContext().bank().isOpen()) {
                        BankUtils.goToClosestBank();
                        BankUtils.openBank();
                        return 500;
                    }
                    if(getAPIContext().bank().isOpen()) {
                        BankUtils.depositInventoryIfNotEmpty();
                        getAPIContext().bank().withdraw(25, "Trout");
                        BankUtils.closeBank();
                        return 500;
                    }
                }
                if(!getAPIContext().combat().isAutoRetaliateOn()) {
                    getAPIContext().combat().toggleAutoRetaliate(true);
                    return 1000;
                }
                if(!Constants.DELRITH.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.DELRITH.getCentralTile());
                    watDo = "Start fight";
                    return 1000;
                }
                break;

            case "Start fight":
                if(getAPIContext().dialogues().isDialogueOpen()) {
                    if (getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        return 500;
                    }
                } else {
                    watDo = "Kill Delrith";
                    return 1000;
                }
                break;

            case "Kill Delrith":
                if(getAPIContext().dialogues().getText().contains("back into the dark dimension from which he came.")) {
                    watDo = "Done";
                    return 1000;
                }
                if(getAPIContext().dialogues().isDialogueOpen()) {
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        return 500;
                    } else
                        for(String line : incantation) {
                            System.out.println(line);
                            if(getAPIContext().dialogues().hasOption(line)) {
                                getAPIContext().dialogues().selectOption(line);
                                try {
                                    Thread.sleep(5_000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        return 1000;
                } else {
                    NPC delrith = getAPIContext().npcs().query().named("Delrith").results().nearest();
                    if(getAPIContext().localPlayer().getHealthPercent() < 50) {
                        getAPIContext().inventory().interactItem("Eat", "Trout");
                        return 1000;
                    }
                    if(!getAPIContext().localPlayer().isInCombat()) {
                        getAPIContext().camera().turnTo(delrith.getLocation());
                        delrith.interact("Attack");
                        return 1000;
                    }
                }
                break;

            case "Done":
                if(getAPIContext().dialogues().canContinue()) {
                    getAPIContext().dialogues().selectContinue();
                    return 1000;
                }
                if(getAPIContext().widgets().get(153).isVisible()) {
                    getAPIContext().widgets().get(153).getChild(16).interact("Close");
                    return 1000;
                }
                BankUtils.goToClosestBank();
                BankUtils.openBank();
                BankUtils.depositInventory();
                BankUtils.closeBank();
                GenUtils.logOut();
        }
        return 1000;
    }

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
