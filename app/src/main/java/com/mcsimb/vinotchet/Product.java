package com.mcsimb.vinotchet;

import android.graphics.Bitmap;

class Product {

    public final String wine;
    public final String counter1;
    public final String counter2;
    public final String stamps1;
    public final String stamps2;
    public final Bitmap label;

    Product(String wine, String counter1, String counter2, String stamps1, String stamps2, Bitmap label) {
        this.wine = wine;
        this.counter1 = counter1;
        this.counter2 = counter2;
        this.stamps1 = stamps1;
        this.stamps2 = stamps2;
        this.label = label;
    }
}
