import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.script.LoopScript;

import java.util.List;

public abstract class InventoryUtils extends LoopScript {

    private static APIContext stx = APIContext.get();

    public static boolean doesInventoryHaveItems(List<String> items) {
        for(String item : items) {
            if(!doesInventoryHaveItem(item)) {
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

    public static boolean doesInventoryHaveItem(String item) {
        return stx.inventory().contains(item);
    }

}
