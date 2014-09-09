package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.math.Vector2;

// just wraps the polygon shape mainly so we can keep track of things
// line width and height
// setasbox utilises already allocated memory internally so can be called
// repeatedly. 
public class BoxShape extends PolygonShape {

    private float width;
    private float height;
    private Vector2 position=new Vector2();
    private float angle;
    private boolean dirty=true;

    BoxShape() {
        super();
    }

    public void setWidth(float Width) { width=Width; dirty=true; }
    public void setHeight(float Height) { height=Height; dirty=true; }

    public void setSize(float Width, float Height) {
        width=Width;
        height=Height;
        dirty=true;
    }
    
    public void setPosition(final Vector2 Position) { position.set(Position); dirty=true; }
    public void setAngle(float Angle) { angle=Angle; dirty=true; }

    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public Vector2 getPosition() { return position; }
    public float getAngle() { return angle; }

    // does update need to be called?
    public boolean isDirty() { return dirty; }

    public void update() {
        setAsBox(width, height, position, angle);
        dirty=false;
    }
    
}
