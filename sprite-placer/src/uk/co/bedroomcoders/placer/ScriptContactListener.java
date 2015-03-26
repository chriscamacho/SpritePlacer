package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.Input.Keys;

public class ScriptContactListener implements ContactListener {

    public void beginContact(Contact contact) {

        // TODO when initialising level determine if function exists
        // cache to a boolean and test before this try
        try {
            SpritePlacer.scriptInvoker.invokeFunction("beginContact", contact);                                    
        } catch (Exception e) {
            if (!(e instanceof java.lang.NoSuchMethodException)) {
                    e.printStackTrace();
					if (SpritePlacer.runMode) SpritePlacer.stopForError=true;
			}
        }
    }

    public void endContact(Contact contact) {
        try {
            SpritePlacer.scriptInvoker.invokeFunction("endContact", contact);                                    
        } catch (Exception e) {
            if (!(e instanceof java.lang.NoSuchMethodException)) {
                    e.printStackTrace();
					if (SpritePlacer.runMode) SpritePlacer.stopForError=true;
			}
        }
    }

    public void postSolve(Contact contact, ContactImpulse impulse) {
        try {
            SpritePlacer.scriptInvoker.invokeFunction("postSolve", contact, impulse);                                    
        } catch (Exception e) {
            if (!(e instanceof java.lang.NoSuchMethodException)) {
                    e.printStackTrace();
					if (SpritePlacer.runMode) SpritePlacer.stopForError=true;
			}
        }
    }

    public void preSolve(Contact contact, Manifold oldManifold) {
        try {
            SpritePlacer.scriptInvoker.invokeFunction("preSolve", contact, oldManifold);                                    
        } catch (Exception e) {
            if (!(e instanceof java.lang.NoSuchMethodException)) {
                    e.printStackTrace();
					if (SpritePlacer.runMode) SpritePlacer.stopForError=true;
			}
        }
    }


}
