package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Process.CommaSeparatedStringInterpreter;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ExtractSubstackTest extends ModuleTest {

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ExtractSubstack(null).getDescription());
    }

    @Test
    public void testInterpretRangeSingleSingle() {
        String inputRange = "1";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{1};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeMultipleSingleInOrder() {
        String inputRange = "1,3,6";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{1,3,6};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeMultipleSingleOutOfOrder() {
        String inputRange = "1,6,3";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{1,3,6};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeSingleRangeAscending() {
        String inputRange = "4-8";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{4,5,6,7,8};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeSingleRangeSame() {
        String inputRange = "4-4";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{4};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeSingleRangeDescending() {
        String inputRange = "8-4";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{4,5,6,7,8};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeMultipleRangeAscending() {
        String inputRange = "4-8,12-15";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{4,5,6,7,8,12,13,14,15};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeMultipleRangeAscendingDescending() {
        String inputRange = "4-8,15-12";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{4,5,6,7,8,12,13,14,15};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeIntervalRangeAscending() {
        String inputRange = "4-8-2";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{4,6,8};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeIntervalRangeAscendingNoneToFind() {
        String inputRange = "4-5-2";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{4};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeMixed() {
        String inputRange = "4-8,18,12-15,20-32-3";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{4,5,6,7,8,12,13,14,15,18,20,23,26,29,32};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeSingleRangeEnd() {
        String inputRange = "4-end";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{4,5,Integer.MAX_VALUE};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeSingleRangeEndMixed() {
        String inputRange = "2-4,6,10-end";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{2,3,4,6,10,11,Integer.MAX_VALUE};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeIntervalRangeEnd() {
        String inputRange = "4-end-3";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{4,7,Integer.MAX_VALUE};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeIntervalRangeEndMixed() {
        String inputRange = "2-4,12-end-3";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{2,3,4,12,15,Integer.MAX_VALUE};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretRangeIntervalRangeEndMixedWhitespace() {
        String inputRange = "2 - 4 , 12 - end - 3";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(inputRange,true);
        int[] expected = new int[]{2,3,4,12,15,Integer.MAX_VALUE};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testExpandRangeToEnd() {
        int[] inputRange = new int[]{2,3,4,12,15,Integer.MAX_VALUE};

        int[] actual = ExtractSubstack.extendRangeToEnd(inputRange,20);
        int[] expected = new int[]{2,3,4,12,15,18};

        assertArrayEquals(expected,actual);

    }
}