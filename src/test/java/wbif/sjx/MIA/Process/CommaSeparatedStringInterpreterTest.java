package wbif.sjx.MIA.Process;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class CommaSeparatedStringInterpreterTest {
    @Test
    public void testInterpretIntegersAscendingPositiveComplete() {
        String string = "1-3";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(string,true);
        int[] expected = new int[]{1,2,3};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretIntegersAscendingNegativeStart() {
        String string = "-2-8";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(string,true);
        int[] expected = new int[]{-2,-1,0,1,2,3,4,5,6,7,8};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretIntegersAscendingNegative() {
        String string = "-5--1";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(string,true);
        int[] expected = new int[]{-5,-4,-3,-2,-1};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretIntegersAscendingPositiveStartNegativeEnd() {
        String string = "5--2";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(string,true);
        int[] expected = new int[]{};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretIntegersAscendingPositiveStartOpenEnd() {
        String string = "5-end";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(string,true);
        int[] expected = new int[]{5,6,Integer.MAX_VALUE};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretIntegersAscendingPositiveGappy() {
        String string = "1-8-3";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(string,true);
        int[] expected = new int[]{1,4,7};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretIntegersAscendingPositiveGappyNegative() {
        String string = "8-1--3";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(string,true);
        int[] expected = new int[]{2,5,8};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretIntegersUnorderedPositiveGappyNegative() {
        String string = "8-1--3";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(string,false);
        int[] expected = new int[]{8,5,2};

        assertArrayEquals(expected,actual);

    }

    @Test
    public void testInterpretIntegersAscendingPositiveStartOpenEndGappy() {
        String string = "5-end-2";

        int[] actual = CommaSeparatedStringInterpreter.interpretIntegers(string,true);
        int[] expected = new int[]{5,7,Integer.MAX_VALUE};

        assertArrayEquals(expected,actual);

    }
}