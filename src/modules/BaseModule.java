package modules;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import modules.parts.*;
import org.w3c.dom.Element;
import simulator.*;
import tools.DeleteOperation;
import util.BinData;
import util.Vec2;

/**
 * Base for module classes
 * @author aw12700
 *
 */
public abstract class BaseModule extends PickableEntity {

    public enum rotationDir {
        ROT_CW,
        ROT_CCW,
        ROT_180
    }

    public double w = 30, h = 30;
    public AffineTransform toWorld = new AffineTransform();
    public AffineTransform toView = new AffineTransform();

    public int orientation = 0;

    public List<Port> ports = new ArrayList<Port>();
    public List<Input> inputs = new ArrayList<Input>();
    public List<Output> outputs = new ArrayList<Output>();
    public List<VisiblePart> parts = new ArrayList<VisiblePart>();

    public int ID;
    
    public abstract AvailableModules getModType();
    
    public boolean error = false;

    /**
     * Get the object's ID
     * @return The ID
     */
    public int getID() {
        return ID;
    }
    
    /**
     * Allows action to be taken after connection
     */
    public void onConnect() {}

//    /**
//     * Instantiates the module.
//     */
//    public abstract BaseModule createModule();

    /**
     * Adds an output
     */
    public Output addOutput(String name, int pos, int type) {
        Output o = new Output();
        o.text = name;
        o.pos = pos;
        o.type = type;
        o.owner = this;

        outputs.add(o);
        ports.add(o);
        return o;
    }

    /**
     * Adds an input
     */
    public Input addInput(String name, int pos, int type) {
        return addInput(name, pos, type, new BinData(0));
    }

    public Input addInput(String name, int pos, int type, BinData pullVal) {
        Input i = new Input();
        i.text = name;
        i.pos = pos;
        i.type = type;
        i.owner = this;
        i.pull = pullVal;


        inputs.add(i);
        ports.add(i);
        return i;
    }
    
    /**
     * Returns ports affected by changes to the given input
     * Should be overwritten by subclasses to improve loop detector accuracy.
     */
    public List<Port> getAffected(Port in) {
        return ports;
    }
    
    
    /**
     * Adds a part
     */
    public void addPart(VisiblePart p) {
        parts.add(p);
        p.owner = this;
    }

    /**
     * Displays the module in local space
     * @param g
     */
    public abstract void paint(Graphics2D g);

    /**
     * Displays the module's bounding box
     */
    @Override
    public void drawBounds(Graphics2D g) {
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(2));
        g.drawRect((int)(- w/2) - 2, (int)(- h/2) - 2, (int)w + 2, (int)h + 2);
    }

    /**
     * Draws the visible parts
     */
    protected void drawParts(Graphics2D g) {
        for (VisiblePart p : parts) {
            p.paint(g);
        }
    }

    /**
     * Draws the outputs as arrows
     */
    protected void drawOutputs(Graphics2D g) {
        g.setStroke(new BasicStroke(2));

        for (Output o : outputs) {
            boolean side = (o.type == Port.CTRL || o.type == Port.CLK);

            int aw = 10;
            int offset = o.pos;

            if (side) offset = - offset;

            int[] aPoints = {-aw + offset, -aw + offset, aw + offset, aw + offset, offset};

            // Base offset
            int base, angle;
            if (!side) {
                base = -(int)h/2;
                angle = 0;
            }
            else {
                base = -(int)w/2;
                angle = 90;
            }

            int[] bPoints = {base+aw, base, base, base+aw, base};

            // Draw internal shape
            if (!side)
                g.fillPolygon(aPoints, bPoints, 5);
            else
                g.fillPolygon(bPoints, aPoints, 5);

            Color oldC = g.getColor();

            if (o.type == Port.GENERIC)
                g.setColor(Color.GRAY);
            else if (o.type == Port.CTRL)
                g.setColor(Color.BLUE);
            else if (o.type == Port.CLK)
                g.setColor(new Color(100, 160, 100));
            else if (o.type == Port.DATA)
                g.setColor(Color.RED);

            if (!side)
                g.fillArc(offset-5, base-5, 10, 10, angle, 180);
            else
                g.fillArc(base - 5, offset - 5, 10, 10, angle, 180);

            g.setColor(oldC);
        }
    }

    /**
     * Draws the inputs as arrows
     */
    protected void drawInputs(Graphics2D g) {
        g.setStroke(new BasicStroke(2));

        // Loop the inputs
        for (Input i : inputs) {
            boolean side = (i.type == Port.CTRL || i.type == Port.CLK);

            int aw = 10;
            int offset = i.pos;

            if (side) offset = - offset;

            // Base offset
            int base, angle;
            if (!side) {
                base = (int)h/2;
                angle = 180;
            }
            else {
                base = (int)w/2;
                angle = 270;
            }
            
            int[] aPoints;
            int[] bPoints;
            int num;
            if (i.bidir) {
                aPoints = new int[] {-aw + offset, -aw + offset, aw + offset, aw + offset, offset};
                bPoints = new int[] {base-aw, base, base, base-aw, base};
                num = 5;
            }
            else {
                aPoints = new int[]{-aw + offset, aw + offset, offset};
                bPoints = new int[]{base, base, base-aw};
                num = 3;
            }
            
            // Draw internal shape
            if (!side)
                g.fillPolygon(aPoints, bPoints, num);
            else
                g.fillPolygon(bPoints, aPoints, num);

            Color oldC = g.getColor();

            if (i.type == Port.GENERIC)
                g.setColor(Color.GRAY);
            else if (i.type == Port.CTRL)
                g.setColor(Color.BLUE);
            else if (i.type == Port.CLK)
                g.setColor(new Color(100, 160, 100));
            else if (i.type == Port.DATA)
                g.setColor(Color.RED);

            int x = offset-5;
            int y = base - 5;
            if (side) {
                int temp = x;
                x = y;
                y = temp;
            }
            
            if (i.bidir)
                g.fillArc(x, y, 10, 10, angle, 180);
            else
                g.drawArc(x, y, 10, 10, angle, 180);

            g.setColor(oldC);
        }
    }

    /**
     * Draws the module as a trapezoid
     */
    protected void drawTrapezoid(Graphics2D g, int corner) {
        drawTrapezoid(g, corner, 0, 0, (int) w, (int) h);
    }

    /**
     * Draws a trapezoid with the specified dimensions
     */
    protected void drawTrapezoid(Graphics2D g, int corner, int x, int y, int w, int h) {
        int[] xPoints = {x-w/2, x+w/2, x+w/2,        x+w/2-corner, x-w/2+corner, x-w/2};
        int[] yPoints = {y+h/2, y+h/2, y-h/2+corner, y-h/2,        y-h/2,        y-h/2 + corner};
        g.fillPolygon(xPoints, yPoints, 6);
    }

    /**
     * Draws the module as a box
     */
    protected void drawBox(Graphics2D g, int corner) {
        int iw = (int)w, ih = (int)h;
        int[] xPoints = {-iw/2, -iw/2+corner, iw/2-corner, iw/2, iw/2, iw/2-corner, -iw/2+corner, -iw/2};
        int[] yPoints = { ih/2-corner, ih/2, ih/2, ih/2-corner, -ih/2+corner, -ih/2, -ih/2, -ih/2+corner};
        g.fillPolygon(xPoints, yPoints, 8);
    }

    /**
     * Rotates the module
     * @param cw Whether to rotate clockwise (false for ccw)
     */
    public final void rotate(rotationDir dir) {
        switch (dir) {
            case ROT_CW:
                orientation = (orientation + 1) % 4;
                break;
            case ROT_CCW:
                orientation = (orientation - 1) % 4;
                break;
            case ROT_180:
                orientation = (orientation + 2) % 4;
                break;
        }
    }

    /**
     * Updates the object's transformation
     */
    public void updateXForm() {
        snapToGrid();

        toWorld = new AffineTransform();
        toWorld.translate(pos.x, pos.y);
        toWorld.rotate((Math.PI / 2) * orientation);

        toView = new AffineTransform(Main.ui.view.wToV);
        toView.concatenate(toWorld);

        // Update links
        for (Output o : outputs) {
            if (o.link != null) o.link.updatePath();
        }
        for (Input i : inputs) {
            if (i.link != null) i.link.updatePath();
        }
    }

    /**
     * Generates on-grid coords
     */
    public void snapToGrid() {
        pos.x = Math.round(pos.x / Main.sim.grid) * Main.sim.grid;
        pos.y = Math.round(pos.y / Main.sim.grid) * Main.sim.grid;
    }

    /**
     * Transforms a point from object to world-space
     * @param p Point
     * @return Transformed point
     */
    public Vec2 objToWorld(Vec2 p) {
        double[] pt = p.asArray();

        toWorld.transform(pt, 0, pt, 0, 1);

        return new Vec2(pt);
    }

    /**
     * Removes a module from the sim. DOES NOT affect its links.
     * Creates a new delete operation.
     */
    @Override
    public void delete() {
        Main.sim.removeEntity(this);
        Main.ui.view.deselect(this);

        Main.ui.view.opStack.pushOp(new DeleteOperation(this));
    }

    /**
     * Handles user interaction through parts
     * @param ix X coord in view space
     * @param iy Y coord in view space
     * @return Whether the input was handled
     */
    public boolean lbDown(int ix, int iy) {
        if (!enabled) return false;

        // Coords in object space
        double[] pt = {ix, iy};
        try {toView.inverseTransform(pt, 0, pt, 0, 1);}
        catch (Exception e) {e.printStackTrace();}

        int dx = (int)pt[0];
        int dy = (int)pt[1];

        synchronized (parts) {
            for (VisiblePart p : parts) {
                if (p.lbDown(dx, dy)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Handles user interaction through parts
     * @param ix X coord in view space
     * @param iy Y coord in view space
     * @return Whether the input was handled
     */
    public boolean lbUp(int ix, int iy) {
        if (!enabled) return false;

        // Coords in object space
        double[] pt = {ix, iy};
        try {toView.inverseTransform(pt, 0, pt, 0, 1);}
        catch (Exception e) {e.printStackTrace();}

        int dx = (int)pt[0];
        int dy = (int)pt[1];

        synchronized (parts) {
            for (VisiblePart p : parts) {
                if (p.lbUp(dx, dy)) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Thread-safe reset
     */
    public void doReset() {
        synchronized(this) {
            reset();
            for (VisiblePart p : parts) {
                p.reset();
            }
        }
    }

    @Override
    public boolean within(double x, double y, double x2, double y2) {
        double[] rect = {x, y, x2, y2};

        // Get clicked point in object space
        try {toWorld.inverseTransform(rect, 0, rect, 0, 2);}
        catch (Exception e) {
            System.err.println("Non inversible transform");
        }

        x  = Math.min(rect[0], rect[2]);
        y  = Math.min(rect[1], rect[3]);
        x2 = Math.max(rect[0], rect[2]);
        y2 = Math.max(rect[1], rect[3]);

        if (    x < - w/2 && x2 > w/2 &&
                y < - h/2 && y2 > h/2 )
            return true;
        else return false;
    }

    @Override
    public boolean intersects(Vec2 pt) {
        double[] dpt = pt.asArray();

        // Get clicked point in object space
        try {toWorld.inverseTransform(dpt, 0, dpt, 0, 1);}
        catch (Exception e) {
            System.err.println("Non inversible transform");
        }

        double nx = dpt[0];
        double ny = dpt[1];

        if (    nx > - w/2 && nx < w/2 &&
                ny > - h/2 && ny < h/2 ) {
            return true;
        }
        else {
            return false;
        }
    }
    
    @Override
    public int getType() {
        return PickableEntity.MODULE;
    }

    /**
     * Updates the module's outputs based on its inputs
     * (Needs override)
     */
    public abstract void propagate();

    /**
     * Resets to initial simulation state (needs override)
     */
    protected abstract void reset();
    
    /**
     * Run tests on the module
     * @return True if tests ran successfully
     */
    public boolean test() {return true;}

    /**
     * Initialize with a loaded XML Data Element (module-specific implementation)
     * Called by XMLReader. Default behaviour is no-op.
     * @param dataElem
     */
    public void dataIn(Element dataElem) {}

    /**
     * Fill an XML data element with module-specific data for retrieval with dataIn.
     * Called by XMLWriter. Default behaviour is to return false, indicating the element was not modified.
     * @param dataElem
     */
    public boolean dataOut(Element dataElem) { return false; }

    public enum AvailableModules {
        // Enum members should not be renamed!
        ADDSUB(new AddSub(), "Arithmetic Unit"),
        CLOCK(new Clock(), "Clock"),
        DEMUX(new Demux(), "Demultiplexor"),
        FANOUT(new Fanout(), "Fanout"),
        LOGIC(new Logic(), "Logic Unit"),
        MUX(new Mux(), "Multiplexor"),
        OR(new Or(), "OR"),
        RAM(new RAM(true), "RAM"),
        REGISTER(new Register(), "Register"),
        LEFT_SHIFT(new Shift(true), "Left-shift"),
        RIGHT_SHIFT(new Shift(false), "Right-shift"),
        SPLIT_MERGE(new SplitMerge(), "Splitter / Merger"),
        SWITCH(new SwitchInput(), "Switch Input");
        
        /**
         * The module represented by this enum value, to use to instantiate and display in GUI.
         */
        private final BaseModule module;
        private final String name;
        
        private AvailableModules(BaseModule mod, String name) {
            this.module = mod;
            this.name = name;
        }
        
        public BaseModule getSrcModule() {
            return module;
        }
        
        @Override
        public String toString() {
            return name;
        }
        
        public static AvailableModules fromModule(BaseModule mod) throws IllegalArgumentException {
            for (AvailableModules am : values()) {
                if (am.module.getClass().equals(mod.getClass())) {
                    return am;
                }
            }
            
            throw new IllegalArgumentException("Module of type " + mod.getClass() + " is not available!");
        }
    }
    
}
