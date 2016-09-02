package me.next.nestedscrollviewdemo;

/**
 * Created by NeXT on 16/9/2.
 */

public class DirectionDetector {
    public static int getDirection(int deltaY, boolean paramBoolean) {
        int i = 0;
        if (deltaY > 0) {
            i = 1;
        }
        if (paramBoolean && deltaY < 0) {
            i = 2;
        }
        return i;
    }
    public int getDirection(int paramInt, boolean paramBoolean, ScrollStateChangedListener paramc) {
        int direction = getDirection(paramInt, paramBoolean);
        if (paramc != null)
            paramc.onChildDirectionChange(direction);
        return direction;
    }
}