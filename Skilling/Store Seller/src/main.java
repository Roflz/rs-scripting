import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.Item;
import com.epicbot.api.shared.entity.ItemWidget;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.Random;

import java.util.*;

import static utils.BankUtils.*;
import static utils.Constants.*;
import static utils.GenUtils.*;
import static utils.InventoryUtils.*;
import static utils.GEUtils.*;

@ScriptManifest(name = "Store Seller", gameType = GameType.OS)
public class main extends LoopScript {

    private List<ItemWidget> itemsList = Arrays.asList();
    private List<Integer> quantitiesList = Arrays.asList();
    private int goldBefore = 0;
    private int goldAfter = 1;
    private int worldBefore;
    private int worldAfter = 1;

    @Override
    protected int loop() {
        if (!getAPIContext().client().isLoggedIn()) {
            return 300;
        }
        System.out.println(goldBefore);
        System.out.println(goldAfter);
        System.out.println(worldBefore);
        System.out.println(worldAfter);

        if(!getAPIContext().store().isOpen()) {

            if(goldAfter > goldBefore || worldBefore == worldAfter) {
                worldBefore = getAPIContext().world().getCurrent();
                goldBefore = goldAfter;
                getAPIContext().world().hopToP2P();
                return Random.nextInt(2000, 3000);
            }
            itemsList = getAPIContext().inventory().getItems("Shortbow (u)", "Longbow (u)", "Oak shortbow (u)","Oak longbow (u)", "Willow shortbow (u)", "Willow longbow (u)", "Maple shortbow (u)");
            getAPIContext().npcs().query().named("Chadwell").results().nearest().interact("Trade");
        }

        if(getAPIContext().store().isOpen()) {
            for(ItemWidget item : itemsList) {
                if(!getAPIContext().store().contains(item.getName()))
                    item.interact("Sell 10");
            }
            getAPIContext().store().close();
            goldAfter = getAPIContext().inventory().getItem("Coins").getStackSize();
            worldAfter = getAPIContext().world().getCurrent();
        }

        return Random.nextInt(1000, 2000);
    }

    @Override
    public boolean onStart(String... strings) {
        return true;
    }
}
