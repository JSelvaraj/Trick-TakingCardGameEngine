package test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import build.Example;

public class ExampleTest {

    Example e = new Example();

    @Test
    void testAdd() {
        assertEquals(e.add(1, 1), 2);
    }
}
