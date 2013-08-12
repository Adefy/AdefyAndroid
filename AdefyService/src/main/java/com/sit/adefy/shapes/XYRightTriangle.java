package com.sit.adefy.shapes;

import org.jbox2d.common.Vec2;

public class XYRightTriangle extends BaseShape {

    private float length;
    private float hLength;

    public XYRightTriangle(Vec2 position, float length) {
        super();

        vertices = new float[9];

        setPosition(position);
        setLength(length);
        refreshVertBuffer();
    }

    private void refreshVertices() {
        vertices[0] = -hLength;
        vertices[1] = -hLength;
        vertices[2] = 1.0f;

        vertices[3] = 0;
        vertices[4] = hLength;
        vertices[5] = 1.0f;

        vertices[6] = hLength;
        vertices[7] = -hLength;
        vertices[8] = 1.0f;

        refreshVertBuffer();
    }

    public void setLength(float length) {
        this.length = length;
        this.hLength = length / 2.0f;
        refreshVertices();
    }

    @Override
    public void setPosition(Vec2 position) {
        super.setPosition(position);
        refreshVertices();
    }

    public float getLength() { return length; }
}
