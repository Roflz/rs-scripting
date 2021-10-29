package utils;

import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.util.time.Time;

import java.util.List;

public abstract class InventoryUtils extends LoopScript {

    private static APIContext stx = APIContext.get();

    public static boolean inventoryIsFull() { return stx.inventory().isFull(); }

    public static int wield(String ...items) {
        for(String item : items) {
            if(stx.inventory().contains(item)) {
                stx.inventory().interactItem("Wield", item);
            }
        }
        return 600;
    }

    public static int wear(String ...items) {
        for(String item : items) {
            if(stx.inventory().contains(item)) {
                stx.inventory().interactItem("Wear", item);
            }
        }
        return 600;
    }

    public static int drop(String item) {
        if(stx.inventory().contains(item)) {
            stx.inventory().dropItem(item);
            return 600;
        }
        return 600;
    }

    public static int dropAllExcept(String ...items) {
        stx.inventory().dropAllExcept(items);
        return 600;
    }

    public static int useItemOnInventoryItem(String item, String itemToUseOn) {
        if(stx.inventory().contains(item) && stx.inventory().contains(itemToUseOn)) {
            stx.inventory().selectItem(item);
            stx.inventory().getItem(itemToUseOn).interact();
            return 1000;
        } else if(!stx.inventory().contains(item)){
            System.out.println("Inventory does not contain " + item);
            return 1000;
        } else if(!stx.inventory().contains(itemToUseOn)){
            System.out.println("Inventory does not contain " + itemToUseOn);
            return 1000;
        }
        return 1000;
    }

    public static int useItemOnObject(String item, String object) {
        if(stx.inventory().contains(item) && stx.objects().query().named(object).results().nearest() != null) {
            stx.inventory().selectItem(item);
            stx.objects().query().named(object).results().nearest().interact();
            return 1000;
        } else if(!stx.inventory().contains(item)){
            System.out.println("Inventory does not contain " + item);
            return 1000;
        } else if(stx.objects().query().named(object).results().nearest() == null){
            System.out.println(object + " is null");
            return 1000;
        }
        return 1000;
    }

    public static int useItemOnObjectAndWaitForDialogue(String item, String object) {
        if(stx.inventory().contains(item) && stx.objects().query().named(object).results().nearest() != null) {
            stx.inventory().selectItem(item);
            stx.objects().query().named(object).results().nearest().interact();
            Time.sleep(5_000, () -> stx.dialogues().isDialogueOpen());
            return 1000;
        } else if(!stx.inventory().contains(item)){
            System.out.println("Inventory does not contain " + item);
            return 1000;
        } else if(stx.objects().query().named(object).results().nearest() == null){
            System.out.println(object + " is null");
            return 1000;
        }
        return 1000;
    }

    public static boolean doesInventoryHaveItems(List<String> items) {
        for(String item : items) {
            if(!inventoryContains(item)) {
                return false;
            }
        }
        return true;
    }
    public static boolean doesInventoryHaveItemsWithQuantities(List<String> items, List<Integer> quantities) {
        int i = 0;
        for(String item : items) {
            if(!doesInventoryHaveItemWithQuantity(item, quantities.get(i))) {
                return false;
            }
            i += 1;
        }
        return true;
    }
    public static boolean doesInventoryHaveItemWithQuantity(String item, int quantity) {
        if(stx.inventory().getCount(item) == 0) {
            return stx.inventory().getCount(item) >= quantity;
        } else {
            return stx.inventory().getCount(item) >= quantity || stx.inventory().getItem(item).getStackSize() >= quantity;
        }
    }

    public static boolean inventoryContains(String item) { return stx.inventory().contains(item); }

    public static boolean inventoryContains(String ...items) {
        for(String item : items) {
            if(!stx.inventory().contains(item)) {
                return false;
            }
        }
        return true;
    }

    public static boolean inventoryContainsAny(String ...items) {
        for(String item : items) {
            if(stx.inventory().contains(item)) {
                return true;
            }
        }
        return false;
    }

}
