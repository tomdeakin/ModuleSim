package gui;

import java.awt.*;
import javax.swing.JPanel;
import static modules.BaseModule.AvailableModules;

/**
 * The module selection pane
 * @author aw12700
 *
 */
public class ComponentPane extends JPanel {

    private static final long serialVersionUID = 1L;

    public ModuleIcon selected = null;

    public ComponentPane() {
        setLayout(new GridBagLayout());
        addModules();
    }

    /**
     * Adds the modules to the listing
     */
    private void addModules() {
        GridBagConstraints gb = new GridBagConstraints();

        gb.fill = GridBagConstraints.HORIZONTAL;
        gb.weightx = 0.5;
        gb.weighty = 0.0;
        gb.anchor = GridBagConstraints.CENTER;
        gb.gridx = 0;
        gb.gridy = 0;

        for (AvailableModules am : AvailableModules.values()) {
            ModuleIcon icon = new ModuleIcon(am);
            add(icon, gb);
            gb.gridy++;
        }

        gb.weighty = 1.0;
        gb.fill = GridBagConstraints.BOTH;
        gb.ipady = 0;
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        add(p, gb);
    }

    @Override
    public void paintComponent(Graphics oldG) {
        Graphics2D g = (Graphics2D) oldG;

        g.setColor(new Color(255, 255, 255));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

}
