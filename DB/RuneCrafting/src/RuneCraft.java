import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.NPC;

import javax.swing.text.Position;
import java.awt.*;

@ScriptManifest(
        author = "PASH",
        description = "Yew Trees with Antiban",
        category = Category.WOODCUTTING,
        version = 1.0,
        name = "[PASH] YewCutter + Antiban"
)

public class RuneCraft extends AbstractScript {
    Area ruinsArea = new Area(3308, 3262, 3315, 3251, 0);
    Area altarArea = new Area(2583, 4841, 2587, 4835, 0);

    @Override
    public void onStart(){
        log("hi");
        Skills.open();
        sleep(Calculations.random(1000, 2000));
        Skills.hoverSkill(Skill.RUNECRAFTING);
        log("ANTIBAN: hovered over skill.");
        log("swag");
        sleep(Calculations.random(1000, 4000));
        if (!Tabs.isOpen(org.dreambot.api.methods.tabs.Tab.INVENTORY)) {
            Tabs.openWithMouse(org.dreambot.api.methods.tabs.Tab.INVENTORY);
            sleep(Calculations.random(1000, 1500));
            log("ANTIBAN: Set tab to inventory.");
        }
    }


    @Override
    public int onLoop(){
        if(Calculations.random(0,20) == 14) {
            if(!Walking.isRunEnabled() && Walking.getRunEnergy() > 30) {
                Walking.toggleRun();
            }
            // ANTIBAN
            if(Calculations.random(0,10) == 7 && Tabs.isOpen(org.dreambot.api.methods.tabs.Tab.INVENTORY)) {
                Skills.open();
                sleep(Calculations.random(1000, 2000));
                Skills.hoverSkill(Skill.RUNECRAFTING);
                log("ANTIBAN: hovering over skill.");
                sleep(Calculations.random(1000, 4000));
                if (!Tabs.isOpen(org.dreambot.api.methods.tabs.Tab.INVENTORY)) {
                    Tabs.openWithMouse(org.dreambot.api.methods.tabs.Tab.INVENTORY);
                    sleep(Calculations.random(1000, 1500));
                    log("ANTIBAN: Set tab to inventory.");
                }
            }
        }
        GameObject ruins = GameObjects.closest(gameObject -> gameObject != null && gameObject.getName().equals("Mysterious ruins"));
        GameObject altar = GameObjects.closest(gameObject -> gameObject.getName().equals("Altar"));
        GameObject portal = GameObjects.closest(gameObject -> gameObject != null && gameObject.getName().equals("Portal"));
        if(Inventory.isFull()) {
            if(altar != null) {
                Walking.walk(altarArea.getRandomTile());
                sleep(Calculations.random(1500, 3000));
                altar.interact("Craft-rune");
                Mouse.move(new Point(Calculations.random(0, 765), Calculations.random(0, 503))); //antiban
                if (sleepUntil(() -> !Inventory.isFull(), Calculations.random(4500, 6500)));
                log("hi");
            }
            if(ruinsArea.contains(getLocalPlayer())) {
                if (Calculations.random(0, 20) == 7) {//antiban
                    Camera.rotateToEntity(GameObjects.all(gameObject -> gameObject != null && gameObject.getName().equals("Mysterious ruins")).get(1));
                    log("ANTIBAN: turned camera to another entity");

                    log("ANTIBAN: running to another area");
                    Area nearbyArea = new Area (3203 + Calculations.random(-3, 3), 3506 + (Calculations.random(-3, 4)), 3223 + (Calculations.random(-4, 3)), 3498 + (Calculations.random(-4, 4)), 0);
                    if(Walking.walk(nearbyArea.getRandomTile())) {
                        sleep(Calculations.random(3000, 10000));
                    }
                } else if(ruins != null && ruins.interact("Enter")) {
                    Mouse.move(new Point(Calculations.random(0, 765), Calculations.random(0, 503))); //antiban
//                    int countLog = Inventory.count("Logs");
//                    sleepUntil(() -> Inventory.count("Logs") > countLog, 12000);
                }

            } else {

                if(Walking.walk(ruinsArea.getRandomTile())) {
                    sleep(Calculations.random(1500, 3000));
                }
            }

        }
        if(!Inventory.isFull()) {
            if(altar != null) {
                if(portal != null && portal.interact("Use")) {
                    Mouse.move(new Point(Calculations.random(0, 765), Calculations.random(0, 503))); //antiban
                    if (sleepUntil(() -> !portal.exists(), Calculations.random(4500, 6500)));
                }
            }
            if(Bank.openClosest()) {
                log("got to bank area");
                Mouse.move(new Point(Calculations.random(0, 765), Calculations.random(0, 503))); //antiban
                if (sleepUntil(() -> Inventory.isFull(), Calculations.random(4500, 6500)));
                log("ANTIBAN: moving mouse to random area");
                //NPC banker = getNpcs().closest(npc -> npc != null && npc.hasAction("Bank"));
                if(Bank.openClosest()) {
                    log("Got into bank");
                    if(sleepUntil(Bank::isOpen,  Calculations.random(7000, 9000))) {
                        if(Bank.depositAllExcept(item -> item != null && item.getName().contains("Rune axe"))) {
                            log("Deposited");
                            if(sleepUntil(() -> !Inventory.isFull(), Calculations.random(5500, 6500))) {
                                if (Bank.withdrawAll("Pure essence")) {
                                    if (sleepUntil(() -> Inventory.isFull(), Calculations.random(7500, 8500)));
                                }
                                if (Bank.close()) {
                                    sleepUntil(() -> !Bank.isOpen(), Calculations.random(7500, 8500));
                                }
                            }
                        }
                    }
                }

            }
        }
        return Calculations.random(500, 800);
    }

    @Override
    public void onExit(){
        super.onExit();
    }

    @Override
    public void onPaint(Graphics graphics) {
        super.onPaint(graphics);
    }
}