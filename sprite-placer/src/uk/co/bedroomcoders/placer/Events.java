package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

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
import com.badlogic.gdx.physics.box2d.PolygonShape;

import uk.co.bedroomcoders.fileDialog.fileDialog;

import java.awt.event.KeyListener;
import java.awt.event.FocusListener;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.FocusEvent;
import java.awt.event.ActionEvent;
import java.awt.Component;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.File;

/*
 *  handles events
 *
 *  really part of the SpritePlacer class but moved to its own
 *  class just to make for more managable sized sources
 *
 *  also keeps all the event handling stuff in one place!                      
 */

public class Events implements InputProcessor, KeyListener, FocusListener, ActionListener {

    protected static Events handler;

    private Vector2 tmpV2 = new Vector2();
    private Vector3 tmpV3 = new Vector3();
        
    Events() {

    }
    

	public void keyPressed(KeyEvent keyEvent) {
		//System.out.println("Pressed "+keyEvent);
	}
	
	public void keyReleased(KeyEvent keyEvent) {
		//System.out.println("Released "+keyEvent);
	}
	
	// if enter pressed pretend we lost focus so an update happens
	public void keyTyped(KeyEvent keyEvent) {
		if (keyEvent.getKeyChar() == KeyEvent.VK_ENTER) {
			FocusEvent fe = new FocusEvent((Component)(keyEvent.getSource()),0);
			focusLost(fe);
		}
	}   


	
	public void actionPerformed(ActionEvent e) {
		//System.out.println("e="+e);
		if (SpritePlacer.selected!=null) {
			
			if (e.getSource()==UI.props.Xwrap) SpritePlacer.selected.setxWrap(UI.props.Xwrap.getSelectedIndex()); 
			if (e.getSource()==UI.props.Ywrap) SpritePlacer.selected.setyWrap(UI.props.Ywrap.getSelectedIndex()); 
			if (e.getSource()==UI.props.Texture) {
				UI.setImgFilter();
				int returnVal = UI.fileChooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = UI.fileChooser.getSelectedFile();
					//System.out.println("file="+file.getName());
					UI.props.Texture.setText("data/"+file.getName());
					SpritePlacer.selected.textureDirty = true;
				}
			}
			if (e.getSource()==UI.body.BodyType) {
				SpritePlacer.selected.body.setType(
					BodyDef.BodyType.values()[UI.body.BodyType.getSelectedIndex()]
				);				
			}
			if (e.getSource()==UI.body.ShapeIndex) {
				SpritePlacer.updateBodyGui();
			}
			if (e.getSource()==UI.body.IsSensor) {
				if (SpritePlacer.selectedFixture!=null && SpritePlacer.selected!=null) {
					if (UI.body.IsSensor.getSelectedIndex()==1) SpritePlacer.selectedFixture.setSensor(true);
					if (UI.body.IsSensor.getSelectedIndex()==0) SpritePlacer.selectedFixture.setSensor(false);
				}
			}
			if (e.getSource()==UI.func.Remove) {
				Pixy.getPixies().remove(SpritePlacer.selected);
				SpritePlacer.selected=null;
				SpritePlacer.clearPropsGui();
				SpritePlacer.clearBodyGui();				
			}
			if (e.getSource()==UI.func.Clone) {
				// TODO clone physics as well
				SpritePlacer.doClone=true;
			}
			if (e.getSource()==UI.func.Fixture) {
				String[] buttons = { "Circle", "Box", "Cancel" };
				int rc = JOptionPane.showOptionDialog(null, "Select physics shape to add", "Sprite-Placer",
					JOptionPane.PLAIN_MESSAGE, 0, null, buttons, buttons[2]);

                if (rc==0) {
                    SpritePlacer.selected.addCircleShape();
                    SpritePlacer.updateBodyGui();
                }
                
                if (rc==1) {
                    SpritePlacer.selected.addBoxShape();
                    SpritePlacer.updateBodyGui();
                }
			}
			
		}

		if (e.getSource()==UI.func.Load) {
			UI.setLevelFilter();
			int returnVal = UI.fileChooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = UI.fileChooser.getSelectedFile();
				Pixy.getPixies().clear();
				Array<Body> bodies = new Array<Body>();
				SpritePlacer.world.getBodies(bodies);
				for (Body b : bodies) {
					SpritePlacer.world.destroyBody(b);
					// TODO double check GC will catch BoxShape wrapper
				}
				SpritePlacer.selected=null;
				SpritePlacer.levelScript=null;
				
				SpritePlacer.levelToLoad="data/"+file.getName();			
			}			
		}
		if (e.getSource()==UI.func.Save) {
			UI.setLevelFilter();
			int returnVal = UI.fileChooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = UI.fileChooser.getSelectedFile();
				SpritePlacer.levelScript=UI.script.textArea.getText();
				SpritePlacer.saveLevel("data/"+file.getName());
			}			
		}

		if (e.getSource()==UI.func.Run) {
			SpritePlacer.runMode=true;
			SpritePlacer.levelScript=UI.script.textArea.getText();

			// save position and rotation for restore
			Iterator<Pixy> itr = Pixy.getPixies().iterator();
			while(itr.hasNext()) {
				Pixy p = itr.next();
				p.saveTransform();
			}
			
			// re-evaluate any possibly changed functions
			if (SpritePlacer.levelScript!=null) {
				try {
					SpritePlacer.scriptEng.put("engine", SpritePlacer.engine);
					SpritePlacer.scriptEng.eval(SpritePlacer.levelScript);
				} catch (javax.script.ScriptException ex) {
					System.out.println(ex.getMessage());
				}
			}
			
			UI.script.scriptWindow.setVisible(false);
			UI.body.bodyWindow.setVisible(false);
			UI.props.propsWindow.setVisible(false);
			UI.func.FuncWin.setVisible(false);			
		}

		if (e.getSource()==UI.func.Add) {
			SpritePlacer.selected = new Pixy(0,0,0,0,32,32,0,
										"data/missing.png","new",
										0,0,32,32);
			SpritePlacer.updatePropGui();
			SpritePlacer.updateBodyGui();			
		}

	}

    public void focusGained(FocusEvent e) {
		
    }


	public float textField2float(JTextField tf) {
		float val;
		try {
			val = Float.parseFloat(tf.getText());
		} catch (Exception ex) {
			SpritePlacer.updatePropGui();
			return 0;
		}
		return val;
	}

	// update the actual properties when a UI component looses the focus
	public void focusLost(FocusEvent e) {

		if (SpritePlacer.selected!=null) {

			if (e.getSource()==UI.props.Name) {SpritePlacer.selected.setName(UI.props.Name.getText());return;}
				
			if ((JTextField)e.getSource()!=null) {
				float val;
				try {
					val = Float.parseFloat(((JTextField)(e.getSource())).getText());
				} catch (Exception ex) {
					SpritePlacer.updatePropGui();
					return;
				}
				
				if (e.getSource()==UI.props.xpos) {SpritePlacer.selected.setX(val);return;}
				if (e.getSource()==UI.props.ypos) {SpritePlacer.selected.setY(val);return;}
				if (e.getSource()==UI.props.Ang) {SpritePlacer.selected.setAngle(val);return;}
				if (e.getSource()==UI.props.Offx) {SpritePlacer.selected.setTextureOffsetX((int)val);return;}
				if (e.getSource()==UI.props.Offy) {SpritePlacer.selected.setTextureOffsetY((int)val);return;}
				if (e.getSource()==UI.props.Width) {SpritePlacer.selected.setWidth((int)val);return;}
				if (e.getSource()==UI.props.Height) {SpritePlacer.selected.setHeight((int)val);return;}
				if (e.getSource()==UI.props.Twidth) {SpritePlacer.selected.setTextureWidth((int)val);return;}
				if (e.getSource()==UI.props.Theight) {SpritePlacer.selected.setTextureHeight((int)val);return;}
				
				if (SpritePlacer.selectedFixture!=null) {

					Shape shp = SpritePlacer.selectedFixture.getShape();

					if (e.getSource() == UI.body.OffsetX || e.getSource() == UI.body.OffsetY) {
						tmpV2.set(textField2float(UI.body.OffsetX)*Const.WORLD2BOX,
									textField2float(UI.body.OffsetY)*Const.WORLD2BOX);
						if (shp.getClass() == CircleShape.class) {
							((CircleShape)shp).setPosition(tmpV2);
						}

						if (shp.getClass() == PolygonShape.class) {
							BoxShape bs=BoxShape.fauxCast((PolygonShape)shp);
							bs.setPosition(tmpV2);
							bs.update();
						}
						return;
					}
					
										
					if (e.getSource() == UI.body.Width || e.getSource() == UI.body.Height) {
						tmpV2.set(textField2float(UI.body.Width)*Const.WORLD2BOX/2f,
									textField2float(UI.body.Height)*Const.WORLD2BOX/2f);
						if (shp.getClass() == CircleShape.class) {
							((CircleShape)shp).setRadius(tmpV2.x*2f); // radius not width so undo /2 correction
						}
						if (shp.getClass() == PolygonShape.class) {
							BoxShape bs=BoxShape.fauxCast((PolygonShape)shp);
							bs.setSize(tmpV2);
							bs.update();
						}
						return;
					}

					if (e.getSource() == UI.body.Restitution) {
						SpritePlacer.selectedFixture.setRestitution(textField2float(UI.body.Restitution));
						return;
					}

					if (e.getSource() == UI.body.Friction) {
						SpritePlacer.selectedFixture.setFriction(textField2float(UI.body.Friction));
						return;
					}

					if (e.getSource() == UI.body.Density) {
						SpritePlacer.selectedFixture.setDensity(textField2float(UI.body.Density));
						SpritePlacer.selected.body.resetMassData();
						return;
					}
				}	
			}	
		}
	}
      
    public boolean scrolled(int amount) { return false; }

    public boolean mouseMoved(int x, int y) { return false; }


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
//System.out.println("char="+c);
// WARNING - reminder - gdx bug???
// constantly spews chars if looses and regains focus during keypress...
        return true;
    }

    public boolean keyUp(int keycode) {
//System.out.println("key up"+keycode+" "+Keys.ESCAPE);
		if (keycode==Keys.ESCAPE && SpritePlacer.runMode==true) {

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

			UI.script.scriptWindow.setVisible(true);			
			UI.body.bodyWindow.setVisible(true);
			UI.props.propsWindow.setVisible(true);
			UI.func.FuncWin.setVisible(true);
			SpritePlacer.runMode=false;
		}
        return true;
    }

    public boolean keyDown(int keycode) {

        return true;
    }


}
