import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.ItemWidget;
import com.epicbot.api.shared.methods.IMouseAPI;
import com.epicbot.api.shared.methods.ITabsAPI;
import com.epicbot.api.shared.model.Skill;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;

import java.awt.*;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ScriptManifest(name = "Constructor", gameType = GameType.OS)
public class main extends LoopScript {

    private int startXp;
    private int startLvl;

    private long startTime;

    protected void onPaint(Graphics2D g, APIContext ctx) {
        super.onPaint(g, ctx);
        if (getAPIContext().client().isLoggedIn()) {
            PaintFrame pf = new PaintFrame("Builder");
            pf.addLine("Runtime: ", Time.getFormattedRuntime(startTime));
            pf.addLine("Levels: ", getAPIContext().skills().construction().getCurrentLevel() + " +(" + getConstLvls() + ")");
            pf.addLine("Xp/Hr: ", getXpHr() + " +(" + (getAPIContext().skills().construction().getExperience() - startXp) + ")");
            pf.addLine("Xp til level: ", getAPIContext().skills().construction().getExperienceToNextLevel());
            pf.draw(g,0,90,ctx);
        }
    }

    private int getConstLvls() {
        return getAPIContext().skills().get(Skill.Skills.CONSTRUCTION).getCurrentLevel() - startLvl;
    }
    private long getXpHr() {
        long xpGained = getAPIContext().skills().construction().getExperience() - startXp;
        return (int) ((3600000.0 / (System.currentTimeMillis() - startTime)) * xpGained);
    }

    @Override
    protected int loop() {
        System.out.println(Time.getRuntime(startTime));
        if(Time.getRuntime(startTime) > 10_800_000) {
            getAPIContext().game().logout();
        }
        if(getAPIContext().dialogues().isDialogueOpen()) {
            // Butler bringing back supplies
            if(getAPIContext().dialogues().getText().contains("Mistress, I have returned with what") &&
                !getAPIContext().dialogues().getText().contains("As I see thy")) {
                getAPIContext().npcs().query().named("Demon butler").results().nearest().interact("Talk-to");
                return 600; 
            }
            // Asking butler for more planks
            if(getAPIContext().dialogues().hasOption("Something else...") ||
                getAPIContext().dialogues().hasOption("Yes")) {
                getAPIContext().keyboard().sendKey(49);
                return 600;
            }
        }
        // Build menu open
        if(getAPIContext().widgets().get(458).isVisible()) {
            getAPIContext().keyboard().sendKey(50);
            return 1200;
        }
        // Remove Larder
        if(getAPIContext().objects().query().named("Larder").actions("Remove").results().nearest() != null) {
            getAPIContext().objects().query().named("Larder").actions("Remove").results().nearest().interact("Remove");
            return 1200;
        }
        // Talk to butler for more planks
        if(getAPIContext().inventory().getCount("Oak plank") < 8 && getAPIContext().npcs().query().named("Demon butler").results().nearest() != null) {
            getAPIContext().npcs().query().named("Demon butler").results().nearest().interact("Talk-to");
            return 900;
        }
        // Build Larder
        if(getAPIContext().objects().query().named("Larder space").actions("Build").results().nearest() != null
            && getAPIContext().inventory().getCount("Oak plank") > 7) {
            getAPIContext().objects().query().named("Larder space").actions("Build").results().nearest().interact("Build");
            return 1000;
        }
        return 300;
    }

    @Override
    public boolean onStart(String... strings) {
        startLvl = getAPIContext().skills().construction().getCurrentLevel();
        startXp = getAPIContext().skills().construction().getExperience();
        startTime = System.currentTimeMillis();

        return true;
    }
}
