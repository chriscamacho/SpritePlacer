package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Body;

import uk.co.bedroomcoders.fileDialog.fileDialog;

/*
 *  handles events
 *
 *  removing from main class makes it more maintainable at the expense
 *  of communicating via a bunch of protected members
 *
 *  TODO look at better method of communication between main class and
 *  event handling
 *                      
 */

public class Events implements EventListener, InputProcessor {

    private SpritePlacer SP;
    private fileDialog fd=null;
    private Dialog sd;
    private enum dialogModes { LEVLOAD, LEVSAVE, TEXLOAD };
    private dialogModes dialogMode;
    private TextButton butCircle,butBox,butCancel;

    private Vector2 tmpV2 = new Vector2();
    private Vector3 tmpV3 = new Vector3();
        
    Events(SpritePlacer Parent) {
        SP=Parent;
        butCircle=new TextButton("Circle",SP.skin);
        butBox=new TextButton("Box",SP.skin);
        butCancel=new TextButton("Cancel",SP.skin);
    }

    public boolean handle(Event event) {
        
        //System.out.println(event+" "+event.getTarget().toString());

        FocusEvent FE=null;  // rather than lots of casts...
        InputEvent IE=null;
        ChangeEvent CE=null;

        if (event.getClass().equals(FocusEvent.class)) {
            FE=(FocusEvent)event;
        }
        
        if (event.getClass().equals(InputEvent.class)) {
            IE=(InputEvent)event;
        }
        
        if (event.getClass().equals(ChangeEvent.class)) {
            CE=(ChangeEvent)event;
        }
        
        // selection of texture
        if (FE!=null) {
            // using a textField as if its a button
            // TODO fix!
            if (FE.getTarget() == SP.textureEd && FE.isFocused()) {
                fd = new fileDialog("Select texture", "data/", SP.stage, SP.skin);
                SP.stage.addActor(fd);
                fd.addListener(this);
                dialogMode = dialogModes.TEXLOAD;
            }
        }

        // pressing enter updates an edited property
		if (SP.selected!=null) {
            // no event object in keyup method..... need target and
            // whole event for update property
            if (IE!=null) {
                if ( IE.getType()==InputEvent.Type.keyUp ) {  // enter key updates pixy property
                    if (IE.getKeyCode() == Keys.ENTER) {
                        SP.updateProperty(event);
                    }
                }
            }
		}

        if (CE!=null) { // deal with change event
            // new shape type selected
            if (sd!=null) {
                if (event.getTarget()==butCircle) {
                    Shape shp = new CircleShape();
                    shp.setRadius(32f*Const.WORLD2BOX);
                    FixtureDef fx = new FixtureDef();
                    fx.density=10f;
                    fx.friction=0.5f;
                    fx.restitution=0.5f;
                    fx.shape=shp;
                    if (SP.selected.body==null) {
                        BodyDef bd=new BodyDef();
                        bd.type = BodyDef.BodyType.DynamicBody;
                        SP.selected.body=SP.world.createBody(bd);
                    }
                    SP.selected.body.createFixture(fx);
                }
                sd=null;
            }
            
            // file dialog return for save/load level and load texture
            if (fd!=null) {
                if (event.getTarget() == fd.ok) {
                    switch(dialogMode) {
                        case LEVLOAD:
                            Pixy.pixies.clear();
                            SP.selected=null;
                            LevelLoader ll = new LevelLoader(fd.getChosen());
                            break;
                        case LEVSAVE:
                            SP.saveLevel(fd.getChosen());
                            break;
                        case TEXLOAD:
                            SP.textureEd.setText(fd.getChosen());
                            event.setTarget(SP.textureEd);
                            SP.updateProperty(event);
                            break;
                    } 
                }
                fd=null; 
            }

            // new pixy
            if (event.getTarget() == SP.newButton) { // create a new pixy with default values
				SP.selected = new Pixy(0,0,0,0,32,32,1,1,0,
                                            "missing.png","new",
                                            0,0,32,32);
                SP.updateGui();
			}

            // copy an existing pixy
            if (event.getTarget() == SP.cloneButton) {
                if (SP.selected!=null) {
                    Pixy c = SP.selected;
                    Pixy p = new Pixy(c.x+8f,c.y+8f,
                                        c.textureOffsetX, c.textureOffsetY,
                                        c.width,c.height,c.scaleX,c.scaleY,
                                        c.angle, c.textureFileName,
                                        c.name+"_clone", c.xWrap,c.yWrap,
                                        c.textureWidth,c.textureHeight);
                    SP.selected = p;
                    SP.xEd.setText(""+SP.selected.x);
                    SP.yEd.setText(""+SP.selected.y);
                }               
            }

			if (event.getTarget() == SP.saveButton) {
                fd = new fileDialog("Select file to save", "data/", SP.stage, SP.skin);
                SP.stage.addActor(fd);
                fd.addListener(this);
                dialogMode = dialogModes.LEVSAVE;
			}
            
            if (event.getTarget() == SP.loadButton) {
                fd = new fileDialog("Select file to load", "data/", SP.stage, SP.skin);
                SP.stage.addActor(fd);
                fd.addListener(this);
                dialogMode = dialogModes.LEVLOAD;
            }

            // adding a new fixture to a body
            if (event.getTarget() == SP.fixtButton && SP.selected!=null) {
                sd = new Dialog("Shape type",SP.skin);
                sd.button(butCircle);
                sd.button(butBox);
                sd.button(butCancel);
                sd.getContentTable().add(new Label("Select a shape type",SP.skin));
                sd.pack();
                SP.stage.addActor(sd);
                sd.addListener(this);
                sd.setPosition((SP.stage.getWidth()/2)-(sd.getWidth()/2),
                                (SP.stage.getHeight()/2)-(sd.getHeight()/2));

            }
            
            // texture wrap mode
            if (event.getTarget()==SP.xwrapEd || event.getTarget()==SP.ywrapEd) {
				SP.updateProperty(event);
            }

            // remove an existing pixy
			if (event.getTarget() == SP.removeButton) {
				if (SP.selected!=null) {
					Pixy.pixies.remove(SP.selected);
					SP.selected=null;
                    SP.clearPropsGui();
				}
			}
			
			return true;
		}
				
		if (event.getClass().equals(FocusEvent.class)) {
			SP.updateProperty(event);
			return true;
		}
		
		return true;
    }

    public boolean scrolled(int amount) {

        return false;
    }

    public boolean mouseMoved(int x, int y) {

        return false;
    }


	Vector2 dragDelta=new Vector2();
    public boolean touchDragged(int x, int y, int pointer) {
        // first double check the selection is still valid
        if (SP.selected!=null) {
            tmpV3.set((float)x,(float)y,0);
            SP.camera.unproject(tmpV3);
            tmpV2.set(tmpV3.x,tmpV3.y);
            if (!SP.selected.pointIntersects(tmpV2)) {
                SP.selected=null;
                SP.clearPropsGui();
                touchDown(x,y,0,0);
                return true;
            }
        }

		dragDelta.x=dragStart.x-(x-Gdx.graphics.getWidth()/2);
        dragDelta.y=dragStart.y-(y-Gdx.graphics.getHeight()/2);

		if (SP.selected==null) {	
			SP.camera.position.x=screenDragStart.x+dragDelta.x;
            SP.camera.position.y=screenDragStart.y-dragDelta.y;
			SP.camera.update();
		} else {
			SP.selected.x=screenDragStart.x-dragDelta.x;
            SP.selected.y=screenDragStart.y+dragDelta.y;
			SP.updateGui();
		}
		
		return true;
    }

    // selection including selecting differnet sprites in a stack via
    // repeated selection
    public boolean touchUp(int x, int y, int pointer, int button) {
        // find all pixies intersecting selection point
        // has to be vector3 for unproject...
		tmpV3.set(x,y,0);
		SP.camera.unproject(tmpV3);
		Iterator<Pixy> itr = Pixy.pixies.iterator();
        ArrayList<Pixy> stack = new ArrayList<Pixy>();
		Pixy Sel = null;
		while(itr.hasNext()) {
			Pixy p = itr.next();
			if (p.pointIntersects(tmpV2.set(tmpV3.x,tmpV3.y))) {
                stack.add(p);
            }
		}

        // loop through the stack when you get to the selected item
        // choose the next one if no selected item or end of list choose first
        if (stack.size()!=0) {
            Iterator<Pixy> si = stack.iterator();
            while(si.hasNext()) {
                Pixy sp = si.next();
                if (SP.selected==sp) {
                    if (si.hasNext()) Sel=si.next();
                }
            }
            if (Sel==null) Sel = stack.iterator().next();
        }

        // if a selection found actually select it and update the gui
        // or select nothing and update the gui
		if (Sel!=null) {
			SP.selected = Sel;
			SP.updateGui();
		} else {
            SP.clearPropsGui();
			SP.selected = null;
		}
		return true;
    }
    
	private Vector2 dragStart=new Vector2(),screenDragStart=new Vector2();

    public boolean touchDown(int x, int y, int pointer, int button) {
		dragStart.x=x-Gdx.graphics.getWidth()/2;
        dragStart.y=y-Gdx.graphics.getHeight()/2;
		if (SP.selected==null) { // drag the screen or selected pixy
			screenDragStart.x=SP.camera.position.x;
            screenDragStart.y=SP.camera.position.y;
		} else {
			screenDragStart.x=SP.selected.x;
            screenDragStart.y=SP.selected.y;
		}
		return false;
    }

    public boolean keyTyped(char c) {

        return true;
    }

    public boolean keyUp(int keycode) {

        return true;
    }

    public boolean keyDown(int keycode) {

        return true;
    }


}
