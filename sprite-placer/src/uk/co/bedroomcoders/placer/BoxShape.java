package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.math.Vector2;

// just wraps the polygon shape mainly so we can keep track of things
// like width and height
// setAsBox utilises already allocated memory internally so can be called
// repeatedly, for example when size changes... 
public class BoxShape extends PolygonShape {

    private Vector2 size=new Vector2();
    private Vector2 position=new Vector2();
    private float angle;
    private boolean dirty=true;

    public void setWidth(float Width) { size.x=Width; dirty=true; }
    public void setHeight(float Height) { size.y=Height; dirty=true; }

    public void setSize(float Width, float Height) {
        size.x=Width; size.y=Height; dirty=true;
    }

    public void setSize(final Vector2 sz) {
        size.set(sz); dirty=true;
    }
    
    public void setPosition(final Vector2 Position) { position.set(Position); dirty=true; }
    public void setAngle(float Angle) { angle=Angle; dirty=true; }

    public float getWidth() { return size.x; }
    public float getHeight() { return size.y; }
    public final Vector2 getSize() { return size; }
    public final Vector2 getPosition() { return position; }
    public float getAngle() { return angle; }

    // does update need to be called?
    public boolean isDirty() { return dirty; }

    public void update() {
        setAsBox(size.x, size.y, position, angle);
        dirty=false;
    }
    
}
