package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
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

import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Body;

import uk.co.bedroomcoders.fileDialog.fileDialog;

/*
 *  handles events
 *
 *  really part of the SpritePlacer class but moved to its own
 *  class just to make for more managable sized sources
 *                      
 */

public class Events implements EventListener, InputProcessor {

    protected static Events handler;

    private fileDialog fd=null;
    private Dialog sd;
    private enum dialogModes { LEVLOAD, LEVSAVE, TEXLOAD };
    private dialogModes dialogMode;
    private TextButton butCircle,butBox,butCancel;

    private Vector2 tmpV2 = new Vector2();
    private Vector3 tmpV3 = new Vector3();
        
    Events() {
        butCircle=new TextButton("Circle",UI.skin);
        butBox=new TextButton("Box",UI.skin);
        butCancel=new TextButton("Cancel",UI.skin);
    }

    public boolean handle(Event event) {
        
        //System.out.println(event+" "+event.getTarget().toString());

        FocusEvent FE=null;  // rather than lots of casts...
        InputEvent IE=null;
        ChangeEvent CE=null;
        Actor target=event.getTarget();

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
            if (target == UI.props.texture && FE.isFocused()) {
                fd = new fileDialog("Select texture", "data/", UI.stage, UI.skin, ".\\.jpg|.\\.png" );
                UI.stage.addActor(fd);
                fd.addListener(this);
                dialogMode = dialogModes.TEXLOAD;
            }
        }

        // pressing enter updates an edited property
		if (SpritePlacer.selected!=null) {
            // no event object in keyup method..... need target and
            // whole event for update property
            if (IE!=null) {
                if ( IE.getType()==InputEvent.Type.keyUp ) {  // enter key updates pixy property
                    if (IE.getKeyCode() == Keys.ENTER) {
                        SpritePlacer.updateProperty(event);
                    }
                }
            }
		}

        if (CE!=null) { // deal with change event

            if (target==UI.body.shapeIndex) {
                SpritePlacer.updateBodyGui();
            }

            
            // new shape type selected
            if (sd!=null) {
                if (target==butCircle) {
                    SpritePlacer.selected.addCircleShape();
                    SpritePlacer.updateBodyGui();
                }
                
                if (target==butBox) {
                    SpritePlacer.selected.addBoxShape();
                    SpritePlacer.updateBodyGui();
                }
                
                sd=null;
            }
            
            // file dialog return for save/load level and load texture
            if (fd!=null) {
                if (target == fd.ok) {
                    switch(dialogMode) {
                        case LEVLOAD:
                            Pixy.getPixies().clear();
                            Array<Body> bodies = new Array<Body>();
                            SpritePlacer.world.getBodies(bodies);
                            for (Body b : bodies) {
                                SpritePlacer.world.destroyBody(b);
                                // TODO double check GC will catch BoxShape wrapper
                            }
                            SpritePlacer.selected=null;
                            SpritePlacer.levelScript=null;
                            LevelLoader ll = new LevelLoader(fd.getChosen());

                            if (SpritePlacer.levelScript!=null) {
                                try {
                                    SpritePlacer.scriptEng.put("engine", SpritePlacer.engine);
                                    SpritePlacer.scriptEng.eval(SpritePlacer.levelScript);
                                    SpritePlacer.scriptInvoker.invokeFunction("levelLoaded");                                    
                                } catch (Exception e) {
                                    if (e instanceof javax.script.ScriptException) {
                                        javax.script.ScriptException se = (javax.script.ScriptException)e;
                                        System.out.println(se.getMessage()+" at line "+se.getLineNumber()+","+se.getColumnNumber());
                                        break;
                                    }
                                    if (e instanceof sun.org.mozilla.javascript.EvaluatorException) {
                                        sun.org.mozilla.javascript.EvaluatorException ee = (sun.org.mozilla.javascript.EvaluatorException)e;
                                        System.out.println(ee.getMessage());
                                        break;
                                    }   

                                    e.printStackTrace();
                                    
                                }
                            }
                            break;
                        case LEVSAVE:
                            SpritePlacer.saveLevel(fd.getChosen());
                            break;
                        case TEXLOAD:
                            UI.props.texture.setText(fd.getChosen());
                            event.setTarget(UI.props.texture);
                            SpritePlacer.updateProperty(event);
                            break;
                    } 
                }
                fd=null; 
            }

            if (target == UI.func.run) {
                if (SpritePlacer.runMode) {
                    SpritePlacer.runMode=false;
                    UI.func.run.setText("Run");
                    // restore position and rotation clear velocities and forces
                    Iterator<Pixy> itr = Pixy.getPixies().iterator();
                    while(itr.hasNext()) {
                        Pixy p = itr.next();
                        p.restoreSavedTransform();
                        if (p.body!=null) {
                            p.body.setAngularVelocity(0);
                            p.body.setLinearVelocity(0,0);
                        }
                    }                   
                    SpritePlacer.world.clearForces();
                    UI.body.win.setVisible(true);
                    UI.props.win.setVisible(true);
                } else {
                    SpritePlacer.runMode=true;
                    UI.func.run.setText("Edit");

                    // save position and rotation for restore
                    Iterator<Pixy> itr = Pixy.getPixies().iterator();
                    while(itr.hasNext()) {
                        Pixy p = itr.next();
                        p.saveTransform();
                    }
                    UI.body.win.setVisible(false);
                    UI.props.win.setVisible(false);
                }
            }

            // new pixy
            if (target == UI.func.add) { // create a new pixy with default values
				SpritePlacer.selected = new Pixy(0,0,0,0,32,32,1,1,0,
                                            "data/missing.png","new",
                                            0,0,32,32);
                SpritePlacer.updatePropGui();
                SpritePlacer.updateBodyGui();
			}

            // copy an existing pixy
            if (target == UI.func.clone) {
                if (SpritePlacer.selected!=null) {
                    Pixy c = SpritePlacer.selected;
                    Pixy p = new Pixy(c.getX()+8f,c.getY()+8f,
                                        c.getTextureOffsetX(), c.getTextureOffsetY(),
                                        c.getWidth(),c.getHeight(),c.getScaleX(),c.getScaleY(),
                                        c.getAngle(), c.getTextureFileName(),
                                        c.getName()+"_clone", c.getxWrap(),c.getyWrap(),
                                        c.getTextureWidth(),c.getTextureHeight());
                    SpritePlacer.selected = p;
                    UI.props.x.setText(""+SpritePlacer.selected.getX());
                    UI.props.y.setText(""+SpritePlacer.selected.getY());
                }               
            }

			if (target == UI.func.save) {
                fd = new fileDialog("Select file to save", "data/", UI.stage, UI.skin, ".\\.xml");
                UI.stage.addActor(fd);
                fd.addListener(this);
                dialogMode = dialogModes.LEVSAVE;
			}
            
            if (target == UI.func.load) {
                fd = new fileDialog("Select file to load", "data/", UI.stage, UI.skin, ".\\.xml");
                UI.stage.addActor(fd);
                fd.addListener(this);
                dialogMode = dialogModes.LEVLOAD;
            }

            // adding a new fixture to a body
            if (target == UI.func.fixture && SpritePlacer.selected!=null) {
                sd = new Dialog("Shape type",UI.skin);
                sd.button(butCircle);
                sd.button(butBox);
                sd.button(butCancel);
                sd.getContentTable().add(new Label("Select a shape type",UI.skin));
                sd.pack();
                UI.stage.addActor(sd);
                sd.addListener(this);
                sd.setPosition((UI.stage.getWidth()/2)-(sd.getWidth()/2),
                                (UI.stage.getHeight()/2)-(sd.getHeight()/2));

            }
            
            // texture wrap mode
            if (target == UI.props.xwrap ||
                target == UI.props.ywrap) {
				SpritePlacer.updateProperty(event);
            }

            // remove an existing pixy
			if (target == UI.func.remove) {
				if (SpritePlacer.selected!=null) {
					Pixy.getPixies().remove(SpritePlacer.selected);
					SpritePlacer.selected=null;
                    SpritePlacer.clearPropsGui();
                    SpritePlacer.clearBodyGui();
				}
			}

            if (target == UI.body.bodyType) {
                if (SpritePlacer.selected!=null) {
                    if (SpritePlacer.selected.body!=null) {
                        SpritePlacer.selected.body.setType(
                            BodyDef.BodyType.values()[UI.body.bodyType.getSelectedIndex()]
                            );
                    }
                }
            }
			
			return true;
		}
				
		if (event.getClass().equals(FocusEvent.class)) {
			SpritePlacer.updateProperty(event);
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
        if (SpritePlacer.selected!=null) {
            tmpV3.set((float)x,(float)y,0);
            SpritePlacer.camera.unproject(tmpV3);
            tmpV2.set(tmpV3.x,tmpV3.y);
            if (!SpritePlacer.selected.pointIntersects(tmpV2)) {
                SpritePlacer.selected=null;
                SpritePlacer.clearPropsGui();
                SpritePlacer.clearBodyGui();
                touchDown(x,y,0,0);
                return true;
            }
        }

		dragDelta.x=dragStart.x-(x-Gdx.graphics.getWidth()/2);
        dragDelta.y=dragStart.y-(y-Gdx.graphics.getHeight()/2);

		if (SpritePlacer.selected==null) {	
			SpritePlacer.camera.position.x=screenDragStart.x+dragDelta.x;
            SpritePlacer.camera.position.y=screenDragStart.y-dragDelta.y;
			SpritePlacer.camera.update();
		} else {
			SpritePlacer.selected.setX(screenDragStart.x-dragDelta.x);
            SpritePlacer.selected.setY(screenDragStart.y+dragDelta.y);
			SpritePlacer.updatePropGui();
            SpritePlacer.updateBodyGui();
		}
		
		return true;
    }


    public boolean touchUp(int x, int y, int pointer, int button) {

        // selection including selecting differnet sprites in a stack via
        // repeated selection


        // find all pixies intersecting selection point
        // has to be vector3 for unproject...
		tmpV3.set(x,y,0);
		SpritePlacer.camera.unproject(tmpV3);
		Iterator<Pixy> itr = Pixy.getPixies().iterator();
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
                if (SpritePlacer.selected==sp) {
                    if (si.hasNext()) Sel=si.next();
                }
            }
            if (Sel==null) Sel = stack.iterator().next();
        }

        // if a selection found actually select it and update the gui
        // or select nothing and update the gui
		if (Sel!=null) {
			SpritePlacer.selected = Sel;
			SpritePlacer.updatePropGui();
            SpritePlacer.updateBodyGui();

            try {
                SpritePlacer.scriptInvoker.invokeFunction("selected", Sel );
            } catch (Exception e) {
                e.printStackTrace();
            }

		} else {
			SpritePlacer.selected = null;
            SpritePlacer.clearPropsGui();
            SpritePlacer.clearBodyGui();

		}



		return true;
    }
    
	private Vector2 dragStart=new Vector2(),screenDragStart=new Vector2();

    public boolean touchDown(int x, int y, int pointer, int button) {
		dragStart.x=x-Gdx.graphics.getWidth()/2;
        dragStart.y=y-Gdx.graphics.getHeight()/2;
		if (SpritePlacer.selected==null) { // drag the screen or selected pixy
			screenDragStart.x=SpritePlacer.camera.position.x;
            screenDragStart.y=SpritePlacer.camera.position.y;
		} else {
			screenDragStart.x=SpritePlacer.selected.getX();
            screenDragStart.y=SpritePlacer.selected.getY();
		}


        
		return true;
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
