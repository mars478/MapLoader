
import com.imp.map.maploader.MapLoader;
import com.imp.map.maploader.MapPainter;
import com.imp.map.maploader.MarkerPainter;
import com.imp.map.maploader.OsmRenderer;
import com.imp.map.maploader.entity.GeoPoint;
import com.imp.map.maploader.entity.TileImage;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.Test;

public class RenderSample {

    @Test
    public void renderMap() throws InterruptedException {
        int zoom = 16;
        double lat = 55.755833d;
        double lon = 37.617778d;
        int width = 800;
        int height = 600;
        int az = 45;

        GeoPoint gp = new GeoPoint(lat, lon, zoom);
        OsmRenderer renderer = new OsmRenderer();

        MapPainter centerDot = new MapPainter() {
            @Override
            public GeoPoint center() {
                return new GeoPoint(lat, lon, zoom);
            }

            @Override
            public BufferedImage picture() {
                int side = 40; //px
                BufferedImage b = new BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB);
                Graphics g = b.getGraphics();
                g.setColor(Color.black);
                g.fillOval(0, 0, side, side);
                return b;
            }
        };

        MapPainter crossLines = new MapPainter() {
            @Override
            protected void draw(Graphics2D gg, MapLoader ml, List<TileImage> map) {
                int w = ml.getWidth();
                int h = ml.getHeight();
                gg.setColor(Color.black);
                gg.draw(new Line2D.Double(0, 0, w, h));
                gg.draw(new Line2D.Double(0, h, w, 0));
            }
        };

        BufferedImage img = renderer
                .prepareBackground(gp, width, height)
                //   .paintOver(centerDot)
                .paintOver(new MarkerPainter(lat, lon, az, zoom))
                .crop()
                .paintOver(crossLines)
                .get();

        JPanel mapViewer = new JPanel();
        mapViewer.add(new JLabel(new ImageIcon(img)));
        JFrame frame = new JFrame("OSM2Image fake frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mapViewer);
        frame.setSize(width, height);
        frame.pack();
        frame.setVisible(true);
        synchronized (this) {
            wait();
        }
    }
}
