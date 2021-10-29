package utils;

import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.entity.ItemWidget;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.methods.IEquipmentAPI;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.util.time.Time;

import java.util.List;

public abstract class GenUtils extends LoopScript {

    private static APIContext stx = APIContext.get();

    public static boolean dialogueIsOpen() { return stx.dialogues().isDialogueOpen(); }
    public static int selectContinue() {
        stx.dialogues().selectContinue();
        return 300;
    }
    public static int selectContinueUntilDialogueIsGone() {
        while(dialogueIsOpen()) {
            stx.dialogues().selectContinue();
        }

        return 300;
    }

    public static boolean playerLocationIs(Tile location) {
        return stx.localPlayer().getLocation().contains(location.getCentralPoint());
    }

    public static int proceedThroughDialogue() {
        if(dialogueIsOpen()) {
            if (stx.dialogues().canContinue()) {
                stx.dialogues().selectContinue();
                return 500;
            }
        }
        return 500;
    }

    public static int proceedThroughDialogue(String ...options) {
        if(dialogueIsOpen()) {
            System.out.println("hi");
            if (stx.dialogues().canContinue()) {
                stx.dialogues().selectContinue();
                return 500;
            } else {
                for(String option : options) {
                    if (stx.dialogues().selectOption(option)) { return 500; }
                }
            }
        }
        return 600;
    }

    public static int talkToNPC(String npcName) {
        if(stx.npcs().query().named(npcName).results().nearest() != null) {
            NPC npc = stx.npcs().query().named(npcName).results().nearest();
            stx.camera().turnTo(npc);
            if (!stx.dialogues().isDialogueOpen()) {
                stx.camera().turnTo(npc.getLocation());
                npc.interact("Talk-to");
                Time.sleep(10_000, () -> stx.dialogues().isDialogueOpen());
            } else {
                if (stx.dialogues().canContinue()) {
                    stx.dialogues().selectContinue();
                    return 500;
                }
            }
            return 1000;
        }
        return 1000;
    }

    public static int talkToNPC(String npcName, String ...options) {
        if(stx.npcs().query().named(npcName).results().nearest() != null) {
            NPC npc = stx.npcs().query().named(npcName).results().nearest();
            stx.camera().turnTo(npc);
            if (!stx.dialogues().isDialogueOpen()) {
                stx.camera().turnTo(npc.getLocation());
                npc.interact("Talk-to");
                Time.sleep(10_000, () -> stx.dialogues().isDialogueOpen());
            } else {
                if (stx.dialogues().canContinue()) {
                    stx.dialogues().selectContinue();
                    return 500;
                } else {
                    for(String option : options) {
                        if (stx.dialogues().selectOption(option)) { return 500; }
                    }
                }
            }
            return 1000;
        }
        return 1000;
    }

    public static int talkToNPCWithOptions(String npcName, Integer ...options) {
        if(stx.npcs().query().named(npcName).results().nearest() != null) {
            NPC npc = stx.npcs().query().named(npcName).results().nearest();
            stx.camera().turnTo(npc);
            if (!stx.dialogues().isDialogueOpen()) {
                stx.camera().turnTo(npc.getLocation());
                npc.interact("Talk-to");
                Time.sleep(10_000, () -> stx.dialogues().isDialogueOpen());
            } else {
                if (stx.dialogues().canContinue()) {
                    stx.dialogues().selectContinue();
                    return 500;
                } else {
                    for(Integer option : options) {
                        if (stx.dialogues().selectOption(option)) { return 500; }
                    }
                }
            }
            return 1000;
        }
        return 1000;
    }

    public static String dialogueText() { return stx.dialogues().getText(); }

    public static int interactWithObjectUntilDialogue(String objectName, String action) {
        if(stx.objects().query().named(objectName).actions(action).results().nearest() != null) {
            SceneObject object = stx.objects().query().named(objectName).actions(action).results().nearest();
            stx.camera().turnTo(object);
            object.interact(action);
            Time.sleep(10_000, () -> stx.dialogues().isDialogueOpen());
            return 1000;
        }
        return 1000;
    }

    public static int interactWithObject(String objectName, String action) {
        if(stx.objects().query().named(objectName).actions(action).results().nearest() != null) {
            SceneObject object = stx.objects().query().named(objectName).actions(action).results().nearest();
            stx.camera().turnTo(object);
            object.interact(action);
            return 1000;
        }
        return 1000;
    }

    public static int interactWithObject(SceneObject object, String action) {
            stx.camera().turnTo(object);
            object.interact(action);
            return 1000;
    }

    public static String getBottomChatMessage() {
        return stx.widgets().get(162).getChild(56).getChild(0).getText();
    }

    public static void logOut() {
        if (stx.bank().isOpen()) {
            stx.bank().close();
        } else {
            stx.game().logout();
            stx.script().stop("Done :)");
        }
    }

    public static boolean inventoryContainsTrash(List<String> trash) {
        for(String item : trash) {
            if(stx.inventory().contains(item)) {
                return true;
            }
        } return false;
    }

    public static void dropTrash(List<String> trash) {
        for(String item : trash) {
            stx.inventory().dropAll(item);
        }
    }

    public static int walkToTile(Tile tile) {
        if (!playerLocationIs(tile)) {
            System.out.println("Walking");
            stx.webWalking().walkTo(tile);
            return 1000;
        }
        return 500;
    }

    public static int walkToTileNoTeleports(Tile tile) {
        if (stx.localPlayer().getLocation() != tile) {
            System.out.println("Walking");
            stx.webWalking().setUseTeleports(false);
            stx.webWalking().walkTo(tile);
            stx.webWalking().setUseTeleports(true);
        }
        return 500;
    }

    public static int walkToAreaCentralTile(Area area) {
        if (!area.contains(stx.localPlayer().getLocation())) {
            System.out.println("Walking to " + area);
            stx.webWalking().walkTo(area.getCentralTile());
        }
        return 500;
    }

    public static int walkToAreaCentralTileNoTeleport(Area area) {
        if (!area.contains(stx.localPlayer().getLocation())) {
            System.out.println("Walking to " + area);
            stx.webWalking().setUseTeleports(false);
            stx.webWalking().walkTo(area.getCentralTile());
            stx.webWalking().setUseTeleports(true);
        }
        return 500;
    }

    public static int walkToAreaNearestTile(Area area) {
        if (!area.contains(stx.localPlayer().getLocation())) {
            System.out.println("Walking to " + area);
            stx.webWalking().walkTo(area.getNearestTile(APIContext.get()));
        } else {
            System.out.println("Player is already in Area");
        }
        return 500;
    }

    public static int walkToAreaNearestTileNoTeleport(Area area) {
        if (!area.contains(stx.localPlayer().getLocation())) {
            System.out.println("Walking to " + area);
            stx.webWalking().setUseTeleports(false);
            stx.webWalking().walkTo(area.getNearestTile(APIContext.get()));
            stx.webWalking().setUseTeleports(true);
        } else {
            System.out.println("Player is already in Area");
        }
        return 500;
    }

    public static int walkToAreaRandomTile(Area area) {
        if (!area.contains(stx.localPlayer().getLocation())) {
            System.out.println("Walking to " + area);
            stx.webWalking().walkTo(area.getRandomTile());
        } else {
            System.out.println("Player is already in Area");
        }
        return 500;
    }

    public static int walkToAreaRandomTileNoTeleport(Area area) {
        if (!area.contains(stx.localPlayer().getLocation())) {
            System.out.println("Walking to " + area);
            stx.webWalking().setUseTeleports(false);
            stx.webWalking().walkTo(area.getRandomTile());
            stx.webWalking().setUseTeleports(true);
        } else {
            System.out.println("Player is already in Area");
        }
        return 500;
    }

    public static void teleportWithChronicle() {
        ItemWidget shield = stx.equipment().getItem(IEquipmentAPI.Slot.SHIELD);
        if (shield != null) {
            if(shield.getName().contains("Chronicle")) {
                shield.interact("Teleport");
                Time.sleep(10_000, () -> Constants.CHRONICLE_TELEPORT_AREA.contains(stx.localPlayer().getLocation()));
            }
        }
    }

    public static boolean playerisInArea(Area area) {
        return area.contains(stx.localPlayer().getLocation());
    }

    public static void turnCameraTowards(SceneObject patch) {
        stx.camera().turnTo(patch.getLocation());
    }

    public static void turnCameraTowards(NPC npc) {
        stx.camera().turnTo(npc.getLocation());
    }

}
