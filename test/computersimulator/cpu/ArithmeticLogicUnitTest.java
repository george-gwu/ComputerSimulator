package computersimulator.cpu;

import computersimulator.components.Unit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pawel
 */
public class ArithmeticLogicUnitTest {
    private ArithmeticLogicUnit alu;
    
    public ArithmeticLogicUnitTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        alu = new ArithmeticLogicUnit();
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testAdd() {
        Unit u1 = new Unit(5, 1);
        Unit u2 = new Unit(5, 1); 
        Unit u3 = alu.add(u1, u2);
        u3.getBinaryString();
        assertEquals("00010", u3.getBinaryString());
    }
    
    @Test
    public void testSubtraction() {
        Unit u1 = new Unit(5, 17);
        Unit u2 = new Unit(5, 14); 
        Unit u3 = alu.subtract(u1, u2);
        u3.getBinaryString();
        assertEquals("00011", u3.getBinaryString());
    }
    
    @Test
    public void testAddBinary() {
        
    }
}
