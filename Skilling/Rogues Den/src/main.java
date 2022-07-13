import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.details.Locatable;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.webwalking.model.WebPath;
import utils.BankUtils;
import utils.Constants;
import utils.GenUtils;

@ScriptManifest(name = "Rogues Den", gameType = GameType.OS)
public class main extends LoopScript {

    public static String watDo = "";

    @Override
    protected int loop() {

        if(Constants.ROGUES_DEN_MAZE_START.contains(getAPIContext().localPlayer().getLocation()) && getAPIContext().inventory().isEmpty()) {
            if(getAPIContext().inventory().isEmpty()) {
                watDo = "Start Maze";
            } else {
                watDo = "Bank";
            }
        }

        if(!getAPIContext().walking().isRunEnabled()) {
            if(getAPIContext().walking().getRunEnergy() == 100) {
                getAPIContext().walking().setRun(true);
            }
        }

        switch (watDo) {

            case "":
                getAPIContext().camera().setPitch(98);
                getAPIContext().mouse().move(getAPIContext().game().getCenterSceneTile().getCentralPoint());
                getAPIContext().mouse().scroll(false, 20);
                watDo = "Go to Rogues Den";
                return 500;

            case "Go to Rogues Den":
                if(!Constants.ROGUES_DEN.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.ROGUES_DEN.getCentralTile());
                    return 1200;
                } else {
                    watDo = "Bank";
                    return 1200;
                }

            case "Bank":
                if(!getAPIContext().bank().isOpen()) {
                    BankUtils.goToClosestBank();
                    BankUtils.openBank();
                    return 1200;
                } else if(!getAPIContext().inventory().isEmpty() || !getAPIContext().equipment().isEmpty()){
                    getAPIContext().bank().depositEquipment();
                    getAPIContext().bank().depositInventory();
                    return 1200;
                } else if(getAPIContext().bank().getCount("Rogue's equipment crate") == 5) {
                    watDo = "Done";
                    return 1200;
                } else {
                    getAPIContext().bank().close();
                    watDo = "Go to maze start";
                    return 1200;
                }

            case "Go to maze start":
                if(!Constants.ROGUES_DEN_MAZE_START.contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().webWalking().walkTo(Constants.ROGUES_DEN_MAZE_START.getCentralTile());
                    return 1200;
                } else {
                    watDo = "Start Maze";
                    return 1200;
                }

            case "Start Maze":
                if(getAPIContext().objects().query().named("Doorway").results().nearest() != null) {
                    getAPIContext().camera().turnTo(getAPIContext().objects().query().named("Doorway").results().nearest().getLocation());
                    getAPIContext().objects().query().named("Doorway").results().nearest().interact("Open");
                    Time.sleep(2_000, 5_000, () -> getAPIContext().inventory().contains("Mystic jewel"));
                    return 600;
                }
                if(getAPIContext().inventory().contains("Mystic jewel")) {
                    watDo = "Run Maze";
                    return 600;
                }
                break;

            case "Run Maze":
                if(new Area(new Tile(3060, 4991, 1), new Tile(3050, 5003, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().camera().turnTo(getAPIContext().objects().query().named("Contortion Bars").results().nearest().getLocation());
                    getAPIContext().objects().query().named("Contortion Bars").results().nearest().interact("Enter");
                    Time.sleep(2_000, 5_000, () -> new Area(new Tile(3049, 4995, 1), new Tile(3043, 5003, 1)).contains(getAPIContext().localPlayer().getLocation()));
                    return 1200;
                }
                if(new Area(new Tile(3049, 4996, 1), new Tile(3023, 5004, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    if(getAPIContext().localPlayer().getX() != 3026) {
                        getAPIContext().mouse().click(new Tile(3039, 4999, 1));
                        Time.sleep(3_000, 15_000, () -> getAPIContext().localPlayer().getX() == 3039);
                        Time.sleep(1200);
                        getAPIContext().mouse().click(new Tile(3037, 5001, 1));
                        Time.sleep(10_000, () -> getAPIContext().localPlayer().getX() == 3037);
                        Time.sleep(500);
                        getAPIContext().mouse().click(new Tile(3037, 5002, 1));
                        Time.sleep(3_000, () -> getAPIContext().localPlayer().getY() == 5002);
                        Time.sleep(500);
                        getAPIContext().mouse().click(new Tile(3026, 5002, 1));
                        Time.sleep(3_000, 20_000, () -> getAPIContext().localPlayer().getX() == 3026);
                        return 1200;
                    } else {
                        getAPIContext().camera().turnTo(getAPIContext().objects().query().named("Grill").results().nearest().getLocation());
                        getAPIContext().objects().query().named("Grill").results().nearest().interact("Open");
                        Time.sleep(2_000, 7_000, () -> new Area(new Tile(3024, 4999, 1), new Tile(3020, 5003, 1)).contains(getAPIContext().localPlayer().getLocation()));
                        return 1200;
                    }
                }
                if(new Area(new Tile(3024, 4999, 1), new Tile(3005, 5006, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().mouse().click(new Tile(3014, 5003, 1));
                    Time.sleep(3_000, 10_000, () -> getAPIContext().localPlayer().getX() == 3014);
                    Time.sleep(500);
                    if(!getAPIContext().walking().isRunEnabled()) {
                        if(getAPIContext().walking().getRunEnergy() < 4) {
                            Time.sleep(60_000, () -> getAPIContext().walking().getRunEnergy() >= 4);
                        }
                        getAPIContext().walking().setRun(true);
                    }
                    getAPIContext().mouse().click(new Tile(3011, 5005, 1));
                    Time.sleep(10_000, () -> getAPIContext().localPlayer().getX() == 3011);
                    Time.sleep(500);
                    getAPIContext().mouse().click(new Tile(3003, 5003, 1));
                    Time.sleep(3_000, 10_000, () -> getAPIContext().localPlayer().getX() == 3003);
                    return 1200;
                }
                if(new Area(new Tile(3005, 5008, 1), new Tile(2990, 4998, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().camera().turnTo(getAPIContext().objects().query().named("Ledge").results().nearest().getLocation());
                    getAPIContext().objects().query().named("Ledge").results().nearest().interact("Climb");
                    Time.sleep( 20_000, () -> getAPIContext().localPlayer().getX() == 2988);
                    return 1200;
                }
                if(new Area(new Tile(2989, 4997, 1), new Tile(2969, 5020, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().mouse().click(new Tile(2976, 5013, 1));
                    Time.sleep( 20_000, () -> getAPIContext().localPlayer().getY() == 5013);
                    Time.sleep(500);
                    getAPIContext().mouse().click(new Tile(2969, 5017, 1));
                    Time.sleep( 15_000, () -> getAPIContext().localPlayer().getX() == 2967);
                    Time.sleep(1200);
                    getAPIContext().mouse().click(new Tile(2958, 5030, 1));
                    Time.sleep( 20_000, () -> getAPIContext().localPlayer().getY() == 5028);
                    Time.sleep(500);
                    getAPIContext().camera().turnTo(getAPIContext().objects().query().named("Ledge").results().nearest().getLocation());
                    getAPIContext().objects().query().named("Ledge").results().nearest().interact("Climb");
                    Time.sleep( 15_000, () -> getAPIContext().localPlayer().getY() == 5035);
                    return 1200;
                }
                if(new Area(new Tile(2955, 5034, 1), new Tile(2970, 5075, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().mouse().click(new Tile(2962, 5048, 1));
                    Time.sleep( 15_000, () -> getAPIContext().localPlayer().getY() == 5048);
                    Time.sleep(500);
                    getAPIContext().mouse().click(new Tile(2962, 5052, 1));
                    Time.sleep( 10_000, () -> getAPIContext().localPlayer().getY() == 5052);
                    Time.sleep(500);
                    getAPIContext().mouse().click(new Tile(2962, 5054, 1));
                    Time.sleep( 10_000, () -> getAPIContext().localPlayer().getY() == 5054);
                    Time.sleep(500);
                    getAPIContext().camera().turnTo(getAPIContext().objects().query().named("Passageway").located(new Tile(2957, 5069, 1)).results().nearest());
                    getAPIContext().objects().query().named("Passageway").located(new Tile(2957, 5069, 1)).results().nearest().interact("Enter");
                    Time.sleep( 20_000, () -> getAPIContext().localPlayer().getY() == 5072);
                    Time.sleep(500);
                    getAPIContext().mouse().click(new Tile(2957, 5074, 1));
                    Time.sleep( 10_000, () -> getAPIContext().localPlayer().getY() == 5076);
                    Time.sleep(1200);
                    return 1200;
                }
                if(new Area(new Tile(2953, 5075, 1), new Tile(2959, 5095, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().mouse().click(new Tile(2955, 5084, 1));
                    Time.sleep( 15_000, () -> getAPIContext().localPlayer().getY() == 5084);
                    Time.sleep(500);
                    getAPIContext().camera().turnTo(getAPIContext().objects().query().named("Passageway").located(new Tile(2955, 5095, 1)).results().nearest());
                    getAPIContext().objects().query().named("Passageway").located(new Tile(2955, 5095, 1)).results().nearest().interact("Enter");
                    Time.sleep( 15_000, () -> getAPIContext().localPlayer().getY() == 5098);
                    return 1200;
                }
                if(new Area(new Tile(2975, 5096, 1), new Tile(2952, 5110, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().mouse().click(new Tile(2960, 5105, 1));
                    Time.sleep( 15_000, () -> getAPIContext().localPlayer().getX() == 2960);
                    Time.sleep(500);
                    getAPIContext().camera().turnTo(getAPIContext().objects().query().named("Passageway").located(new Tile(2972, 5097, 1)).results().nearest());
                    getAPIContext().objects().query().named("Passageway").located(new Tile(2972, 5097, 1)).results().nearest().interact("Enter");
                    Time.sleep( 20_000, () -> getAPIContext().localPlayer().getY() == 5094);
                    return 1200;
                }
                if(new Area(new Tile(2968, 5095, 1), new Tile(2989, 5083, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().objects().query().named("Grill").results().nearest().interact("Open");
                    Time.sleep( 20_000, () -> getAPIContext().localPlayer().getY() == 5093);
                    Time.sleep(500);
                    getAPIContext().mouse().click(new Tile(2976, 5086, 1));
                    Time.sleep( 15_000, () -> getAPIContext().localPlayer().getX() == 2976);
                    Time.sleep(500);
                    getAPIContext().camera().turnTo(getAPIContext().objects().query().named("Ledge").results().nearest());
                    getAPIContext().objects().query().named("Ledge").results().nearest().interact("Climb");
                    Time.sleep( 20_000, () -> getAPIContext().localPlayer().getX() == 2991);
                    return 1200;
                }
                if(new Area(new Tile(2989, 5085, 1), new Tile(3007, 5094, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    if(getAPIContext().localPlayer().getX() == 2991) {
                        getAPIContext().objects().query().named("Wall").results().nearest().interact("Search");
                        Time.sleep( 10_000, () -> getAPIContext().localPlayer().getX() == 2993);
                        return 1200;
                    }
                    if(!getAPIContext().walking().isRunEnabled()) {
                        if(getAPIContext().walking().getRunEnergy() < 4) {
                            Time.sleep(60_000, () -> getAPIContext().walking().getRunEnergy() >= 4);
                        }
                        getAPIContext().walking().setRun(true);
                    }
                    getAPIContext().mouse().click(new Tile(3001, 5087, 1));
                    Time.sleep( 15_000, () -> getAPIContext().localPlayer().getX() == 3003);
                    Time.sleep(1200);
                    getAPIContext().mouse().click(new Tile(3018, 5081, 1));
                    Time.sleep( 15_000, () -> getAPIContext().localPlayer().getX() == 3018);
                    Time.sleep(1200);
                    return 1200;
                }
                if(new Area(new Tile(3006, 5091, 1), new Tile(3023, 5079, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    if(!getAPIContext().inventory().contains("Tile")) {
                        getAPIContext().groundItems().query().named("Tile").results().nearest().interact("Take");
                        Time.sleep( 5_000, () -> getAPIContext().inventory().contains("Tile"));
                        return 1200;
                    }
                    if(getAPIContext().widgets().get(688).isVisible()) {
                        getAPIContext().widgets().get(688).getChild(5).interact("Select");
                        Time.sleep(10_000, () -> GenUtils.getBottomChatMessage().contains("A perfect match!"));
                        Time.sleep(1200);
                    } else {
                        getAPIContext().objects().query().named("Door").results().nearest().interact("Open");
                        Time.sleep( 8_000, () -> getAPIContext().widgets().get(688).isVisible());
                        return 1200;
                    }
                    return 1200;
                }
                if(new Area(new Tile(3024, 5083, 1), new Tile(3030, 5078, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    getAPIContext().objects().query().named("Grill").results().nearest().interact("Open");
                    Time.sleep( 8_000, () -> getAPIContext().localPlayer().getX() == 3031);
                    return 1200;
                }
                if(new Area(new Tile(3031, 5080, 1), new Tile(3045, 5066, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    if(getAPIContext().localPlayer().getX() == 3031) {
                        getAPIContext().camera().turnTo(new Tile(3032, 5078, 1));
                        getAPIContext().objects().query().named("Grill").located(new Tile(3032, 5078, 1)).results().nearest().interact("Open");
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getY() == 5077);
                        Time.sleep(600);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getX() == 3032) {
                        getAPIContext().camera().turnTo(new Tile(3036, 5076, 1));
                        getAPIContext().objects().query().named("Grill").located(new Tile(3036, 5076, 1)).results().nearest().interact("Open");
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getX() == 3037);
                        Time.sleep(600);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getX() == 3037 && getAPIContext().localPlayer().getY() > 5067) {
                        getAPIContext().camera().turnTo(new Tile(3039, 5079, 1));
                        getAPIContext().objects().query().named("Grill").located(new Tile(3039, 5079, 1)).results().nearest().interact("Open");
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getX() == 3040);
                        Time.sleep(600);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getX() == 3040) {
                        getAPIContext().camera().turnTo(new Tile(3042, 5076, 1));
                        getAPIContext().objects().query().named("Grill").located(new Tile(3042, 5076, 1)).results().nearest().interact("Open");
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getX() == 3043);
                        Time.sleep(600);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getX() == 3043) {
                        getAPIContext().camera().turnTo(new Tile(3044, 5069, 1));
                        getAPIContext().objects().query().named("Grill").located(new Tile(3044, 5069, 1)).results().nearest().interact("Open");
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getY() == 5068);
                        Time.sleep(600);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getY() == 5068 && getAPIContext().localPlayer().getX() != 3038) {
                        getAPIContext().camera().turnTo(new Tile(3041, 5068, 1));
                        getAPIContext().objects().query().named("Grill").located(new Tile(3041, 5068, 1)).results().nearest().interact("Open");
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getY() == 5069);
                        Time.sleep(600);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getY() == 5069) {
                        getAPIContext().camera().turnTo(new Tile(3040, 5070, 1));
                        getAPIContext().objects().query().named("Grill").located(new Tile(3040, 5070, 1)).results().nearest().interact("Open");
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getX() == 3039);
                        Time.sleep(600);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getX() == 3039) {
                        getAPIContext().camera().turnTo(new Tile(3038, 5069, 1));
                        getAPIContext().objects().query().named("Grill").located(new Tile(3038, 5069, 1)).results().nearest().interact("Open");
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getY() == 5068);
                        Time.sleep(600);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getY() == 5068) {
                        getAPIContext().camera().turnTo(new Tile(3037, 5057, 1));
                        getAPIContext().mouse().click(new Tile(3037, 5057, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getY() < 5060);
                        return 1200;
                    }
                    return 1200;
                }
                if(new Area(new Tile(3045, 5063, 1), new Tile(3020, 5031, 1)).contains(getAPIContext().localPlayer().getLocation()) && (getAPIContext().localPlayer().getX() > 3031 || getAPIContext().localPlayer().getY() < 5041)) {
                    if(getAPIContext().localPlayer().getY() > 5037) {
                        getAPIContext().mouse().click(new Tile(3039, 5044, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getY() < 5046);
                        Time.sleep(600);
                        getAPIContext().mouse().click(new Tile(3037, 5033, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getY() < 5035);
                        Time.sleep(1200);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getX() != 3028) {
                        getAPIContext().camera().turnTo(new Tile(3028, 5033, 1));
                        getAPIContext().mouse().click(new Tile(3028, 5033, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getX() == 3028);
                        Time.sleep(1200);
                        return 1200;
                    } else {
                        if(!getAPIContext().walking().isRunEnabled()) {
                            if(getAPIContext().walking().getRunEnergy() < 4) {
                                Time.sleep(60_000, () -> getAPIContext().walking().getRunEnergy() >= 4);
                            }
                            getAPIContext().walking().setRun(true);
                        }
                        getAPIContext().camera().turnTo(new Tile(3015, 5033, 1));
                        getAPIContext().mouse().click(new Tile(3015, 5033, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getX() < 3024);
                        return 1200;
                    }
                }
                if(new Area(new Tile(3019, 5031, 1), new Tile(3002, 5035, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    if(getAPIContext().localPlayer().getX() > 3014) {
                        getAPIContext().objects().query().named("Grill").results().nearest().interact("Open");
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getX() == 3014);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getX() == 3014) {
                        if(!getAPIContext().walking().isRunEnabled()) {
                            if(getAPIContext().walking().getRunEnergy() < 4) {
                                Time.sleep(60_000, () -> getAPIContext().walking().getRunEnergy() >= 4);
                            }
                            getAPIContext().walking().setRun(true);
                        }
                        getAPIContext().camera().turnTo(new Tile(3010, 5033, 1));
                        getAPIContext().mouse().click(new Tile(3010, 5033, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getX() == 3010);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getX() == 3010) {
                        getAPIContext().objects().query().named("Grill").results().nearest().interact("Open");
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getX() == 3009);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getX() == 3009) {
                        if(!getAPIContext().walking().isRunEnabled()) {
                            if(getAPIContext().walking().getRunEnergy() < 4) {
                                Time.sleep(60_000, () -> getAPIContext().walking().getRunEnergy() >= 4);
                            }
                            getAPIContext().walking().setRun(true);
                        }
                        getAPIContext().camera().turnTo(new Tile(3004, 5033, 1));
                        getAPIContext().mouse().click(new Tile(3004, 5033, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getX() == 3004);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getX() == 3004) {
                        getAPIContext().camera().turnTo(new Tile(2999, 5033, 1));
                        getAPIContext().mouse().click(new Tile(2999, 5033, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getX() == 3000);
                        return 1200;
                    }
                    return 1200;
                }
                if(new Area(new Tile(3001, 5032, 1), new Tile(2990, 5056, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    if(getAPIContext().localPlayer().getY() < 5041) {
                        getAPIContext().camera().turnTo(new Tile(2996, 5043, 1));
                        getAPIContext().mouse().click(new Tile(2996, 5043, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getY() < 5041);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getY() != 5045) {
                        getAPIContext().camera().turnTo(new Tile(2992, 5045, 1));
                        getAPIContext().mouse().click(new Tile(2992, 5045, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getY() == 5045);
                        return 1200;
                    } else {
                        if(!getAPIContext().walking().isRunEnabled()) {
                            if(getAPIContext().walking().getRunEnergy() < 4) {
                                Time.sleep(60_000, () -> getAPIContext().walking().getRunEnergy() >= 4);
                            }
                            getAPIContext().walking().setRun(true);
                        }
                        getAPIContext().camera().turnTo(new Tile(2992, 5059, 1));
                        getAPIContext().mouse().click(new Tile(2992, 5059, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getY() > 5056);
                        return 1200;
                    }
                }
                if(new Area(new Tile(2990, 5057, 1), new Tile(2999, 5079, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    if (getAPIContext().localPlayer().getY() < 5067) {
                        getAPIContext().camera().turnTo(new Tile(2992, 5067, 1));
                        getAPIContext().mouse().click(new Tile(2992, 5067, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getY() == 5067);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getY() == 5067) {
                        if(!getAPIContext().walking().isRunEnabled()) {
                            if(getAPIContext().walking().getRunEnergy() < 4) {
                                Time.sleep(60_000, () -> getAPIContext().walking().getRunEnergy() >= 4);
                            }
                            getAPIContext().walking().setRun(true);
                        }
                        getAPIContext().camera().turnTo(new Tile(3003, 5067, 1));
                        getAPIContext().mouse().click(new Tile(3003, 5067, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getX() > 3001);
                        return 1200;
                    }
                    return 1200;
                }
                if(new Area(new Tile(3000, 5074, 1), new Tile(3030, 5058, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    if(!getAPIContext().inventory().contains("Flash powder")) {
                        if(getAPIContext().walking().getRunEnergy() < 6) {
                            Time.sleep(60_000, () -> getAPIContext().walking().getRunEnergy() >= 6);
                        }
                        getAPIContext().groundItems().query().named("Flash powder").located(new Tile(3009, 5063, 1)).results().nearest().interact("Take");
                        Time.sleep(10_000, () -> getAPIContext().inventory().contains("Flash powder"));
                        return 600;
                    } else {
                        if(!getAPIContext().walking().isRunEnabled()) {
                            getAPIContext().walking().setRun(true);
                        }
                        getAPIContext().inventory().getItem("Flash powder").interact("Use");
                        Time.sleep(5_000, () -> getAPIContext().inventory().isItemSelected());
                        getAPIContext().camera().turnTo(getAPIContext().npcs().query().named("Rogue Guard").results().nearest().getLocation());
                        getAPIContext().npcs().query().named("Rogue Guard").results().nearest().interact();
                        Time.sleep(10_000, () -> getAPIContext().localPlayer().isAnimating());
                        getAPIContext().mouse().click(new Tile(3023, 5060, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getX() > 3020);
                        getAPIContext().mouse().click(new Tile(3029, 5055, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getY() < 5057);
                    }
                }
                if(new Area(new Tile(3030, 5057, 1), new Tile(3010, 5042, 1)).contains(getAPIContext().localPlayer().getLocation())) {
                    if(!getAPIContext().inventory().contains("Mystic jewel")) {
                        watDo = "";
                        return 1200;
                    }
                    if (getAPIContext().localPlayer().getY() > 5054) {
                        getAPIContext().camera().turnTo(new Tile(3028, 5051, 1));
                        getAPIContext().mouse().click(new Tile(3028, 5051, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getY() == 5051);
                        return 1200;
                    } else if (getAPIContext().localPlayer().getY() > 5050) {
                        getAPIContext().camera().turnTo(new Tile(3028, 5047, 1));
                        getAPIContext().mouse().click(new Tile(3028, 5047, 1));
                        Time.sleep( 8_000, () -> getAPIContext().localPlayer().getY() == 5047);
                        return 1200;
                    } else if(getAPIContext().localPlayer().getY() < 5050) {
                        getAPIContext().objects().query().named("Wall safe").results().nearest().interact("Crack");
                        Time.sleep(15_000, () -> getAPIContext().dialogues().canContinue());
                        return 1200;
                    }
                    return 1200;
                }
                break;
            case "Done":
//                if(getAPIContext().inventory().contains("Rogue's equipment crate")) {
//
//                }
//                if(!getAPIContext().bank().isOpen()) {
//                    BankUtils.goToClosestBank();
//                    BankUtils.openBank();
//                    return 1200;
//                } else if(getAPIContext().inventory().getCount("Rogue's equipment crate") < 5){
//                    getAPIContext().bank().withdraw(5, "Rogue's equipment crate");
//                    return 1200;
//                } else if(getAPIContext().inventory().getCount("Rogue's equipment crate") == 5) {
//                    getAPIContext().bank().close();
//                    return 1200;
//                }

        }

        return 1200;
    }

    @Override
    public boolean onStart(String... strings) {
        return true;
    }
}
