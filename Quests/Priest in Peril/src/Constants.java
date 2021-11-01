import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.SceneOffset;
import com.epicbot.api.shared.model.Tile;

public interface Constants {


    public final Area KING_ROALD = new Area(new Tile(3219, 3478, 0), new Tile(3225, 3470, 0));
    public final Area TEMPLE_DOOR = new Area(new Tile(3409, 3490, 0), new Tile(3406, 3487, 0));
    public final Area TEMPLE = new Area(new Tile(3408, 3494, 0), new Tile(3417, 3483, 0));
    public final Area TRAPDOOR = new Area(new Tile(3405, 3507, 0), new Tile(3405, 3505, 0));
    public final Area UPSTAIRS_KITCHEN = new Area(new Tile(3221, 3495, 1), new Tile(3222, 3497, 1));
    public final Area UPSTAIRS_TEMPLE = new Area(new Tile(3408, 3483, 1), new Tile(3419, 3494, 1));
    public final Area DREZEL = new Area(new Tile(3437, 9894, 0), new Tile(3442, 9900, 0));
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
    public static final SceneOffset ENTRANA_BOAT_OFFSET = new SceneOffset(48, 48, 0, 0);


}
