
import com.imp.map.maploader.entity.GeoPoint;
import com.imp.map.maploader.entity.Tile;
import com.imp.map.maploader.entity.TileImage;
import org.junit.Test;
import static org.junit.Assert.*;

public class EntityTest {

    public EntityTest() {
    }

    @Test
    public void geoPoint() {
        GeoPoint g1 = new GeoPoint(0, 0, 0);
        GeoPoint g2 = new GeoPoint(0.0, 0d, 0);
        GeoPoint g3 = new GeoPoint(20, 30, 0);
        GeoPoint g4 = new GeoPoint(0, 0, 10);

        assertTrue(GeoPoint.same(g1, g2));
        assertFalse(GeoPoint.same(g1, g3));
        assertFalse(GeoPoint.same(g1, g4));
        assertFalse(GeoPoint.same(g2, g3));
        assertFalse(GeoPoint.same(g4, g3));
        assertFalse(GeoPoint.same(null, g3));
        assertFalse(GeoPoint.same(null, null));
    }

    @Test
    public void tile() {
        Tile t1 = new Tile(10, 10, 10, Tile.DEF_TILESIZE);
        Tile t2 = new Tile(10, 10, 10, Tile.DEF_TILESIZE);

        assertTrue(t1.sameGeoLocation(t2));
        assertFalse(t1.hasPixLocation());

        t1.setPixPosition(4, 2);
        assertTrue(t1.hasPixLocation());
    }

    @Test
    public void tileImage() {
        Tile t1 = new Tile(10, 10, 10, Tile.DEF_TILESIZE);
        Tile t2 = new Tile(10, 10, 10, Tile.DEF_TILESIZE);

        TileImage ti = new TileImage(null, t1);
        assertTrue(ti.empty);
        assertTrue(ti.sameGeoLocation(t2));
        assertFalse(ti.hasPixLocation());

        ti.setPixPosition(4, 2);
        assertTrue(ti.hasPixLocation());
    }

}
