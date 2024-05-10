import java.util.List;

public class WaterBody {
    public enum WaterBodyType{
        OCEAN,
        SEA,
        ;

    }
    private WaterBodyType type;
    private String name;
    private double area;

    private List<WaterBody> neighbours;//or set...


    private double cordX;//or represented by Point(x,y) in world map;
    private double cordY;

}
