
package computersimulator.components;

/**
 *
 * @author george
 */
public class Word extends Unit {
    
    private final static int WORD_SIZE=20;

    public Word() {
        super(WORD_SIZE);
    }

    public Word(int value) {
        super(WORD_SIZE, value);
    }
    
}
