package View;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import MapComponents.Node;
import MapComponents.Region;

/**
 * 
 * @author Andrei C
 *	code is a modified version of source code for Assignment 5, Winter 2014 Comp 202
 *	original author: Jonathan Trembley, University of McGill, COMP 202
 */
public class MapView extends JFrame implements Runnable{
	public Region region;
	public List<Node> nodes;
	public ArrayList<Color> nodeColors;

	public int PAD = 30;
	public int MAX_X, MAX_Y;

	public	int pointSize = 10;

	public MapView(Region region, String name){
		super("The Map of the Amazing " + name);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Map map = new Map();
		add(map);
		setSize(1024, 1024);
		setVisible(true);

		this.region = region;
		this.nodes = region.getVoronoiNodes();

		this.MAX_X = this.MAX_Y = region.getRegionSize();

	}

	@Override
	public void run() 
	{
		while (true) 
		{
			this.nodes = new ArrayList<>();
			this.nodes.addAll(region.getNodes());
			//this.nodes.addAll(region.getVoronoiNodes());
			nodeColors = new ArrayList<Color>();  
			int colorScheme = 1;
			int maxHeight = region.computeMaximumElevation();

			switch (colorScheme)
			{
			case 0: // blue elevation map
				for (int i = 0; i < this.nodes.size(); i++)
				{
					Node node = nodes.get(i);
					double color = (1.0*node.getZ()/maxHeight);
					Color c = new Color((int)(255-55-200*color%200), (int)(255 - 35 - 220*color%220), 250,250);
					nodeColors.add(i,c);
				}
				break;

			case 1: 
				for (int i = 0; i < this.nodes.size(); i++)
				{	
					Node node = nodes.get(i);	
					if (!node.getWater())
					{ 
						// land
						double color = (1.0*(node.getZ() - region.getWaterLevel())/(region.getMaximumElevation()-region.getWaterLevel())*1.5);
						if (color < 0) color = 0;
						if (color > 1 ) color = 1;
						Color c = new Color((int)(0 + 250*color), (int)(150 + 100 * color), (int)(0 + 250*color),200);
						c = c.darker();
						nodeColors.add(i,c);
						}
					
					else
					{ 
						// water
						double color = (1.0*(region.getWaterLevel() - node.getZ())/(region.getWaterLevel()-region.getMinimumElevation())*3);
						if (color < 0) color = 0;
						if (color > 1 ) color = 1;
						Color c = new Color((int)(255 - 85 - 170*color), (int)(255 - 65 - 190*color), 250,200);
						nodeColors.add(i,c);
					}
				}
				break;
			default:
				break;
			}
			this.repaint();
			try
			{
				Thread.sleep(2000);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	public class Map extends JPanel
	{
		protected void paintComponent(Graphics g) {

			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			g2.setColor(Color.black);
			g.setFont(new Font("default", Font.PLAIN, 14));

			int w = getWidth();
			int h = getHeight();

			double scale_x = (double)(w - 2*PAD)/MAX_X;
			double scale_y = (double)(h - 2*PAD)/MAX_Y;

			double xInc = (double) (w - 2 * PAD) / (MAX_X - 1);
			double scale = (double) (h - 2 * PAD) / MAX_Y;

			// Draw abcissa.
			int tickInc = MAX_X / 10;
			for (int i = 0; i <= MAX_X; i += tickInc) {
				int x = PAD + (int) (i * xInc);
				int y = h - PAD;
				g.drawString(Integer.toString(i), x - 5, y + 20);
				g2.draw(new Line2D.Double(x, y - 5, x, y + 5));
			}

			g2.draw(new Line2D.Double(PAD, h - PAD, w - PAD / 2, h - PAD));
			AffineTransform orig = g2.getTransform();
			g2.rotate(-Math.PI / 2);
			g2.setColor(Color.black);
			g2.drawString("Latitude", -((h + PAD) / 2), PAD / 3);
			g2.setTransform(orig);

			// Draw ordinate.
			tickInc = MAX_Y / 10;

			for (int i = tickInc; i < h - PAD; i += tickInc) {
				int x = PAD;
				int closest_10 = (int)(Math.round((i / scale) / 10) * 10);

				int y = h - PAD - (int) (closest_10 * scale);
				if (y < PAD)
					break;
				String tickMark = Integer.toString(closest_10);
				int stringLen = (int) g2.getFontMetrics()
						.getStringBounds(tickMark, g2).getWidth();
				g.drawString(tickMark, x - stringLen - 8, y + 5);
				g2.draw(new Line2D.Double(x - 5, y, x + 5, y));
			}
			g2.draw(new Line2D.Double(PAD, PAD / 2, PAD, h - PAD));
			g.drawString("Longitude", (w - PAD) / 2, h - PAD + 40);

			for(int i =0; i<nodes.size(); i++){
				Node node = nodes.get(i);
				int posX = PAD + (int)(node.getX()*scale_x);
				int posY = h - PAD - (int)(node.getY()*scale_y);

				// draw links to neighbors
				//Color c = new Color(30, 30, 30, 60);
				//g2.setColor(c);
				//for(Node voisin: node.getConnectedTo()){
				//	if(voisin!=null){
				//		int vPosX = PAD + (int)((voisin.getX())*scale_x);
				//		int vPosY = h - PAD - (int)(voisin.getY()*scale_y);
				//		g2.drawLine(posX, posY, vPosX, vPosY);
				//	}
				//}

				// draw city
				if (nodeColors.size()==nodes.size()){
					g2.setColor(nodeColors.get(i));
					g2.fill(new Ellipse2D.Double(posX-pointSize/2, posY-pointSize/2, pointSize, pointSize));
					//								g.setFont(new Font("TimesRoman", Font.PLAIN, 11));
					//								g.drawString("" + node, posX+10, posY+10);
				}
			}
		}
	}


}
