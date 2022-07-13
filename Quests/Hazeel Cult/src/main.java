import com.epicbot.api.os.model.game.WidgetID;
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

@ScriptManifest(name = "Hazeel Cult", gameType = GameType.OS)
public class main extends LoopScript {

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
                if (!getAPIContext().equipment().contains("Mithril scimitar")) {
                    if (!getAPIContext().bank().isOpen()) {
                        BankUtils.goToClosestBank();
                        BankUtils.openBank();
                        return 10000;
                    } else {
                        getAPIContext().bank().withdraw(1, "Mithril scimitar");
                        getAPIContext().inventory().interactItem("Wield", "Mithril scimitar");
                        BankUtils.closeBank();
                    }
                } else {
                    watDo = "Start Quest";
                    return 1000;
                }
                break;

            case "Start Quest":
                if(getAPIContext().quests().isStarted(IQuestAPI.Quest.HAZEEL_CULT)) {
                    watDo = "Talk to Clivet";
                    return 1000;
                }
                if(!Constants.TOP_CERIL_HOUSE.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.TOP_CERIL_HOUSE.getCentralTile());
                    return 1000;
                } else {
                    NPC ceril = getAPIContext().npcs().query().named("Ceril Carnillean").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        getAPIContext().webWalking().walkTo(ceril.getLocation());
                        ceril.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("What's wrong?")) {
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption("Yes, of course, I'd be happy to help.")) {
                            return 1000;
                        }
                    }
                }
                break;

            case "Talk to Clivet":
                if(!Constants.CLIVET_CAVE.contains(getAPIContext().localPlayer().getLocation()) && getAPIContext().npcs().query().named("Clivet").results().nearest() == null) {
                    getAPIContext().webWalking().walkTo(Constants.CLIVET_CAVE.getRandomTile());
                    return 1000;
                } else if(getAPIContext().npcs().query().named("Clivet").results().nearest() == null) {
                    getAPIContext().objects().query().named("Cave entrance").results().nearest().interact("Enter");
                    return 1000;
                }
                if(getAPIContext().npcs().query().named("Clivet").results().nearest() != null) {
                    NPC clivet = getAPIContext().npcs().query().named("Clivet").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        clivet.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().getText().contains("Oh no... not you again.")) {
                            watDo = "Leave Cave";
                            return 1000;
                        } else if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            Time.sleep(5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                            return 1000;
                        } else if(getAPIContext().dialogues().selectOption(0)) {
                            Time.sleep(5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                            return 1000;
                        }
                    }
                }
                break;

            case "Leave Cave":
                if(!Constants.CLIVET_CAVE.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().objects().query().named("Stairs").results().nearest().interact("Climb-up");
                    Time.sleep(5_000, () -> Constants.CLIVET_CAVE.contains(getAPIContext().localPlayer().getLocation()));
                    return 1000;
                } else {
                    watDo = "Turn valve 1";
                    return 1000;
                }

            case "Turn valve 1":
                if(getAPIContext().dialogues().canContinue()) {
                    getAPIContext().dialogues().selectContinue();
                    Time.sleep(3_000, () -> !getAPIContext().dialogues().isDialogueOpen());
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        watDo = "Turn valve 2";
                        return 1000;
                    }
                    return 1000;
                }
                getAPIContext().webWalking().walkTo(new Tile(2562, 3246));
                SceneObject valve1 = getAPIContext().objects().query().named("Sewer valve").results().nearest();
                valve1.interact("Turn-right");
                return 1000;


            case "Turn valve 2":
                if(getAPIContext().dialogues().canContinue()) {
                    getAPIContext().dialogues().selectContinue();
                    Time.sleep(3_000, () -> !getAPIContext().dialogues().isDialogueOpen());
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        watDo = "Turn valve 3";
                        return 1000;
                    }
                    return 1000;
                }
                getAPIContext().webWalking().walkTo(new Tile(2571, 3263));
                SceneObject valve2 = getAPIContext().objects().query().named("Sewer valve").results().nearest();
                valve2.interact("Turn-right");
                return 1000;

            case "Turn valve 3":
                if(getAPIContext().dialogues().canContinue()) {
                    getAPIContext().dialogues().selectContinue();
                    Time.sleep(3_000, () -> !getAPIContext().dialogues().isDialogueOpen());
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        watDo = "Turn valve 4";
                        return 1000;
                    }
                    return 1000;
                }
                getAPIContext().webWalking().walkTo(new Tile(2586, 3244));
                SceneObject valve3 = getAPIContext().objects().query().named("Sewer valve").results().nearest();
                valve3.interact("Turn-left");
                return 1000;

            case "Turn valve 4":
                if(getAPIContext().dialogues().canContinue()) {
                    getAPIContext().dialogues().selectContinue();
                    Time.sleep(3_000, () -> !getAPIContext().dialogues().isDialogueOpen());
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        watDo = "Turn valve 5";
                        return 1000;
                    }
                    return 1000;
                }
                getAPIContext().webWalking().walkTo(new Tile(2597, 3262));
                SceneObject valve4 = getAPIContext().objects().query().named("Sewer valve").results().nearest();
                valve4.interact("Turn-right");
                return 1000;

            case "Turn valve 5":
                if(getAPIContext().dialogues().canContinue()) {
                    getAPIContext().dialogues().selectContinue();
                    Time.sleep(3_000, () -> !getAPIContext().dialogues().isDialogueOpen());
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        watDo = "Talk to Clivet again";
                        return 1000;
                    }
                    return 1000;
                }
                getAPIContext().webWalking().walkTo(new Tile(2610, 3242));
                SceneObject valve5 = getAPIContext().objects().query().named("Sewer valve").results().nearest();
                valve5.interact("Turn-right");
                return 1000;

            case "Talk to Clivet again":
                if(getAPIContext().dialogues().getText().contains("When Lord Hazeel is revived you will")) {
                    getAPIContext().dialogues().selectContinue();
                    watDo = "Board raft";
                    return 1000;
                }
                if(!Constants.CLIVET_CAVE.contains(getAPIContext().localPlayer().getLocation()) && getAPIContext().npcs().query().named("Clivet").results().nearest() == null) {
                    getAPIContext().webWalking().walkTo(Constants.CLIVET_CAVE.getRandomTile());
                    return 1000;
                } else if(getAPIContext().npcs().query().named("Clivet").results().nearest() == null) {
                    getAPIContext().objects().query().named("Cave entrance").results().nearest().interact("Enter");
                    return 1000;
                }
                if(getAPIContext().npcs().query().named("Clivet").results().nearest() != null) {
                    NPC clivet = getAPIContext().npcs().query().named("Clivet").results().nearest();
                    if(!getAPIContext().dialogues().isDialogueOpen()) {
                        clivet.interact("Talk-to");
                        Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    } else {
                        if(getAPIContext().dialogues().canContinue()) {
                            getAPIContext().dialogues().selectContinue();
                            Time.sleep(5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                            return 500;
                        } else if(getAPIContext().dialogues().selectOption(0)) {
                            Time.sleep(5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                            return 1000;
                        }
                    }
                }
                break;

            case "Board raft":
                SceneObject raft = getAPIContext().objects().query().named("Raft").results().nearest();
                raft.interact("Board");
                Time.sleep(5_000, () -> getAPIContext().dialogues().canContinue());
                getAPIContext().dialogues().selectContinue();
                watDo = "Kill Alomone";
                return 1000;

            case "Kill Alomone":
                System.out.println(getAPIContext().npcs().query().named("Alomone").results().nearest());
                if(getAPIContext().inventory().contains("Carnillean armour")) {
                    watDo = "Board raft back";
                    return 1000;
                }
                if(getAPIContext().groundItems().query().named("Carnillean armour").results().nearest() != null) {
                    getAPIContext().camera().turnTo(getAPIContext().groundItems().query().named("Carnillean armour").results().nearest().getLocation());
                    getAPIContext().groundItems().query().named("Carnillean armour").results().nearest().interact("Take");
                    return 1000;
                }
                if(!getAPIContext().localPlayer().isAttacking() && getAPIContext().groundItems().query().named("Carnillean armour").results().nearest() == null) {
                    getAPIContext().camera().turnTo(new Tile(2607, 9680, 0).getLocation());
                    getAPIContext().mouse().click(new Tile(2607, 9680, 0).getCentralPoint());
                    Time.sleep(7_000, () -> getAPIContext().npcs().query().named("Alomone").results().nearest() != null);
                    NPC alomone = getAPIContext().npcs().query().named("Alomone").results().nearest();
                    alomone.interact("Attack");
                    Time.sleep(7_000, () -> getAPIContext().localPlayer().isAttacking());
                    return 1000;
                }
                break;

            case "Board raft back":
                if(!getAPIContext().dialogues().canContinue()) {
                    getAPIContext().camera().turnTo(new Tile(2607, 9684, 0).getLocation());
                    getAPIContext().mouse().click(new Tile(2607, 9684, 0).getCentralPoint());
                    Time.sleep(7_000, () -> getAPIContext().objects().query().named("Raft").results().nearest() != null);
                    raft = getAPIContext().objects().query().named("Raft").results().nearest();
                    raft.interact("Board");
                    Time.sleep(7_000, () -> getAPIContext().dialogues().canContinue());
                } else {
                    getAPIContext().dialogues().selectContinue();
                    watDo = "Leave Cave again";
                    return 1000;
                }
                break;

            case "Leave Cave again":
                if(!Constants.CLIVET_CAVE.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().objects().query().named("Stairs").results().nearest().interact("Climb-up");
                    Time.sleep(5_000, () -> Constants.CLIVET_CAVE.contains(getAPIContext().localPlayer().getLocation()));
                    return 1000;
                } else {
                    watDo = "Go back to house";
                    return 1000;
                }

            case "Go back to house":
                if(!Constants.TOP_CERIL_HOUSE.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.TOP_CERIL_HOUSE.getCentralTile());
                    return 1000;
                } else {
                    watDo = "Talk to butler";
                    return 1000;
                }

            case "Talk to butler":
                if(getAPIContext().dialogues().getText().contains("This is your last warning adventurer")) {
                    watDo = "Talk to Ceril";
                }
                NPC butler = getAPIContext().npcs().query().named("Butler Jones").results().nearest();
                if(!getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().walkTo(butler.getLocation());
                    butler.interact("Talk-to");
                    Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                } else {
                    if (getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        Time.sleep(5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                        return 1000;
                    }
                }
                break;

            case "Talk to Ceril":
                if(getAPIContext().dialogues().getText().contains("Jones smirks at you")) {
                    watDo = "Gather evidence";
                    return 1000;
                }
                NPC ceril = getAPIContext().npcs().query().named("Ceril Carnillean").results().nearest();
                if(!getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().webWalking().walkTo(ceril.getLocation());
                    ceril.interact("Talk-to");
                    Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                } else {
                    if (getAPIContext().dialogues().canContinue()) {
                        getAPIContext().dialogues().selectContinue();
                        Time.sleep(5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                        return 500;
                    }
                }
                break;

            case "Gather evidence":
                if(getAPIContext().quests().isCompleted(IQuestAPI.Quest.HAZEEL_CULT)) {
                    watDo = "Done";
                    return 1000;
                }
                if(getAPIContext().dialogues().isDialogueOpen()) {
                    getAPIContext().dialogues().selectContinue();
                }
                SceneObject cupboard = getAPIContext().objects().query().named("Cupboard").results().nearest();
                if(cupboard.hasAction("Open")) {
                    getAPIContext().webWalking().walkTo(cupboard.getLocation());
                    cupboard.interact("Open");
                    Time.sleep( 5_000, () -> !getAPIContext().objects().query().named("Cupboard").results().nearest().hasAction("Open"));
                    return 1000;
                }
                if(cupboard.hasAction("Search") && !getAPIContext().dialogues().isDialogueOpen()) {
                    cupboard.interact("Search");
                    Time.sleep( 5_000, () -> getAPIContext().dialogues().isDialogueOpen());
                    return 1000;
                }
                break;

            case "Done":
                if(getAPIContext().widgets().get(153).isVisible()) {
                    getAPIContext().widgets().get(153).getChild(16).interact("Close");
                    return 1000;
                }
                getAPIContext().webWalking().setUseTeleports(false);
                BankUtils.goToClosestBank();
                BankUtils.openBank();
                BankUtils.depositInventory();
                BankUtils.closeBank();
                GenUtils.logOut();

        }
        return 1000;
    }

    public static String watDo = "Go back to house";

    @Override
    public boolean onStart(String... strings) {
        return true;
    }
}
