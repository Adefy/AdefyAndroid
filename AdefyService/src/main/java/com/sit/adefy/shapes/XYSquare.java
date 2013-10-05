package com.sit.adefy.shapes;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

public class XYSquare extends XYRectangle {

    private float length;

    public XYSquare(float length) { super(length, length); }

    public void setLength(float length) {
        this.length = length;
        setWidth(length);
        setHeight(length);
        refreshVertices();
    }

    public float getLength() { return this.length; }
}
