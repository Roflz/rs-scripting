import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.entity.ItemWidget;
import com.epicbot.api.shared.entity.NPC;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.methods.IEquipmentAPI;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.util.time.Time;

import java.util.Arrays;
import java.util.List;

public abstract class GenUtils extends LoopScript {

    private static APIContext stx = APIContext.get();

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

    public static void removeNullValues(List<Crops> crops) {
        for(int i = crops.size() - 1 ; i >= 0 ; i--) {
            if(crops.get(i) == null) {
                crops.remove(i);
            }
        }
    }

    public static boolean hasNullValues(List<Crops> crops) {
        for(int i = crops.size() - 1 ; i >= 0 ; i--) {
            if(crops.get(i) == null) {
                return true;
            }
        }
        return false;
    }

    public static void setState(String newState) {
        main.watDo = newState;
    }

    public static void saveState() {
        main.savedState = main.watDo;
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

    public static int walkToAreaCentralTile(Area area) {
        if (!area.contains(stx.localPlayer().getLocation())) {
            System.out.println("Walking");
            stx.webWalking().walkTo(area.getCentralTile());
        }
        return 500;
    }

    public static int walkToAreaNearestTile(Area area) {
        if (!area.contains(stx.localPlayer().getLocation())) {
            stx.webWalking().walkTo(area.getNearestTile(APIContext.get()));
        } else {
            System.out.println("Player is already in Area");
        }
        return 500;
    }

    public static int walkToAreaRandomTile(Area area) {
        if (!area.contains(stx.localPlayer().getLocation())) {
            stx.webWalking().walkTo(area.getRandomTile());
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
