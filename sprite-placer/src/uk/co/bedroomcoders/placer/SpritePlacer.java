package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;

import com.badlogic.gdx.InputMultiplexer;

import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.OutputStream;

import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import java.lang.Throwable;
import java.lang.Exception;

import javax.script.*;


import javax.swing.JLabel;


public class SpritePlacer implements ApplicationListener { 

	private SpriteBatch batch;
	protected static OrthographicCamera camera;

	public static Pixy selected=null;
    protected static Fixture selectedFixture=null;
    private final static Vector2 tmpV2=new Vector2();

    
    private ShapeRenderer shpBatch; // selection hilight
    public OneBodyRenderer obr;


    private static final Color selCols[] = { Color.RED, Color.GREEN, Color.BLUE,
                                                Color.WHITE, Color.BLACK, Color.YELLOW,
                                                Color.PURPLE }; 
    private int selCol = 0; int physCol = selCols.length/2;
    private int coltick = 0;


    public static World world;

    public static boolean runMode=false;

    protected static ScriptEngineManager scriptMan = new ScriptEngineManager();
    protected static ScriptEngine scriptEng = scriptMan.getEngineByName("JavaScript");
    protected static String levelScript="";
    protected static Invocable scriptInvoker;
    protected static String levelToLoad=null;

    protected static ContactListener scl = new ScriptContactListener();

    public static SpritePlacer engine = null;
    
	@Override
	public void create() {

        scriptInvoker = (Invocable) scriptEng;
        engine = this;
        world = new World(Const.GRAVITY, false);

        world.setContactListener(scl);
        
		// sets up UI controls
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(w,h);
		
		batch = new SpriteBatch();
        shpBatch = new ShapeRenderer();

        UI.initialise();
        clearBodyGui();

		InputMultiplexer multiplexer = new InputMultiplexer();
		//multiplexer.addProcessor(UI.stage);
		multiplexer.addProcessor(Events.handler);
		Gdx.input.setInputProcessor(multiplexer);



        obr = new OneBodyRenderer();     
	}




	@Override
	public void dispose() {
		batch.dispose();
	}
	

    // iterate all pixies making them dump themselves to xml
	protected static void saveLevel(String fname)
	{
		OutputStream os = Gdx.files.local(fname).write(false);
		try 
		{
			os.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<level".getBytes());
//System.out.println("levelScript="+levelScript);
            if (levelScript!=null) {
				String l = levelScript.replace("&","&amp;");
				l = l.replace("\n","&#xA;");
				l = l.replace("\r","");
				l = l.replace("\t","&#x9;");
				l = l.replace(">","&gt;"); // shouldn't need to but....
				l = l.replace("<","&lt;");
				l = l.replace("'","&apos;");
				l = l.replace("\"","&quot;");
				l = " script=\""+l;

                os.write(l.getBytes());
            }
            os.write("\">\n".getBytes());

			Iterator<Pixy> itr = Pixy.getPixies().iterator();
			while(itr.hasNext())
			{
				Pixy p = itr.next();
				os.write(p.toXml().getBytes());
			}
			os.write("</level>\n".getBytes());
			os.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

    // updates the gui controls from a pixy
	protected static void updatePropGui() {
        
        UI.props.Name.setText(selected.getName());
        UI.props.xpos.setText(""+selected.getX());
        UI.props.ypos.setText(""+selected.getY());
        UI.props.Ang.setText(""+selected.getAngle());
        UI.props.Offx.setText(""+selected.getTextureOffsetX());
        UI.props.Offy.setText(""+selected.getTextureOffsetY());
        UI.props.Width.setText(""+selected.getWidth());
        UI.props.Height.setText(""+selected.getHeight());
        UI.props.Twidth.setText(""+selected.getTextureWidth());
        UI.props.Theight.setText(""+selected.getTextureHeight());
        UI.props.Texture.setText(""+selected.getTextureFileName());
        UI.props.Xwrap.setSelectedIndex(selected.getxWrap());
        UI.props.Ywrap.setSelectedIndex(selected.getyWrap());			
        
	}

    protected static void clearPropsGui() {
        
        UI.props.Name.setText("");
        UI.props.xpos.setText("");
        UI.props.ypos.setText("");
        UI.props.Ang.setText("");
        UI.props.Offx.setText("");
        UI.props.Offy.setText("");
        UI.props.Width.setText("");
        UI.props.Height.setText("");
        UI.props.Twidth.setText("");
        UI.props.Theight.setText("");
        UI.props.Texture.setText("");
        UI.props.Xwrap.setSelectedIndex(0);
        UI.props.Ywrap.setSelectedIndex(0);
      
    }



    protected static void updateBodyGui() {
        clearBodyGui();
        if (selected!=null ) {
            if (selected.body!=null) {
                
                Vector2 p=null;
                
                UI.body.BodyType.setVisible(true);
                UI.body.ShapeIndex.setVisible(true);
                
                UI.body.BodyType.setSelectedIndex(SpritePlacer.selected.body.getType().getValue());

                if (UI.body.ShapeIndex.getItemCount()!=selected.body.getFixtureList().size) {
					UI.body.ShapeIndex.removeActionListener(Events.handler);
                    int i=0;
                    UI.body.ShapeIndex.removeAllItems();
                    for(Fixture f : selected.body.getFixtureList()) {
						//System.out.println("i="+i);
						UI.body.ShapeIndex.addItem("Shape "+i);
                        i++;
                    }
                    UI.body.ShapeIndex.addActionListener(Events.handler);
                }
                
                                
                selectedFixture=selected.body.getFixtureList().get(UI.body.ShapeIndex.getSelectedIndex());
                Shape shp = SpritePlacer.selectedFixture.getShape();
                
				if (selectedFixture!=null) {
					if (selectedFixture.isSensor()) {
						UI.body.IsSensor.setSelectedIndex(1);
					} else {
						UI.body.IsSensor.setSelectedIndex(0);
					}
				}
				
                if (shp.getClass() == CircleShape.class) {
                    p=((CircleShape)shp).getPosition();
                    UI.body.ShapeType.setText("Circle");
                    UI.body.Height.setVisible(false);
                    UI.body.radiusLabel.setText("Radius");
                    UI.body.Width.setText(""+(((CircleShape)shp).getRadius()*Const.BOX2WORLD));
                    //System.out.println("set text from getRadius "+(((CircleShape)shp).getRadius()*Const.BOX2WORLD));
                }

                if (shp.getClass() == PolygonShape.class) {
                    p=BoxShape.fauxCast((PolygonShape)shp).getPosition();
                    UI.body.ShapeType.setText("Box");
                    UI.body.Height.setVisible(true);
                    UI.body.radiusLabel.setText("Width");
                    tmpV2.set(BoxShape.fauxCast((PolygonShape)shp).getSize());
                    UI.body.Width.setText(""+(tmpV2.x*Const.BOX2WORLD*2));
                    UI.body.Height.setText(""+(tmpV2.y*Const.BOX2WORLD*2));
                }
                 
                 
                if (p!=null) {
                    UI.body.OffsetX.setText(""+(p.x*Const.BOX2WORLD));
                    UI.body.OffsetY.setText(""+(p.y*Const.BOX2WORLD));
                }
                UI.body.Restitution.setText(""+selectedFixture.getRestitution());
                UI.body.Density.setText(""+selectedFixture.getDensity());
                UI.body.Friction.setText(""+selectedFixture.getFriction());								
                
            }  
        } 
         
    }


    protected static void clearBodyGui() {
        
        UI.body.BodyType.setVisible(false);
        UI.body.ShapeIndex.setVisible(false);
        UI.body.ShapeType.setText("");
        UI.body.OffsetX.setText("");
        UI.body.OffsetY.setText("");
        UI.body.Width.setText("");
        UI.body.Height.setText("");  
    }

    float accumulator;
    Array<Body> bodies = new Array<Body>();
    
	@Override
	public void render() {
		
		if (levelToLoad!=null) { // because swing thread doesn't have the GL context...
			LevelLoader ll = new LevelLoader(levelToLoad);
			if (SpritePlacer.levelScript!=null) {
				try {
					SpritePlacer.scriptEng.put("engine", SpritePlacer.engine);
					SpritePlacer.scriptEng.eval(SpritePlacer.levelScript);
					SpritePlacer.scriptInvoker.invokeFunction("levelLoaded");                                    
				} catch (javax.script.ScriptException ex) {
					System.out.println(ex.getMessage());
				} catch (NoSuchMethodException ex) {
					System.out.println("[Warning] levelLoaded missing");
				}
			}
			UI.script.textArea.setText(SpritePlacer.levelScript);	
			levelToLoad=null;
		}

        try {
            SpritePlacer.scriptInvoker.invokeFunction("beforeRender");                                    
        } catch (Exception e) {
            if (!(e instanceof java.lang.NoSuchMethodException))
                    e.printStackTrace();
        }
        
		Gdx.gl.glClearColor(1, .5f, .25f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);

        if (runMode) {
            float frameTime = Math.min(Gdx.graphics.getDeltaTime(), Const.MAXFRAMETIME);
            accumulator += frameTime;
            while (accumulator >= Const.TIME_STEP) {
                world.step(Const.TIME_STEP, Const.VEL_ITER, Const.POS_ITER);
                accumulator -= Const.TIME_STEP;
            }

            // TODO double check can we get away with only doing this
            // just when run mode first starts?
            world.getBodies(bodies);

            for (Body b : bodies) {
                Pixy p = (Pixy) b.getUserData();
                if (p != null) { p.updateFromBody(b); }
            }  
        }
		
		batch.begin();
		Pixy.drawAll(batch);
		batch.end();

        coltick++;
        if (coltick>4) {
            coltick=0;
            selCol++;physCol++;
            if (selCol==selCols.length) selCol=0;
            if (physCol==selCols.length) physCol=0;
        }
        if (selected!=null) {
            shpBatch.setProjectionMatrix(camera.combined);
            shpBatch.begin(ShapeType.Line);
            shpBatch.setColor(selCols[selCol]);
            // see pixy draw - offset / x,y is done this way to decouple
            // offset from position so it only works with texture...
            shpBatch.rect(selected.getX()-selected.getOriginX(), selected.getY()-selected.getOriginY(),
                        selected.getOriginX(), selected.getOriginY(),
                        selected.getWidth(), selected.getHeight(),
                        1f,1f,
                        selected.getAngle());

            shpBatch.end();
            
            // Overlay the selected objects physics shapes

            if (selected.body!=null) {
                Matrix4 dm=new Matrix4();
                dm.set(camera.combined);
                dm.scale(Const.BOX2WORLD,Const.BOX2WORLD,1f);

                obr.renderOneBody(selected.body, dm, selCols[physCol]);
            }

        }

        try {
            SpritePlacer.scriptInvoker.invokeFunction("afterRender");                                    
        } catch (Exception e) {
            if (!(e instanceof java.lang.NoSuchMethodException))
                    e.printStackTrace();
        }

	}


    // guarantees a unique number when run but may take as much as
    // 1+ MS to return!
    private static long lastUID=-1;
    public static long getUID() {
        long UID=-2;
        while (UID<=lastUID) {
            UID=System.currentTimeMillis();
        }
        lastUID = UID;
        //System.out.println("UID="+UID);
        return UID;
    }


	@Override
	public void resize(int width, int height) {
		// TODO update cam matrix and viewport here
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
