import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.time.Time;
import utils.BankUtils;
import utils.GenUtils;

@ScriptManifest(name = "Wintertodt", gameType = GameType.OS)
public class main extends LoopScript {

    public static String watDo = "";
    int fireCount = 0;

    @Override
    protected int loop() {
        System.out.println(watDo);
        if(getAPIContext().widgets().get(396).getChild(21).getText().contains(": 0%") && getAPIContext().widgets().get(396).getChild(21).isVisible() &&
                (getAPIContext().widgets().get(396).getChild(3).getText().contains(": 1:") || getAPIContext().widgets().get(396).getChild(3).getText().contains(": 0:5") || getAPIContext().widgets().get(396).getChild(3).getText().contains(": 0:4"))) {
            if(getAPIContext().inventory().contains("Supply crate")) {
                getAPIContext().inventory().interactItem("Open", "Supply crate");
            }
            watDo = "Bank2";
        }

        if(GenUtils.getBottomChatMessage().contains("The cold of the Wintertodt") || GenUtils.getBottomChatMessage().contains("You light the brazier") || GenUtils.getBottomChatMessage().contains("It heals some health") && !getAPIContext().localPlayer().isAnimating() || getAPIContext().dialogues().canContinue()) {
            fireCount = 0;
        }


        switch (watDo) {
            case "":
                getAPIContext().camera().setPitch(98);
                getAPIContext().mouse().move(getAPIContext().game().getCenterSceneTile().getCentralPoint());
                getAPIContext().mouse().scroll(false, 20);
                watDo = "Bank";
                return 500;

            case "Bank":
                if (!getAPIContext().bank().isOpen()) {
                    System.out.println("Going to pick up starting items");
                    BankUtils.goToClosestBank();
                    BankUtils.openBank();
                    return 600;
                } else if(getAPIContext().bank().isOpen() && !(getAPIContext().equipment().containsAll("Pyromancer garb", "Warm gloves", "Bruma torch", "Tome of fire") &&
                        getAPIContext().inventory().contains("Dragon axe") &&
                        getAPIContext().inventory().getCount("Monkfish") == 7)) {
                    BankUtils.depositInventoryIfNotEmpty();
                    BankUtils.depositEquipment();
                    getAPIContext().bank().withdraw(7, "Monkfish");
                    getAPIContext().bank().withdraw(1, "Dragon axe");
                    getAPIContext().bank().withdraw(1, "Pyromancer garb");
                    getAPIContext().bank().withdraw(1, "Warm gloves");
                    getAPIContext().bank().withdraw(1, "Tome of fire");
                    getAPIContext().bank().withdraw(1, "Bruma torch");
                    getAPIContext().inventory().interactItem("Wear",  "Warm gloves");
                    getAPIContext().inventory().interactItem("Wear", "Pyromancer garb");
                    getAPIContext().inventory().interactItem("Wield",  "Bruma torch");
                    getAPIContext().inventory().interactItem("Wield", "Tome of fire");
                    return 1800;
                } else if(getAPIContext().equipment().containsAll("Pyromancer garb", "Warm gloves", "Bruma torch", "Tome of fire") &&
                            getAPIContext().inventory().contains("Dragon axe") &&
                            getAPIContext().inventory().getCount("Monkfish") == 7) {
                    BankUtils.closeBank();
                    watDo = "Start Wintertodt";
                    return 600;
                }
                break;

            case "Bank2":
                if(getAPIContext().localPlayer().getHealthPercent() < 80 && getAPIContext().inventory().contains("Monkfish")) {
                    getAPIContext().inventory().interactItem("Eat", "Monkfish");
                    return 600;
                } else if (!getAPIContext().bank().isOpen()) {
                    BankUtils.goToClosestBank();
                    BankUtils.openBank();
                    return 600;
                } else if(getAPIContext().bank().isOpen() && !(getAPIContext().equipment().containsAll("Pyromancer garb", "Warm gloves", "Bruma torch", "Tome of fire") &&
                        getAPIContext().inventory().contains("Dragon axe") &&
                        getAPIContext().inventory().getCount("Monkfish") == 7)) {
                    BankUtils.depositInventoryIfNotEmpty();
                    getAPIContext().bank().withdraw(7, "Monkfish");
                    getAPIContext().bank().withdraw(1, "Dragon axe");
                    return 600;
                } else if(getAPIContext().equipment().containsAll("Pyromancer garb", "Warm gloves", "Bruma torch", "Tome of fire") &&
                        getAPIContext().inventory().contains("Dragon axe") &&
                        getAPIContext().inventory().getCount("Monkfish") == 7) {
                    BankUtils.closeBank();
                    watDo = "Start Wintertodt";
                    return 600;
                }
                break;

            case "Start Wintertodt":
                if(!(new Area(new Tile(1623, 3994, 0), new Tile(1619, 3998, 0)).contains(getAPIContext().localPlayer().getLocation()))) {
                    getAPIContext().webWalking().walkTo(new Area(new Tile(1622, 3995, 0), new Tile(1620, 3997, 0)).getCentralTile());
                    return 600;
                } else {
                    watDo = "Fire";
                    return 600;
                }

            case "Fire":
                if(getAPIContext().localPlayer().getHealthPercent() < 50) {
                    getAPIContext().inventory().interactItem("Eat", "Monkfish");
                    return 600;
                } else if(getAPIContext().objects().query().named("Brazier").actions("Light").within(new Area(new Tile(1623, 3996, 0), new Tile(1619, 4000, 0))).results().nearest() != null &&
                        (!GenUtils.getBottomChatMessage().contains("There's no need") || !getAPIContext().widgets().get(396).getChild(21).getText().contains(": 0%"))) {
                    getAPIContext().objects().query().named("Brazier").actions("Light").results().nearest().interact("Light");
                    return 1200;
                } else if(getAPIContext().objects().query().named("Brazier").actions("Fix").within(new Area(new Tile(1623, 3996, 0), new Tile(1619, 4000, 0))).results().nearest() != null) {
                    getAPIContext().objects().query().named("Brazier").actions("Fix").results().nearest().interact("Fix");
                    return 1200;
                } else if(getAPIContext().inventory().contains("Bruma root")) {
                    if(fireCount == 0) {
                        getAPIContext().objects().query().named("Burning brazier").actions("Feed").within(new Area(new Tile(1623, 3996, 0), new Tile(1619, 4000, 0))).results().nearest().interact("Feed");
                        Time.sleep(10_000, () -> !(GenUtils.getBottomChatMessage().contains("The cold of the Wintertodt") || GenUtils.getBottomChatMessage().contains("You light the brazier") || GenUtils.getBottomChatMessage().contains("It heals some health")));
                        fireCount += 1;
                        return 1800;
                    }
                } else if(!getAPIContext().inventory().contains("Bruma root") &&
                            !getAPIContext().widgets().get(396).getChild(21).getText().contains(": 0%")){
                    watDo = "Chop";
                    return 600;
                }
                break;

            case "Chop":
                if(getAPIContext().localPlayer().getHealthPercent() < 50) {
                    getAPIContext().inventory().interactItem("Eat", "Monkfish");
                    return 600;
                } else if(!(new Area(new Tile(1624, 3990, 0), new Tile(1620, 3986, 0)).contains(getAPIContext().localPlayer().getLocation()))) {
                    getAPIContext().webWalking().walkTo(new Area(new Tile(1623, 3989, 0), new Tile(1621, 3987, 0)).getCentralTile());
                    return 600;
                } else if(!getAPIContext().inventory().isFull() && !getAPIContext().localPlayer().isAnimating()) {
                    getAPIContext().objects().query().named("Bruma roots").actions("Chop").results().nearest().interact("Chop");
                    return 600;
                } else if(getAPIContext().inventory().isFull()) {
                    getAPIContext().objects().query().named("Burning brazier").actions("Feed").results().nearest().interact("Feed");
                    watDo = "Fire";
                    return 600;
                }
                break;

        }
        return 100;
    }

    @Override
    public boolean onStart(String... strings) {
        return true;
    }
}
