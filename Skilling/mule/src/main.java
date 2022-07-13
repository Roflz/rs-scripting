import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.methods.IBankAPI;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.time.Time;
import utils.BankUtils;
import utils.GenUtils;

import java.util.Arrays;
import java.util.List;

import static utils.BankUtils.*;
import static utils.Constants.*;
import static utils.GenUtils.*;
import static utils.InventoryUtils.*;
import static utils.GEUtils.*;

@ScriptManifest(name = "Mule", gameType = GameType.OS)
public class main extends LoopScript {

    public static String watDo = "";
    public static List<String> transferList = Arrays.asList("Grimy toadflax", "Yanillian hops", "Strawberry", "Cactus spine", "Jangerberries", "Limpwurt root");
    
    @Override
    protected int loop() {
        System.out.println(watDo);

        switch (watDo) {
            case "":
                if (!GRAND_EXCHANGE_AREA.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(GRAND_EXCHANGE_AREA.getRandomTile());
                    return 600;
                } else {
                    watDo = "Gather items";
                    return 600;
                }
            case "Gather items":
                if (!getAPIContext().bank().isOpen()) {
                    openBank();
                    getAPIContext().bank().depositInventory();
                    return 600;
                } else {
                    getAPIContext().bank().selectWithdrawMode(IBankAPI.WithdrawMode.NOTE);
                    for (String item : Crops.getItemNames()) {
                        getAPIContext().bank().withdrawAll(item);
                    }
                    getAPIContext().bank().withdraw(getAPIContext().bank().getCount("Coins") - 600_000, "Coins");
                    getAPIContext().bank().withdrawAll("Skills necklace");
                    int emptyItems = Crops.getItemNames().size();
                    if (getAPIContext().inventory().contains("Coins")) {
                        for (int i = 0; i < Crops.getItemNames().size(); i++) {
                            if (getAPIContext().bank().getCount(Crops.getItemNames().get(i)) == 0) {
                                emptyItems--;
                            }
                            if (emptyItems == 0) {
                                getAPIContext().bank().close();
                                watDo = "Trade";
                                return 1200;
                            }
                        }
                    }
                    return 1200;
                }
            case "Trade":
                if (!getAPIContext().trade().isTrading()) {
                    if (getAPIContext().players().query().named("roflezz").results().nearest() != null) {
                        getAPIContext().players().query().named("roflezz").results().nearest().interact("Trade with");
                        Time.sleep(15_000, () -> getAPIContext().trade().isTrading());
                        return 600;
                    }
                    return 600;
                } else {
                    watDo = "Trading";
                    return 1200;
                }
            case "Trading":
                System.out.println(getAPIContext().trade().getOtherPlayer());
                if (!getAPIContext().trade().isTrading()) {
                    watDo = "Done";
                    return 1200;
                } else if (getAPIContext().trade().getOtherPlayer().contains("roflezz")) {
                    for (String item : Crops.getItemNames()) {
                        getAPIContext().trade().offerAll(item);
                    }
                    getAPIContext().trade().offerAll("Coins");
                    getAPIContext().trade().offerAll("Skills necklace");
                    getAPIContext().trade().acceptTrade();
                    return 3000;
                }
                break;
            case "Done":
                if(!getAPIContext().bank().isOpen()) {
                    getAPIContext().webWalking().setUseTeleports(false);
                    BankUtils.goToClosestBank();
                    BankUtils.openBank();
                    return 1200;
                } else {
                    BankUtils.depositInventory();
                    getAPIContext().bank().depositEquipment();
                    BankUtils.closeBank();
                    GenUtils.logOut();
                    return 1200;
                }
        }

    return 1200;
    }

    @Override
    public boolean onStart(String... strings) {
        return true;
    }
}
