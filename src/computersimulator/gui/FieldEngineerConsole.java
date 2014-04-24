package computersimulator.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
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
public class FieldEngineerConsole extends JFrame {  
    private JPanel mainPanel;
    private JPanel panel;
    private JPanel buttonPanel;
    
    // Elements of Field EngineerConsole
    private JTextArea memory;               // holds memory inspection 
    private JTextArea cache;                // holds cache inspection
    private JTextArea inspection;           // holds speculative execution inspection 
    
    private JFrame engineerConsole;
    private JButton close;
    
    public FieldEngineerConsole()  {

        mainPanel = new JPanel();
        panel = new JPanel();
        buttonPanel = new JPanel();
        close = new JButton("Close");
        buttonPanel.add(close);

        memory = new JTextArea("Memory Inspection");
        cache = new JTextArea("Cache Inspection");
        cache.setBackground(Color.gray);
        inspection = new JTextArea("Speculative Excecution Inspection");
        
        engineerConsole = new JFrame("Field Engineer Console");
        engineerConsole.setSize(400, 250);
        engineerConsole.add(mainPanel);
        engineerConsole.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        pack();
        engineerConsole.setVisible(true);
        engineerConsole.setLocation(730, 0);
        
        GridLayout layout = new GridLayout(3, 1, 5, 5);
        panel.setLayout(layout);
        panel.add(memory);       
        panel.add(cache);
        panel.add(inspection);

        GridLayout mainLayout = new GridLayout(2, 1, 5, 5);
        mainPanel.setLayout(mainLayout);
        mainPanel.add(panel);
        mainPanel.add(buttonPanel);
        
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                engineerConsole.dispose();
            }
        });
    }
       
    /**
     * @return panel instance
     */ 
    public JComponent getGUI() {
        return panel;
    }    

    /**
     * @return this frame/engineer console
     */
    public JFrame getJFrame() {
        return engineerConsole;
    }
    
    /**
     * @return close button
     */
    public JButton getCloseButton() {
        return close;
    }
}
