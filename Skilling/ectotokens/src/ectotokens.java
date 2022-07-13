import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.time.Time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ScriptManifest(name = "Ectotokens", gameType = GameType.OS)
public class ectotokens extends LoopScript {

    public static String watDo = "";

    private Area ectofuntus = new Area(new Tile(3665, 3515, 0), new Tile(3655, 3524, 0));
    private Area slime = new Area(new Tile(3685, 9890, 0), new Tile(3681, 9886, 0));
    private Area loader= new Area(new Tile(3656, 3527, 1), new Tile(3663, 3522, 1));

    @Override
    protected int loop() {

        if(getAPIContext().inventory().getCount("Bucket of slime") == 0) {
            if(getAPIContext().inventory().getCount("Bucket") == 0) {
                if(!getAPIContext().bank().isOpen()) {
                    getAPIContext().webWalking().walkToBank();
                    getAPIContext().bank().open();
                    return 600;
                } else {
                    getAPIContext().bank().withdraw(9, "Bucket");
                    getAPIContext().bank().close();
                    return 600;
                }
            } else if(getAPIContext().inventory().getCount("Bucket") == 9) {
                if(slime.contains(getAPIContext().localPlayer().getLocation())) {
                    // collect slime
                    getAPIContext().inventory().interactItem("Use", "Bucket");
                    getAPIContext().camera().turnTo(getAPIContext().objects().query().named("Pool of Slime").results().nearest().getLocation());
                    getAPIContext().objects().query().named("Pool of Slime").results().nearest().interact();
                    Time.sleep(20_000, () -> !getAPIContext().inventory().contains("Bucket"));
                    return 600;
                } else {
                    // go to slime area
                    getAPIContext().webWalking().walkTo(slime.getCentralTile());
                    return 600;
                }
            }
        }

        if(getAPIContext().inventory().getCount("Dragon bonemeal") == 0) {
            System.out.println("yo");
            if(getAPIContext().inventory().getCount("Dragon bones") == 0 || getAPIContext().inventory().getCount("Pot") == 0) {
                if(!getAPIContext().bank().isOpen()) {
                    getAPIContext().webWalking().walkToBank();
                    getAPIContext().bank().open();
                    return 600;
                } else {
                    if(!getAPIContext().inventory().contains("Dragon bones")) {
                        getAPIContext().bank().withdraw(9, "Dragon bones");
                        getAPIContext().bank().close();
                        return 600;
                    }
                    if(!getAPIContext().inventory().contains("Pot")) {
                        getAPIContext().bank().withdraw(9, "Pot");
                        getAPIContext().bank().close();
                        return 600;
                    }

                }
            } else {
                if(loader.contains(getAPIContext().localPlayer().getLocation())) {
                    // grind bones
                    System.out.println("hi");
                    getAPIContext().inventory().interactItem("Use", "Dragon bones");
                    getAPIContext().camera().turnTo(getAPIContext().objects().query().named("Loader").results().nearest().getLocation());
                    getAPIContext().objects().query().named("Loader").results().nearest().interact();
                    Time.sleep(120_000, () -> getAPIContext().inventory().getCount("Dragon bonemeal") == 9);
                    return 600;
                } else {
                    // go to loader
                    getAPIContext().webWalking().walkTo(loader.getCentralTile());
                    return 600;
                }
            }
        }

        if(getAPIContext().inventory().getCount("Bucket of slime", "Dragon bonemeal") >= 2) {
            if(ectofuntus.contains(getAPIContext().localPlayer().getLocation())) {
                // worship ectofuntus
                getAPIContext().objects().query().named("Ectofuntus").results().nearest().interact("Worship");
                return 600;
            } else {
                // go to ectofuntus area
                getAPIContext().inventory().interactItem("Empty", "Ectophial");
                return 600;
            }
        }
        return 600;
    }

    @Override
    public boolean onStart(String... strings) {
        return true;
    }

}
