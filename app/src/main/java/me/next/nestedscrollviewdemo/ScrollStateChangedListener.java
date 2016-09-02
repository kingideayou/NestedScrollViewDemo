package me.next.nestedscrollviewdemo;


public interface ScrollStateChangedListener {
    void onChildDirectionChange(int position);

    void onChildPositionChange(ScrollState param);

    enum ScrollState {TOP, BOTTOM, MIDDLE, NO_SCROLL}
}