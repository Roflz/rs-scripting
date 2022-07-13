import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.SceneOffset;
import com.epicbot.api.shared.model.Tile;

public interface Constants {

    public interface FarmingAreas {
        public static final Area CATHERBY_AREA = new Area(new Tile(2803, 3471, 0), new Tile(2817, 3456, 0));
        public static final Area ARDOUGNE_AREA = new Area(new Tile(2659, 3382, 0), new Tile(2675, 3367, 0));
        public static final Area FALADOR_AREA = new Area(new Tile(3049, 3313, 0), new Tile(3061, 3301, 0));
        public static final Area HOSIDIUS_AREA = new Area(new Tile(1728, 3562, 0), new Tile(1742, 3547, 0));
        public static final Area LUMBRIDGE_AREA = new Area(new Tile(3232, 3312, 0), new Tile(3234, 3317, 0));
        public static final Area MCGRUBOR_AREA = new Area(new Tile(2674, 3518, 0), new Tile(2660, 3532, 0));
        public static final Area YANILLE_AREA = new Area(new Tile(2580, 3100, 0), new Tile(2569, 3108, 0));
        public static final Area ENTRANA_AREA = new Area(new Tile(2808, 3334, 0), new Tile(2813, 3333, 0));
        public static final Area ENTRANA_BIGGER_AREA = new Area(new Tile(2817, 3340, 0), new Tile(2807, 3330, 0));
        public static final Area FARMING_GUILD_AREA = new Area(new Tile(1274, 3720, 0), new Tile(1224, 3748, 0));
        public static final Area FARMING_GUILD_WEST_AREA = new Area(new Tile(1253, 3720, 0), new Tile(1224, 3748, 0));
        public static final Area RIMMINGTON_AREA = new Area(new Tile(2937, 3226, 0), new Tile(2944, 3218, 0));
        public static final Area MONASTERY_AREA = new Area(new Tile(2612, 3230, 0), new Tile(2621, 3221, 0));
        public static final Area CHAMPIONS_GUILD_AREA = new Area(new Tile(3182, 3354, 0), new Tile(3179, 3361, 0));
        public static final Area AL_KHARID_AREA = new Area(new Tile(3312, 3206, 0), new Tile(3320, 3199, 0));
    }

    public final Area GRAND_EXCHANGE_AREA = new Area(new Tile(3157, 3482, 0), new Tile(3172, 3495, 0));
    public final Area CHRONICLE_TELEPORT_AREA = new Area(new Tile(3195, 3349, 0), new Tile(3207, 3363, 0));
    //public static final Area DOCK_TO_ENTRANA = new Area(new Tile(3041, 3237, 0), new Tile(3050, 3234, 0));
    public static final Area DOCK_TO_ENTRANA = new Area(new Tile(3047, 3235, 0), new Tile(3043, 3236, 0));
//    public static final Area DOCK_FROM_ENTRANA = new Area(new Tile(2837, 3335, 0), new Tile(2831, 3336, 0));
    public static final Area DOCK_FROM_ENTRANA = new Area(new Tile(2833, 3335, 0), new Tile(2831, 3336, 0));
    public static final Area VEOS_PORT_SARIM = new Area(new Tile(3052, 3248, 0), new Tile(3053, 3247, 0));
    public static final Area ONSHIP_ENTRANA = new Area(new Tile(2834, 3331, 1), new Tile(2834, 3334, 1));
    public static final Area ONSHIP_PORT_SARIM = new Area(new Tile(3048, 3230, 1), new Tile(3048, 3232, 1));
    public static final Area OFFSHIP_ENTRANA = new Area(new Tile(2835, 3333, 0), new Tile(2833, 3336, 0));
    public static final Area MIDLAND_ENTRANA = new Area(new Tile(2836, 3345, 0), new Tile(2813, 3329, 0));
    public static final Area DRAYNOR_VILLAGE = new Area(new Tile(3077, 3252, 0), new Tile(3084, 3247, 0));
    public static final SceneOffset ENTRANA_BOAT_OFFSET = new SceneOffset(48, 48, 0, 0);
    public static final Area FARMING_GUILD_EAST_LEPRECHAUN = new Area(new Tile(1274, 3720, 0), new Tile(1248, 3744, 0));
    public static final Area FARMING_GUILD_WEST_LEPRECHAUN = new Area(new Tile(1248, 3720, 0), new Tile(1224, 3744, 0));


}
