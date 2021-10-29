import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.time.Time;

@ScriptManifest(name = "GEUtil", gameType = GameType.OS)
public class main extends LoopScript {

    private static APIContext ctx = APIContext.get();

    //private GEUtils ge = new GEUtils();

    @Override
    protected int loop() {

//        geUtils.openGE();
//        geUtils.makeBuyOfferAndIncreasePriceUntilCompleted("Air rune", 10, 15000);
//        geUtils.makeBuyOffer("Air rune", 10);
        if(geUtils.isBuyOfferCompleted("Air rune")) {
            geUtils.viewOffer("Air rune");
            geUtils.collectOfferToInventoryItem();
        }
//        geUtils.viewOffer("Air rune");
//        geUtils.collectBuyOfferToInventoryItem();
//        geUtils.viewOffer("Tomato");
//        geUtils.collectBuyOfferToBank();
//        geUtils.viewOffer("Spade");
//        geUtils.collectBuyOfferToInventoryNote();
//        geUtils.viewOffer("Grimy guam leaf");
//        geUtils.collectSellOfferToInventory();
//        geUtils.collectToBank();
//        geUtils.closeGE();

//        geUtils.viewAndCollectOfferItem(geUtils.getGESlotWithItem("Sapphire ring"));

        utils.sleepUntil(geUtils.getGESlotWithItem("Air rune") != null);
        System.out.println("hi");

        return 1000;
    }

    @Override
    public boolean onStart(String... strings) {
        return true;
    }
}
