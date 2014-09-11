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
            if (FE.getTarget() == UI.props.texture && FE.isFocused()) {
                fd = new fileDialog("Select texture", "data/", UI.stage, UI.skin);
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
            // new shape type selected
            if (sd!=null) {
                if (event.getTarget()==butCircle) {
                    CircleShape shp = new CircleShape();
                    shp.setRadius(16f*Const.WORLD2BOX);
                    FixtureDef fx = new FixtureDef();
                    fx.density=10f; fx.friction=0.5f;
                    fx.restitution=0.5f; fx.shape=shp;
                    shp.setPosition(new Vector2(16f*Const.WORLD2BOX,16f*Const.WORLD2BOX));
                    if (SpritePlacer.selected.body==null) {
                        BodyDef bd=new BodyDef();
                        bd.type = BodyDef.BodyType.DynamicBody;
                        SpritePlacer.selected.body=SpritePlacer.world.createBody(bd);
                        SpritePlacer.selected.body.setTransform(SpritePlacer.selected.getX()*Const.WORLD2BOX,
                                                        SpritePlacer.selected.getY()*Const.WORLD2BOX,SpritePlacer.selected.getAngle()*Const.PI180);
                        SpritePlacer.selected.body.setUserData(SpritePlacer.selected);
                    }
                    SpritePlacer.selected.body.createFixture(fx);
                }
                
                if (event.getTarget()==butBox) {
                    // NB must use update after changing one or more BoxShape
                    // properties via their setters...
                    BoxShape shp = new BoxShape();
                    FixtureDef fx = new FixtureDef();
                    fx.density=10f; fx.friction=0.5f;
                    fx.restitution=0.5f;
                    shp.setSize(16f*Const.WORLD2BOX,8f*Const.WORLD2BOX);
                    shp.setPosition(new Vector2(-16f*Const.WORLD2BOX,16f*Const.WORLD2BOX));
                    shp.setAngle(0);
                    shp.update(); fx.shape=shp;
                    if (SpritePlacer.selected.body==null) {
                        BodyDef bd=new BodyDef();
                        bd.type = BodyDef.BodyType.DynamicBody;
                        SpritePlacer.selected.body=SpritePlacer.world.createBody(bd);
                        SpritePlacer.selected.body.setTransform(SpritePlacer.selected.getX()*Const.WORLD2BOX,
                                                        SpritePlacer.selected.getY()*Const.WORLD2BOX,SpritePlacer.selected.getAngle()*Const.PI180);
                        SpritePlacer.selected.body.setUserData(SpritePlacer.selected);
                    }
                    SpritePlacer.selected.body.createFixture(fx);
                }
                
                sd=null;
            }
            
            // file dialog return for save/load level and load texture
            if (fd!=null) {
                if (event.getTarget() == fd.ok) {
                    switch(dialogMode) {
                        case LEVLOAD:
                            Pixy.getPixies().clear();
                            SpritePlacer.selected=null;
                            LevelLoader ll = new LevelLoader(fd.getChosen());
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

            // new pixy
            if (event.getTarget() == UI.func.add) { // create a new pixy with default values
				SpritePlacer.selected = new Pixy(0,0,0,0,32,32,1,1,0,
                                            "missing.png","new",
                                            0,0,32,32);
                SpritePlacer.updatePropGui();
			}

            // copy an existing pixy
            if (event.getTarget() == UI.func.clone) {
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

			if (event.getTarget() == UI.func.save) {
                fd = new fileDialog("Select file to save", "data/", UI.stage, UI.skin);
                UI.stage.addActor(fd);
                fd.addListener(this);
                dialogMode = dialogModes.LEVSAVE;
			}
            
            if (event.getTarget() == UI.func.load) {
                fd = new fileDialog("Select file to load", "data/", UI.stage, UI.skin);
                UI.stage.addActor(fd);
                fd.addListener(this);
                dialogMode = dialogModes.LEVLOAD;
            }

            // adding a new fixture to a body
            if (event.getTarget() == UI.func.fixture && SpritePlacer.selected!=null) {
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
            if (event.getTarget()==UI.props.xwrap ||
                event.getTarget()==UI.props.ywrap) {
				SpritePlacer.updateProperty(event);
            }

            // remove an existing pixy
			if (event.getTarget() == UI.func.remove) {
				if (SpritePlacer.selected!=null) {
					Pixy.getPixies().remove(SpritePlacer.selected);
					SpritePlacer.selected=null;
                    SpritePlacer.clearPropsGui();
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
		}
		
		return true;
    }

    // selection including selecting differnet sprites in a stack via
    // repeated selection
    public boolean touchUp(int x, int y, int pointer, int button) {
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
		} else {
            SpritePlacer.clearPropsGui();
			SpritePlacer.selected = null;
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
