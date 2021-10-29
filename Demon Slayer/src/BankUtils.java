import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.methods.IBankAPI;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.webwalking.model.RSBank;

import java.util.List;

public abstract class BankUtils extends LoopScript {

    private static APIContext stx = APIContext.get();

    public static void depositInventoryIfNotEmpty() {
        if(!stx.inventory().isEmpty()) {
            stx.bank().depositInventory();
        }
    }

    public static void depositInventory() {
        stx.bank().depositInventory();
    }

    public static void goToClosestBank() {
        stx.webWalking().walkToBank();
    }

    public static void goToBank(RSBank bank) {
        stx.webWalking().walkToBank(bank);
    }

    public static void goToClosestBankAndOpen() {
        if(!stx.bank().isOpen()) {
            stx.webWalking().walkToBank();
            stx.bank().open();
            Time.sleep(5_000, () -> stx.bank().isOpen());
        } else {
            System.out.println("Bank is already open!");
        }
    }

    public static void openBank() {
        while(!stx.bank().isOpen()) {
            stx.bank().open();
            if(stx.bank().isOpen()) {
                break;
            }
        }
    }

    public static void closeBank() {
        while(stx.bank().isOpen()) {
            stx.bank().close();
            if(!stx.bank().isOpen()) {
                break;
            }
        }
    }

    public static void withdrawItemsFromListWithQuantity(List<String> items, List<Integer> quantities) {
        int i = 0;
        for(String item: items) {
            while(!stx.inventory().contains(item)) {
                stx.bank().withdraw(quantities.get(i), item);
                if(stx.inventory().contains(item)) {
                    break;
                }
            }
            i += 1;
        }
    }
    public static void withdrawAllItemsFromList(List<String> items) {
        for(String item: items) {
            stx.bank().withdrawAll(item);
        }
    }

    public static void withdrawAllCoins() {
        stx.bank().withdrawAll("Coins");
    }

    public static void depositAllCoins() {
        stx.bank().depositAll("Coins");
    }

    public static boolean doesBankHaveItems(List<String> items) {
        for(String item : items) {
            if(!doesBankHaveItem(item)) {
                return false;
            }
        }
        return true;
    }

    public static void withdrawAllAsNote(String item) {
        stx.bank().selectWithdrawMode(IBankAPI.WithdrawMode.NOTE);
        stx.bank().withdrawAll(item);
    }

    public static void withdrawXAsNote(int quantity, String item) {
        stx.bank().selectWithdrawMode(IBankAPI.WithdrawMode.NOTE);
        stx.bank().withdraw(quantity, item);
    }

    public static boolean doesBankHaveItemsWithQuantity(List<String> items, List<Integer> quantities) {
        int i = 0;
        for(String item : items) {
            if(!doesBankHaveItemWithQuantity(item, quantities.get(i))) {
                return false;
            }
            i += 1;
        }
        return true;
    }

    public static boolean doesBankHaveItemWithQuantity(String item, int quantity) {
        return stx.bank().getCount(item) >= quantity;
    }

    public static boolean doesBankHaveItem(String item) {
        return stx.bank().contains(item);
    }
}
