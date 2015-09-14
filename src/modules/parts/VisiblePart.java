package modules.parts;

import java.awt.Graphics2D;
import modules.BaseModule;

/**
 * Base class for visible components on the modules - 
 * LEDs, switches, screens and the like.
 * @author aw12700
 *
 */
public abstract class VisiblePart {

	int x, y;
	public BaseModule owner;
	
	// Interaction
	public boolean lbDown(int x, int y) {return false;}
	public boolean lbUp(int x, int y) {return false;}
	
	// Display
	public abstract void paint(Graphics2D g);
	public abstract void reset();
}
