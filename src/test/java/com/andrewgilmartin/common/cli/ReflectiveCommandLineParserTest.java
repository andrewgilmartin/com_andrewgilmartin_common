package com.andrewgilmartin.common.cli;

import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ReflectiveCommandLineParserTest {

    @Test
    public void test() {
        Test1 instance = new Test1();
        ReflectiveCommandLineParser parser = new ReflectiveCommandLineParser();
        parser.parse(instance, "--a0 --a1 a --a2 b c d e f".split(" "), 0);
        Assert.assertEquals(true, instance.argument0);
        Assert.assertEquals(1, instance.argument1.length);
        Assert.assertEquals("a", instance.argument1[0]);
        Assert.assertEquals(2, instance.argument2.length);
        Assert.assertEquals("b", instance.argument2[0]);
        Assert.assertEquals("c", instance.argument2[1]);
        Assert.assertEquals(3, instance.position.size());
        Assert.assertEquals("d", instance.position.get(0));
        Assert.assertEquals("e", instance.position.get(1));
        Assert.assertEquals("f", instance.position.get(2));
    }

    static class Test1 {

        boolean argument0 = false;
        String[] argument1 = null;
        String[] argument2 = null;
        String[] argumentN = null;
        List<String> position = null;
        
        public void setA0() {
            this.argument0 = true;
        }

        public void setA1(String a1) {
            this.argument1 = new String[]{a1};
        }

        public void setA2(String a1, String a2) {
            this.argument2 = new String[]{a1, a2};
        }

        public void addPositional(String a) {
            if ( this.position == null ) {
                this.position = new LinkedList<>();
            }
            this.position.add(a);
        }
        
    }

}
