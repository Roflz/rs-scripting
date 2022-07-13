import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import static utils.BankUtils.*;
import static utils.Constants.*;
import static utils.GenUtils.*;
import static utils.InventoryUtils.*;
import static utils.GEUtils.*;

@ScriptManifest(name = "Sheep Shearer", gameType = GameType.OS)
public class main extends LoopScript {
    
    @Override
    protected int loop() {
        return 0;
    }

    @Override
    public boolean onStart(String... strings) {
        return false;
    }
}
