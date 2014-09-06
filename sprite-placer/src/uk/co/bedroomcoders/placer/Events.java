package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.Gdx;
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
    private enum dialogModes { LEVLOAD, LEVSAVE, TEXLOAD };
    private dialogModes dialogMode;

    private Vector2 tmpV2 = new Vector2();
    private Vector3 tmpV3 = new Vector3();
        
    Events(SpritePlacer Parent) {
        SP=Parent;
    }

    public boolean handle(Event event) {
		//System.out.println(event+" "+event.getTarget().toString());

        if (event.getClass().equals(FocusEvent.class)) {
            if (event.getTarget() == SP.textureEd && ((FocusEvent)event).isFocused()) {
                fd = new fileDialog("Select texture", "data/", SP.stage, SP.skin);
                SP.stage.addActor(fd);
                fd.addListener(this);
                dialogMode = dialogModes.TEXLOAD;
            }
        }
        
		if (SP.selected!=null) {
            // no event object in keyup method.....
			if (event.toString().equals("keyUp")) {  // enter key updates pixy property
				if (((InputEvent)event).getKeyCode() == Keys.ENTER) {
					SP.updateProperty(event);
				}
			}
		}

		ChangeEvent ie=null;

        if (event.getClass().equals(ChangeEvent.class)) {  // its a change event!
            ie = (ChangeEvent)event;
        }
		
		float tx=0,ty=0;
		if (ie!=null) { // deal with change event
            if (fd!=null) {
                if (event.getTarget() == fd.ok) {
                    switch(dialogMode) {
                        case LEVLOAD:
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

            if (event.getTarget() == SP.newButton) { // create a new pixy with default values
				SP.selected = new Pixy(0,0,0,0,32,32,1,1,0,
                                            "missing.png","new",
                                            0,0,32,32);
                SP.updateGui();
			}

            if (event.getTarget() == SP.cloneButton) {
                if (SP.selected!=null) {
                    Pixy c = SP.selected;
                    Pixy p = new Pixy(c.x+8f,c.y+8f,c.textureOffsetX,c.textureOffsetY,
                                        c.width,c.height,c.scaleX,c.scaleY,c.angle,
                                        c.textureFileName,c.name+"_clone",
                                        c.xWrap,c.yWrap,c.textureWidth,c.textureHeight);
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
            
			if (event.getTarget()==SP.xwrapEd || event.getTarget()==SP.ywrapEd) {
				SP.updateProperty(event);
            }
			
			if (event.getTarget() == SP.removeButton) {
				if (SP.selected!=null) {
					Pixy.pixies.remove(SP.selected);
					SP.selected=null;
                    SP.clearPropsGui();
				}
			}
			
			SP.camera.translate(tx,ty,0);
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
