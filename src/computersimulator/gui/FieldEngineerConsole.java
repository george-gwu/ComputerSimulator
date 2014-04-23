package computersimulator.gui;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * FieldEngineerConsole - Extra debug information. Design is up to us.
 * Suggestions: Display contents of internal registers within our simulated CPU. 
 * These are registers the operator doesn't need to see, but could be useful in
 * debugging.
 */
public class FieldEngineerConsole {
    //@TODO: Mocking Optional engineer console in later parts.   
    private JPanel p;
    
    // Elements of Field EngineerConsole
    private JTextArea memory;
    
    private JFrame engineerConsole;
    
    public FieldEngineerConsole() {
        p = new JPanel();
        memory = new JTextArea("Memory");
        p.add(memory);
        
        engineerConsole = new JFrame("Field Engineer Console");
        engineerConsole.setSize(400, 400);
        engineerConsole.add(p);
        engineerConsole.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        engineerConsole.setVisible(true);
    }
        
    /**
     * @return panel instance
     */ 
    public JComponent getGUI() {
        return p;
    }    

    /**
     * @return this frame/engineer console
     */
    public JFrame getJFrame() {
        return engineerConsole;
    }
}
