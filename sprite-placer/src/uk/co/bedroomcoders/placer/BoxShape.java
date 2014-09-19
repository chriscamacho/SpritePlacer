package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.utils.Disposable;


import java.util.HashMap;
// just wraps the polygon shape mainly so we can keep track of things
// like width, height and position(offset) 
// setAsBox utilises already allocated memory internally so can be called
// repeatedly, for example when size changes... 

// the class uses a polyshape when creating the fixture then after the
// fixture is created, its shape is put into the cast list
// as the fixture shape is cloned - with the correct instance of
// polygonShape we can then find out BoxShape and get things like
// position and size....

public class BoxShape implements Disposable { // extends PolygonShape {

    private Vector2 size=new Vector2();
    private Vector2 position=new Vector2();
    private float angle;
    private boolean dirty=true;
    protected PolygonShape poly;
    
    // we can only get a PolygonShape from box2d so we use this to
    // "cast" from poly to BoxShape  TODO (better/faster way ???)
    private static HashMap<Shape,BoxShape> shapes = new HashMap<Shape,BoxShape>();

    BoxShape() {
        poly=new PolygonShape(); // temp initial use
    }


    public void dispose() {
        shapes.remove(poly);
        poly.dispose();
    }

    // icky!
    public void putShape(Shape s) {
        shapes.put(s,this); // s from the created fixture
        poly.dispose();     // get rid of the initial shape
        poly=(PolygonShape)s; // store it in the box instance as well as the lookup
    }

    public PolygonShape getShape() {
        return poly;
    }

    public static BoxShape fauxCast(PolygonShape p) {
        return shapes.get(p);
    }

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
        poly.setAsBox(size.x, size.y, position, angle);
        dirty=false;
    }
    
}
