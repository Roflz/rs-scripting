import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.methods.IQuestAPI;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.time.Time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ScriptManifest(name = "Priest in Peril", gameType = GameType.OS)
public class main extends LoopScript {

    public static String watDo = "";

    private List<String> startingItemsList = Arrays.asList("Pure essence");
    private List<Integer> startingQuantitiesList = Arrays.asList(50);
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
                if(!getAPIContext().bank().isOpen()) {
                    System.out.println("Going to pick up starting items");
                    BankUtils.goToClosestBank();
                    BankUtils.openBank();
                    BankUtils.depositInventoryIfNotEmpty();
                    return 500;
                }
                if(getAPIContext().bank().isOpen()) {
                    System.out.println("banks open");
                    if(BankUtils.doesBankHaveItemsWithQuantity(startingItemsList, startingQuantitiesList) || InventoryUtils.doesInventoryHaveItemsWithQuantities(startingItemsList, startingQuantitiesList)) {
                        System.out.println("Withdrawing starting items from Bank");
                        BankUtils.depositInventoryIfNotEmpty();
                        getAPIContext().bank().depositEquipment();
                        if(getAPIContext().skills().attack().getCurrentLevel() >= 20) {
                            getAPIContext().bank().withdraw(1, "Mithril scimitar");
                            getAPIContext().inventory().interactItem("Wield", "Mithril scimitar");
                        } else {
                            getAPIContext().bank().withdraw(1, "Steel scimitar");
                            getAPIContext().inventory().interactItem("Wield", "Steel scimitar");
                        }
                        getAPIContext().bank().withdraw(1, "Bucket");
                        getAPIContext().bank().withdraw(3, "Varrock teleport");
                        getAPIContext().bank().withdraw(20, "Trout");

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
                        GEUtils.buyItemsFromShoppingList(shoppingListItems, shoppingListQuantities);
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
                if(getAPIContext().quests().isStarted(IQuestAPI.Quest.PRIEST_IN_PERIL)) {
                    watDo = "Go to Monk Temple";
                    return 1000;
                }
                if(!Constants.KING_ROALD.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.KING_ROALD.getCentralTile());
                    return 1000;
                } else {
                    NPC kingRoald = getAPIContext().npcs().query().named("King Roald").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        kingRoald.interact("Talk-to");
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                        } else if(getAPIContext().dialogues().selectOption("I'm looking for a quest!")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("Yes.")) {
                            return 1000;
                        }
                    }
                }
                break;
            case "Go to Monk Temple":
                if(!Constants.TEMPLE_DOOR.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().walkTo(Constants.TEMPLE_DOOR.getCentralTile());
                    SceneObject door = getAPIContext().objects().query().named("Large door").results().nearest();
                    door.interact("Open");
                    return 1000;
                }
                if(getAPIContext().dialogues().isDialogueOpen()) {
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                    } else {
                        getAPIContext().dialogues().selectOption(0);
                    }
                } else {
                    watDo = "Go to Temple Guardian";
                    return 1000;
                }
                break;
            case "Go to Temple Guardian":
                if(getAPIContext().npcs().query().named("Temple Guardian").results().nearest() != null){
                    watDo = "Fight Temple Guardian";
                    return 1000;
                }
                if(!Constants.TRAPDOOR.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.TRAPDOOR.getCentralTile());
                    return 1000;
                }
                if(getAPIContext().dialogues().isDialogueOpen()) {
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        return 1000;
                    } else {
                        getAPIContext().dialogues().selectOption("Yes.");
                        Time.sleep(5_000, () -> getAPIContext().npcs().get(7620) != null);
                        return 1000;
                    }
                }
                SceneObject trapDoor = getAPIContext().objects().query().named("Trapdoor").results().nearest();
                if(trapDoor.hasAction("Open")) {
                    trapDoor.interact("Open");
                    Time.sleep(5_000, () -> trapDoor.hasAction("Climb-down"));
                    trapDoor.interact("Climb-down");
                    return 1000;
                } else if(trapDoor.hasAction("Climb-down")) {
                    trapDoor.interact("Climb-down");
                    return 1000;
                }
                break;
            case "Fight Temple Guardian":
                if(getAPIContext().npcs().query().named("Temple Guardian").results().nearest() != null) {
                    NPC templeGuardian = getAPIContext().npcs().query().named("Temple Guardian").results().nearest();
                    if(!getAPIContext().localPlayer().isAttacking()) {
                        templeGuardian.interact("Attack");
                        return 1000;
                    } else {
                        if(getAPIContext().localPlayer().getHealthPercent() < 50) {
                            getAPIContext().inventory().interactItem("Eat", "Trout");
                            return 1000;
                        }
                    }
                } else {
                    System.out.println("Killed the temple guardian");
                    watDo = "Temple Guardian is dead";
                    return 1000;
                }
                break;
            case "Temple Guardian is dead":
                if(getAPIContext().npcs().query().named("Temple Guardian").results().nearest() == null && getAPIContext().objects().query().named("Monument").results().nearest() != null) {
                    SceneObject ladder = getAPIContext().objects().query().named("Ladder").results().nearest();
                    ladder.interact("Climb-up");
                    Time.sleep(5_000, () -> Constants.TRAPDOOR.contains(getAPIContext().localPlayer().getLocation()));
                    getAPIContext().webWalking().walkTo(Constants.TEMPLE_DOOR.getCentralTile());
                    SceneObject door = getAPIContext().objects().query().named("Large door").results().nearest();
                    door.interact("Open");
                    return 1000;
                }
                if(Constants.TEMPLE_DOOR.contains(getAPIContext().localPlayer().getLocation())) {
                    if(getAPIContext().dialogues().isDialogueOpen()) {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        }
                    } else {
                        watDo = "Back to King Roald";
                        return 1000;
                    }
                    return 1000;
                }
                if(!Constants.TEMPLE_DOOR.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.TEMPLE_DOOR.getCentralTile());
                    return 1000;
                } else  {
                    SceneObject door = getAPIContext().objects().query().named("Large door").results().nearest();
                    door.interact("Open");
                    return 1000;
                }
            case "Back to King Roald":
                if(!Constants.KING_ROALD.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.KING_ROALD.getCentralTile());
                    return 1000;
                }
                if(Constants.KING_ROALD.contains(getAPIContext().localPlayer().getLocation())) {
                    NPC kingRoald = getAPIContext().npcs().query().named("King Roald").results().nearest();
                    if(getAPIContext().dialogues().isDialogueOpen()) {
                        if(getAPIContext().dialogues().getText().contains("NOW GO")) {
                            watDo = "The Vampyre";
                            return 1000;
                        }
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        }
                        return 1000;
                    } else {
                        kingRoald.interact("Talk-to");
                        return 1000;
                    }
                }
            case "The Vampyre":
                if(getAPIContext().groundItems().query().named("Golden key").results().nearest() != null) {
                    getAPIContext().groundItems().query().named("Golden key").results().nearest().interact("Take");
                    System.out.println("Killed the monk");
                    watDo = "Monk is dead";
                    return 1000;
                }
                if(!getAPIContext().inventory().contains("Bucket")) {
                    if(!Constants.UPSTAIRS_KITCHEN.contains(getAPIContext().localPlayer().getLocation())) {
                        getAPIContext().webWalking().walkTo(Constants.UPSTAIRS_KITCHEN.getRandomTile());
                        return 500;
                    } else if(getAPIContext().groundItems().query().named("Bucket").results().nearest() != null) {
                        getAPIContext().groundItems().query().named("Bucket").results().nearest().interact("Take");
                    }
                } else if(!getAPIContext().bank().isOpen() && getAPIContext().inventory().getCount("Trout") < 20 && !getAPIContext().localPlayer().isAttacking()) {
                    System.out.println("Going to pick up trout");
                    BankUtils.goToClosestBank();
                    BankUtils.openBank();
                    getAPIContext().bank().depositAll("Trout");
                    getAPIContext().bank().withdraw(20, "Trout");
                    BankUtils.closeBank();
                    return 1000;
                }
                if(getAPIContext().inventory().contains("Bucket") && getAPIContext().inventory().getCount("Trout") <= 20) {
                    System.out.println(getAPIContext().objects().query().named("Study desk").results().nearest());
                    System.out.println(getAPIContext().npcs().query().named("Monk of Zamorak").results().nearest());
                    if(getAPIContext().objects().query().named("Study desk").results().nearest() == null && getAPIContext().npcs().query().named("Monk of Zamorak").results().nearest() == null) {
                        getAPIContext().webWalking().walkTo(Constants.TEMPLE_DOOR.getCentralTile());
                        SceneObject door = getAPIContext().objects().query().named("Large door").results().nearest();
                        door.interact("Open");
                        return 1000;
                    }
                    if(Constants.TEMPLE.contains(getAPIContext().localPlayer().getLocation())) {
                        SceneObject stairs1 = getAPIContext().objects().query().named("Staircase").results().nearest();
                        stairs1.interact("Climb-up");
                        Time.sleep( 5_000, () -> getAPIContext().objects().query().named("Study desk").results().nearest() != null);
                        return 1000;
                    }
                    if(getAPIContext().objects().query().named("Study desk").results().nearest() != null) {
                        NPC monk = getAPIContext().npcs().query().named("Monk of Zamorak").id(3486).results().nearest();
                        if(!getAPIContext().localPlayer().isAttacking()) {
                            monk.interact("Attack");
                            return 1000;
                        } else if(monk.isAttacking()){
                            if(getAPIContext().localPlayer().getHealthPercent() < 50) {
                                getAPIContext().inventory().interactItem("Eat", "Trout");
                                return 1000;
                            }
                        }
                    }
                }
            case "Monk is dead":
                if(Constants.UPSTAIRS_TEMPLE.contains(getAPIContext().localPlayer().getLocation())) {
                    SceneObject stairs = getAPIContext().objects().query().named("Ladder").results().nearest();
                    stairs.interact("Climb-up");
                    Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() == 2);
                    return 1000;
                }
                if(getAPIContext().npcs().query().named("Drezel").results().nearest() != null && !getAPIContext().dialogues().isDialogueOpen()) {
                    NPC drezel = getAPIContext().npcs().query().named("Drezel").results().nearest();
                    drezel.interact("Talk-to");
                    Time.sleep(6_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    return 1000;
                }
                if(getAPIContext().dialogues().isDialogueOpen()) {
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        return 1000;
                    }
                    if(getAPIContext().dialogues().hasOption("So, what now?")) {
                        getAPIContext().dialogues().selectOption(1);
                        return 1000;
                    }
                    if(getAPIContext().dialogues().hasOption("Yes, of course.")) {
                        getAPIContext().dialogues().selectOption("Yes, of course.");
                        watDo = "Go back to underground temple";
                        return 1000;
                    }
                    return 1000;
                }
            case "Go back to underground temple":
                if(getAPIContext().objects().query().named("Ladder").actions("Climb-down").results().nearest() != null) {
                    getAPIContext().objects().query().named("Ladder").actions("Climb-down").results().nearest().interact("Climb-down");
                    Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() == 1);
                    return 1000;
                }
                if(getAPIContext().objects().query().named("Staircase").actions("Climb-down").results().nearest() != null) {
                    getAPIContext().objects().query().named("Staircase").actions("Climb-down").results().nearest().interact("Climb-down");
                    Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() == 0);
                    return 1000;
                }
                if(Constants.TEMPLE.contains(getAPIContext().localPlayer().getLocation()) || Constants.TRAPDOOR.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.TRAPDOOR.getCentralTile());
                    SceneObject trapDoor2 = getAPIContext().objects().query().named("Trapdoor").results().nearest();
                    if(trapDoor2.hasAction("Open")) {
                        trapDoor2.interact("Open");
                        Time.sleep(5_000, () -> trapDoor2.hasAction("Climb-down"));
                        trapDoor2.interact("Climb-down");
                        return 1000;
                    } else if(trapDoor2.hasAction("Climb-down")) {
                        trapDoor2.interact("Climb-down");
                        return 1000;
                    }
                    return 1000;
                }
                if(getAPIContext().localPlayer().getY() > 9_000) {
                    watDo = "Pass gate";
                    return 1000;
                }
                break;
            case "Pass gate":
                SceneObject gate = getAPIContext().objects().query().named("Gate").results().nearest();
                getAPIContext().camera().turnTo(gate);
                gate.interact("Open");
                Time.sleep(5_000, () -> !gate.hasAction("Open"));
                watDo = "Study Monuments";
                return 1000;
            case "Study Monuments":
                List<SceneObject> monuments = getAPIContext().objects().query().named("Monument").results().nearestList();
                SceneObject well = getAPIContext().objects().query().named("Well").results().nearest();
                if(!getAPIContext().inventory().contains("Iron key")) {
                    for(SceneObject monument : monuments) {
                        monument.interact("Study");
                        Time.sleep(10_000, () -> getAPIContext().widgets().get(272).isVisible());
                        String monumentText = getAPIContext().widgets().get(272).getChild(7).getText();
                        if(monumentText.contains("Saradomin is thekey")) {
                            getAPIContext().widgets().closeInterface();
                            Time.sleep(5_000, () -> !getAPIContext().widgets().isInterfaceOpen());
                            getAPIContext().inventory().selectItem("Golden key");
                            monument.interact();
                            Time.sleep(5_000, () -> getAPIContext().dialogues().canContinue());
                            getAPIContext().dialogues().selectContinue();
                            return 2000;
                        } else {
                            getAPIContext().widgets().closeInterface();
                        }
                    }
                }
                if(getAPIContext().inventory().contains("Iron key")) {
                    getAPIContext().inventory().selectItem("Bucket");
                    well.interact();
                    Time.sleep(7_000, () -> getAPIContext().inventory().contains("Murky water"));
                    watDo = "Pass gate again";
                }
            case "Pass gate again":
                getAPIContext().webWalking().walkTo(new Tile(3405, 9894, 0));
                SceneObject gate2 = getAPIContext().objects().query().named("Gate").results().nearest();
                getAPIContext().camera().turnTo(gate2);
                gate2.interact("Open");
                watDo = "Back to Drezel";
                return 1000;
            case "Back to Drezel":
                if(!getAPIContext().inventory().contains("Iron key")) {
                    if(!getAPIContext().dialogues().isDialogueOpen() && !getAPIContext().inventory().contains("Blessed water")) {
                        getAPIContext().npcs().query().named("Drezel").results().nearest().interact("Talk-to");
                        return 1000;
                    }
                    if(getAPIContext().dialogues().isDialogueOpen()) {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        }
                    }
                    if(getAPIContext().inventory().contains("Blessed water")) {
                        getAPIContext().inventory().selectItem("Blessed water");
                        getAPIContext().objects().query().named("Coffin").results().nearest().interact();
                        Time.sleep(5_000, () -> getAPIContext().dialogues().canContinue());
                        getAPIContext().dialogues().selectContinue();
                        return 1000;
                    }
                    if(getAPIContext().inventory().contains("Bucket")) {
                        if(!getAPIContext().dialogues().isDialogueOpen()) {
                            getAPIContext().npcs().query().named("Drezel").results().nearest().interact("Talk-to");
                            return 1000;
                        } else if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            Time.sleep(2_000);
                            if(!getAPIContext().dialogues().isDialogueOpen()) {
                                watDo = "Back to Monuments";
                            }
                            return 1000;
                        }

                    }
                }
                if(getAPIContext().objects().query().named("Monument").results().nearest() != null) {
                    SceneObject ladder = getAPIContext().objects().query().named("Ladder").results().nearest();
                    ladder.interact("Climb-up");
                    Time.sleep(5_000, () -> Constants.TRAPDOOR.contains(getAPIContext().localPlayer().getLocation()));
                    getAPIContext().webWalking().walkTo(Constants.TEMPLE_DOOR.getCentralTile());
                    return 1000;
                }
                if(Constants.TEMPLE_DOOR.contains(getAPIContext().localPlayer().getLocation())) {
                    SceneObject door = getAPIContext().objects().query().named("Large door").results().nearest();
                    door.interact("Open");
                    return 1000;
                }
                if(Constants.TEMPLE.contains(getAPIContext().localPlayer().getLocation())) {
                    SceneObject stairs = getAPIContext().objects().query().named("Staircase").results().nearest();
                    stairs.interact("Climb-up");
                    Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() == 1);
                    return 1000;
                }
                if(Constants.UPSTAIRS_TEMPLE.contains(getAPIContext().localPlayer().getLocation())) {
                    SceneObject stairs = getAPIContext().objects().query().named("Ladder").results().nearest();
                    stairs.interact("Climb-up");
                    Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() == 2);
                    return 1000;
                }
                if(getAPIContext().npcs().query().named("Drezel").results().nearest() != null) {
                    SceneObject cellDoor = getAPIContext().objects().query().named("Cell door").results().nearest();
                    getAPIContext().inventory().selectItem("Iron key");
                    cellDoor.interact();
                    Time.sleep(6_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    return 1000;
                }
                if(getAPIContext().dialogues().isDialogueOpen()) {
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        return 1000;
                    }
                }
            case "Back to Monuments":
                if(getAPIContext().objects().query().named("Ladder").actions("Climb-down").results().nearest() != null) {
                    getAPIContext().objects().query().named("Ladder").actions("Climb-down").results().nearest().interact("Climb-down");
                    Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() == 1);
                    return 1000;
                }
                if(getAPIContext().objects().query().named("Staircase").actions("Climb-down").results().nearest() != null) {
                    getAPIContext().objects().query().named("Staircase").actions("Climb-down").results().nearest().interact("Climb-down");
                    Time.sleep(5_000, () -> getAPIContext().localPlayer().getY() == 0);
                    return 1000;
                }
                if(Constants.TEMPLE.contains(getAPIContext().localPlayer().getLocation()) || Constants.TRAPDOOR.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.TRAPDOOR.getCentralTile());
                    SceneObject trapDoor2 = getAPIContext().objects().query().named("Trapdoor").results().nearest();
                    if(trapDoor2.hasAction("Open")) {
                        trapDoor2.interact("Open");
                        Time.sleep(5_000, () -> getAPIContext().objects().query().named("Trapdoor").results().nearest().hasAction("Climb-down"));
                        trapDoor2.interact("Climb-down");
                        Time.sleep(3_000, () -> getAPIContext().localPlayer().getY() > 9_000);
                        return 1000;
                    } else if(trapDoor2.hasAction("Climb-down")) {
                        trapDoor2.interact("Climb-down");
                        Time.sleep(3_000, () -> getAPIContext().localPlayer().getY() > 9_000);
                        return 1000;
                    }
                    return 1000;
                }
                if(getAPIContext().localPlayer().getY() > 9_000) {
                    if(!Constants.DREZEL.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().dialogues().isDialogueOpen()) {
                        getAPIContext().webWalking().walkTo(new Tile(3440, 9897, 0));
                        getAPIContext().npcs().query().named("Drezel").results().nearest().interact("Talk-to");
                        return 1000;
                    }
                    if(getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        return 1000;
                    } else {
                        watDo = "Bring Essence to Drezel";
                        return 1000;
                    }
                }
            case "Bring Essence to Drezel":
                if(getAPIContext().quests().isCompleted(IQuestAPI.Quest.PRIEST_IN_PERIL)) {
                    watDo = "Done";
                    return 1000;
                }
                if(Constants.DREZEL.contains(getAPIContext().localPlayer().getLocation()) && !getAPIContext().inventory().contains("Pure essence")) {
                    getAPIContext().webWalking().walkTo(new Tile(3405, 9906, 0));
                    SceneObject ladder = getAPIContext().objects().query().named("Ladder").results().nearest();
                    ladder.interact("Climb-up");
                    Time.sleep(5_000, () -> Constants.TRAPDOOR.contains(getAPIContext().localPlayer().getLocation()));
                    BankUtils.goToClosestBank();
                    return 1000;
                }
                if(getAPIContext().bank().isVisible() && !getAPIContext().inventory().contains("Pure essence")) {
                    BankUtils.openBank();
                    BankUtils.depositInventory();
                    getAPIContext().bank().withdraw(25, "Pure essence");
                    return 1000;
                }
                if(getAPIContext().bank().isVisible() && getAPIContext().inventory().contains("Pure essence")) {
                    getAPIContext().webWalking().walkTo(Constants.TRAPDOOR.getCentralTile());
                    return 1000;
                }
                if(Constants.TRAPDOOR.contains(getAPIContext().localPlayer().getLocation()) && getAPIContext().inventory().contains("Pure essence")) {
                    SceneObject trapDoor2 = getAPIContext().objects().query().named("Trapdoor").results().nearest();
                    if(trapDoor2.hasAction("Open")) {
                        trapDoor2.interact("Open");
                        Time.sleep(5_000, () -> getAPIContext().objects().query().named("Trapdoor").results().nearest().hasAction("Climb-down"));
                        trapDoor2.interact("Climb-down");
                        Time.sleep(3_000, () -> getAPIContext().localPlayer().getY() > 9_000);
                        return 1000;
                    } else if(trapDoor2.hasAction("Climb-down")) {
                        trapDoor2.interact("Climb-down");
                        Time.sleep(3_000, () -> getAPIContext().localPlayer().getY() > 9_000);
                        return 1000;
                    }
                    return 1000;
                }
                if(getAPIContext().localPlayer().getY() > 9000 && getAPIContext().inventory().contains("Pure essence")) {
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        getAPIContext().webWalking().walkTo(new Tile(3440, 9897, 0));
                        getAPIContext().npcs().query().named("Drezel").results().nearest().interact("Talk-to");
                        return 1000;
                    } else {
                        getAPIContext().dialogues().selectContinue();
                        return 1000;
                    }
                }
            case "Done":
                GenUtils.logOut();
        }
        return 1000;
    }

    @Override
    public boolean onStart(String... strings) {
        return true;
    }
}
