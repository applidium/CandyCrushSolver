package com.applidium.candycrushsolver;

import com.applidium.candycrushsolver.engine.Sweet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.applidium.candycrushsolver.engine.Sweet.Type.BLUE;
import static com.applidium.candycrushsolver.engine.Sweet.Type.GREEN;
import static com.applidium.candycrushsolver.engine.Sweet.Type.ORANGE;
import static com.applidium.candycrushsolver.engine.Sweet.Type.PURPLE;
import static com.applidium.candycrushsolver.engine.Sweet.Type.RED;
import static com.applidium.candycrushsolver.engine.Sweet.Type.YELLOW;

public class GridFixture {
    static List<List<Sweet.Type>> fixture1() {
        List<List<Sweet.Type>> model = new ArrayList<>();

        //there is an empty row at the beginning (no candy on top of the screen)
        model.add(Collections.<Sweet.Type>emptyList());
        model.add(Arrays.asList(null,   RED,    ORANGE, BLUE,   GREEN,  GREEN,  ORANGE, ORANGE, null));
        model.add(Arrays.asList(null,   ORANGE, RED,    ORANGE, PURPLE, BLUE,   GREEN,  ORANGE, null));
        model.add(Arrays.asList(YELLOW, GREEN,  RED,    GREEN,  RED,    PURPLE, ORANGE, YELLOW, BLUE));
        model.add(Arrays.asList(BLUE,   BLUE,   ORANGE, ORANGE, PURPLE, YELLOW, ORANGE, YELLOW, PURPLE));
        model.add(Arrays.asList(ORANGE, PURPLE, PURPLE, YELLOW, RED,    PURPLE, GREEN,  PURPLE, BLUE));
        model.add(Arrays.asList(PURPLE, GREEN,  ORANGE, ORANGE, GREEN,  YELLOW, GREEN,  GREEN,  YELLOW));
        model.add(Arrays.asList(BLUE,   PURPLE, RED,    RED,    GREEN,  RED,    PURPLE, RED,    YELLOW));
        model.add(Arrays.asList(null,   RED,    GREEN,  YELLOW, RED,    GREEN,  YELLOW, YELLOW, null));
        model.add(Arrays.asList(null,   GREEN,  GREEN,  PURPLE, GREEN,  GREEN,  ORANGE, GREEN,  null));

        return model;
    }

    static List<List<Sweet.Type>> fixture2() {
        List<List<Sweet.Type>> model = new ArrayList<>();

        //there is an empty row at the beginning (no candy on top of the screen)
        model.add(Collections.<Sweet.Type>emptyList());
        model.add(Arrays.asList(RED,    RED,    YELLOW, YELLOW, RED,    ORANGE, YELLOW, BLUE));
        model.add(Arrays.asList(YELLOW, RED,    PURPLE, BLUE,   GREEN,  YELLOW, GREEN,  YELLOW));
        model.add(Arrays.asList(YELLOW, YELLOW, BLUE,   YELLOW, PURPLE, BLUE,   ORANGE, PURPLE));
        model.add(Arrays.asList(ORANGE, BLUE,   PURPLE, BLUE,   PURPLE, ORANGE, YELLOW, GREEN));
        model.add(Arrays.asList(YELLOW, GREEN,  PURPLE, YELLOW, ORANGE, ORANGE, YELLOW, RED));

        return model;
    }

    static List<List<Sweet.Type>> fixture3() {
        List<List<Sweet.Type>> model = new ArrayList<>();

        model.add(Collections.<Sweet.Type>emptyList());
        model.add(Arrays.asList(null,   BLUE,   ORANGE, GREEN,  RED,    GREEN,  null));
        model.add(Arrays.asList(PURPLE, RED,    BLUE,   BLUE,   ORANGE, GREEN,  GREEN));
        model.add(Arrays.asList(GREEN,  PURPLE, PURPLE, ORANGE, BLUE,   RED,    PURPLE));
        model.add(Arrays.asList(PURPLE, RED,    GREEN,  RED,    PURPLE, GREEN,  BLUE));
        model.add(Arrays.asList(ORANGE, RED,    BLUE,   RED,    RED,    ORANGE, GREEN));
        model.add(Arrays.asList(PURPLE, BLUE,   GREEN,  BLUE,   ORANGE, PURPLE, BLUE));
        model.add(Arrays.asList(null,   BLUE,   PURPLE, RED,    PURPLE, RED,    null));

        return model;
    }

    static List<List<Sweet.Type>> fixture4() {
        List<List<Sweet.Type>> model = new ArrayList<>();

        model.add(Collections.<Sweet.Type>emptyList());
        model.add(Arrays.asList(null,   null,   ORANGE, ORANGE, BLUE,   PURPLE, PURPLE, null,   null));
        model.add(Arrays.asList(YELLOW, null,   GREEN,  BLUE,   RED,    BLUE,   PURPLE, null,   RED));
        model.add(Arrays.asList(PURPLE, YELLOW, YELLOW, RED,    ORANGE, ORANGE, GREEN,  RED,    BLUE));
        model.add(Arrays.asList(ORANGE, ORANGE, RED,    PURPLE, GREEN,  ORANGE, YELLOW, RED,    PURPLE));
        model.add(Arrays.asList(RED,    YELLOW, PURPLE, ORANGE, RED,    BLUE,   ORANGE, ORANGE, GREEN));
        model.add(Arrays.asList(BLUE,   PURPLE, GREEN,  PURPLE, GREEN,  ORANGE, RED,    YELLOW, ORANGE));
        model.add(Arrays.asList(YELLOW, ORANGE, GREEN,  ORANGE, PURPLE, BLUE,   YELLOW, GREEN,  RED));
        model.add(Arrays.asList(BLUE,   null,   ORANGE, ORANGE, RED,    ORANGE, PURPLE, null,   ORANGE));
        model.add(Arrays.asList(null,   null,   RED,    GREEN,  BLUE,   YELLOW, RED,    null,   null));

        return model;
    }

    static List<List<Sweet.Type>> fixture5() {
        List<List<Sweet.Type>> model = new ArrayList<>();

        model.add(Collections.<Sweet.Type>emptyList());
        model.add(Arrays.asList(BLUE,   RED,    GREEN,  null,   null,   null,   BLUE,   BLUE,   PURPLE));
        model.add(Arrays.asList(GREEN,  BLUE,   ORANGE, ORANGE, GREEN,  GREEN,  PURPLE, GREEN,  PURPLE));
        model.add(Arrays.asList(BLUE,   RED,    ORANGE, PURPLE, PURPLE, GREEN,  ORANGE, RED,    GREEN));
        model.add(Arrays.asList(null,   PURPLE, RED,    BLUE,   PURPLE, RED,    PURPLE, RED,    null));
        model.add(Arrays.asList(null,   PURPLE, ORANGE, RED,    GREEN,  RED,    ORANGE, ORANGE, null));
        model.add(Arrays.asList(null,   RED,    BLUE,   PURPLE, ORANGE, PURPLE, RED,    PURPLE, null));
        model.add(Arrays.asList(BLUE,   PURPLE, PURPLE, BLUE,   GREEN,  GREEN,  BLUE,   ORANGE, RED));
        model.add(Arrays.asList(GREEN,  ORANGE, RED,    GREEN,  BLUE,   ORANGE, PURPLE, RED,    ORANGE));
        model.add(Arrays.asList(PURPLE, GREEN,  PURPLE, null,   null,   null,   GREEN,  ORANGE, PURPLE));

        return model;
    }
}
