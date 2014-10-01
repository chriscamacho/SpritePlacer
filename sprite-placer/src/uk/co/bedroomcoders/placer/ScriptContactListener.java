package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Manifold;

public class ScriptContactListener implements ContactListener {

    public void beginContact(Contact contact) {

        // TODO when initialising level determine if function exists
        // cache to a boolean and test before this try
        try {
            SpritePlacer.scriptInvoker.invokeFunction("beginContact", contact);                                    
        } catch (Exception e) {
            if (!(e instanceof java.lang.NoSuchMethodException))
                    e.printStackTrace();
        }
    }

    // TODO fill in these!
    public void endContact(Contact contact) {

    }

    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    public void preSolve(Contact contact, Manifold oldManifold) {

    }


}
