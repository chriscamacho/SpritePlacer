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

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Actor;

import com.badlogic.gdx.scenes.scene2d.Event;
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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import java.lang.Throwable;
import java.lang.Exception;

public class SpritePlacer implements ApplicationListener { 

	private SpriteBatch batch;
	protected static OrthographicCamera camera;

	protected static Pixy selected=null;
    protected static Fixture selectedFixture=null;
    private final static Vector2 tmpV2=new Vector2();

    
    private ShapeRenderer shpBatch; // selection hilight
    private OneBodyRenderer obr;


    private static final Color selCols[] = { Color.RED, Color.GREEN, Color.BLUE,
                                                Color.WHITE, Color.BLACK, Color.YELLOW,
                                                Color.PURPLE }; 
    private int selCol = 0; int physCol = selCols.length/2;
    private int coltick = 0;


    protected static World world;

    protected static boolean runMode=false;
    
	@Override
	public void create() {


        world = new World(Const.GRAVITY, false);
        
		// sets up UI controls
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(w,h);
		
		batch = new SpriteBatch();
        shpBatch = new ShapeRenderer();

        UI.initialise();
        clearBodyGui();

		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(UI.stage);
		multiplexer.addProcessor(Events.handler);
		Gdx.input.setInputProcessor(multiplexer);



        obr = new OneBodyRenderer();     
	}




	@Override
	public void dispose() {
		batch.dispose();
	}
	
	// converts an input string to a float, if the conversion fails the string
	// is replaced with a string of the existing property value.
	protected static float parseFloatString(TextField tf, float v)
	{
		float f;
		try {
			f = Float.parseFloat(tf.getText());
		} catch (Exception e) {
			f = v;
			tf.setText(Float.toString(v));
		}
		return f;
	}

	// updates pixy properties depending on current UI widget
	protected static void updateProperty( Event event)
	{
		if (selected!=null) { // only if selected
            Actor target = event.getTarget();
			if (target == UI.props.name)
                    selected.setName(UI.props.name.getText());
			if (target == UI.props.twidth)
                    selected.setTextureWidth((int)parseFloatString(UI.props.twidth,selected.getTextureWidth()));
			if (target == UI.props.theight)
                    selected.setTextureHeight((int)parseFloatString(UI.props.theight,selected.getTextureHeight()));
			if (target == UI.props.x)
                    selected.setX(parseFloatString(UI.props.x,selected.getX()));
			if (target == UI.props.y)
                    selected.setY(parseFloatString(UI.props.y,selected.getY()));
			if (target == UI.props.sclx)
                    selected.setScaleX(parseFloatString(UI.props.sclx,selected.getScaleX()));
			if (target == UI.props.scly)
                    selected.setScaleY(parseFloatString(UI.props.scly,selected.getScaleY()));
			if (target == UI.props.ang)
                    selected.setAngle(parseFloatString(UI.props.ang,selected.getAngle()));
			if (target == UI.props.width) {
				selected.setWidth((int)parseFloatString(UI.props.width,selected.getWidth()));
				selected.setOriginX(selected.getWidth() / 2);
			}
			if (target == UI.props.height) {
				selected.setHeight((int)parseFloatString(UI.props.height,selected.getHeight()));
				selected.setOriginY(selected.getHeight() / 2);
			}
			if (target == UI.props.offx) {
				selected.setTextureOffsetX((int)parseFloatString(UI.props.offx,selected.getTextureOffsetX()));
            }
			if (target == UI.props.offy) {
				selected.setTextureOffsetY((int)parseFloatString(UI.props.offy,selected.getTextureOffsetX()));
            }
			if (target == UI.props.texture) {
				selected.setTextureFileName(UI.props.texture.getText());
				try
				{
                    selected.setTexture(new Texture(Gdx.files.internal(selected.getTextureFileName())));
				} 
				catch (Exception e)
				{
					selected.setTexture(Pixy.getBrokenTexture());
					UI.props.texture.setText("missing!");
				}
			}
			if (target == UI.props.xwrap)
			{
				int s = UI.props.xwrap.getSelectedIndex();
				if ( s == Texture.TextureWrap.ClampToEdge.ordinal() )
						selected.setxWrap(Texture.TextureWrap.ClampToEdge.ordinal()); 
				if ( s == Texture.TextureWrap.Repeat.ordinal() )
						selected.setxWrap(Texture.TextureWrap.Repeat.ordinal()); 
				if ( s == Texture.TextureWrap.MirroredRepeat.ordinal() )
						selected.setxWrap(Texture.TextureWrap.MirroredRepeat.ordinal()); 
			}
			
			if (target == UI.props.ywrap)
			{
				int s = UI.props.ywrap.getSelectedIndex();
				if ( s == Texture.TextureWrap.ClampToEdge.ordinal() )
						selected.setyWrap(Texture.TextureWrap.ClampToEdge.ordinal()); 
				if ( s == Texture.TextureWrap.Repeat.ordinal() )
						selected.setyWrap(Texture.TextureWrap.Repeat.ordinal()); 
				if ( s == Texture.TextureWrap.MirroredRepeat.ordinal() )
						selected.setyWrap(Texture.TextureWrap.MirroredRepeat.ordinal()); 
			}
			
			if (target == UI.props.xwrap ||
                target == UI.props.ywrap)
			{
				selected.getTexture().setWrap(
						Texture.TextureWrap.values()[selected.getxWrap()],
						Texture.TextureWrap.values()[selected.getyWrap()]
					);
			}

            
            if (target == UI.body.offsetX || target == UI.body.offsetY) {
                tmpV2.set(parseFloatString(UI.body.offsetX,0)*Const.WORLD2BOX,
                            parseFloatString(UI.body.offsetY,0)*Const.WORLD2BOX);
                Shape shp = selectedFixture.getShape();
                if (shp.getClass() == CircleShape.class) {
                    ((CircleShape)shp).setPosition(tmpV2);
                }

                if (shp.getClass() == PolygonShape.class) {
                    BoxShape bs=BoxShape.fauxCast((PolygonShape)shp);
                    bs.setPosition(tmpV2);
                    bs.update();
                }
            }

            if (selectedFixture!=null) {
                Shape shp = selectedFixture.getShape();
                    
                if (target == UI.body.width || target == UI.body.height) {
                    tmpV2.set(parseFloatString(UI.body.width,0)*Const.WORLD2BOX/2f,
                                parseFloatString(UI.body.height,0)*Const.WORLD2BOX/2f);
                    if (shp.getClass() == CircleShape.class) {
                        ((CircleShape)shp).setRadius(tmpV2.x*2f); // radius not width so undo /2 correction
                    }
                    if (shp.getClass() == PolygonShape.class) {
                        BoxShape bs=BoxShape.fauxCast((PolygonShape)shp);
                        bs.setSize(tmpV2);
                        bs.update();
                    }
                }

                if (target == UI.body.restitution) {
                    selectedFixture.setRestitution(parseFloatString(UI.body.restitution,0));
                }

                if (target == UI.body.friction) {
                    selectedFixture.setFriction(parseFloatString(UI.body.friction,0));
                }

                if (target == UI.body.density) {
                    selectedFixture.setDensity(parseFloatString(UI.body.density,0));
                    selected.body.resetMassData();
                }
            }
		}
	}


    // iterate all pixies making them dump themselves to xml
	protected static void saveLevel(String fname)
	{
		OutputStream os = Gdx.files.local(fname).write(false);
		try 
		{
			os.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<level>\n".getBytes());
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
		UI.props.name.setText(selected.getName());
		UI.props.x.setText(""+selected.getX());
		UI.props.y.setText(""+selected.getY());
		UI.props.sclx.setText(""+selected.getScaleX());
		UI.props.scly.setText(""+selected.getScaleY());
		UI.props.ang.setText(""+selected.getAngle());
		UI.props.offx.setText(""+selected.getTextureOffsetX());
		UI.props.offy.setText(""+selected.getTextureOffsetY());
		UI.props.width.setText(""+selected.getWidth());
		UI.props.height.setText(""+selected.getHeight());
		UI.props.twidth.setText(""+selected.getTextureWidth());
		UI.props.theight.setText(""+selected.getTextureHeight());
		UI.props.texture.setText(selected.getTextureFileName());
		UI.props.xwrap.setSelectedIndex(selected.getxWrap());
		UI.props.ywrap.setSelectedIndex(selected.getyWrap());		
	}

    protected static void clearPropsGui() {
        UI.props.name.setText("");
        UI.props.x.setText("");
        UI.props.y.setText("");
        UI.props.sclx.setText("");
        UI.props.scly.setText("");
        UI.props.ang.setText("");
        UI.props.offx.setText("");
        UI.props.offy.setText("");
        UI.props.width.setText("");
        UI.props.height.setText("");
        UI.props.twidth.setText("");
        UI.props.theight.setText("");
        UI.props.texture.setText("");
        UI.props.xwrap.setSelectedIndex(0);
        UI.props.ywrap.setSelectedIndex(0);
    }



    protected static void updateBodyGui() {
        clearBodyGui();
        if (selected!=null ) {
            if (selected.body!=null) {
                UI.body.bodyType.setVisible(true);
                UI.body.shapeIndex.setVisible(true);
                UI.body.bodyType.setSelectedIndex(SpritePlacer.selected.body.getType().getValue());

                if (UI.body.shapeIndex.getItems().size!=selected.body.getFixtureList().size) {
                    int i=0;
                    Array<String> lst=new Array<String>();
                    for(Fixture f : selected.body.getFixtureList()) {
                        lst.add("shape "+i);
                        i++;
                    }
                    UI.body.shapeIndex.setItems(lst); 
                }
                selectedFixture=selected.body.getFixtureList().get(UI.body.shapeIndex.getSelectedIndex());
                String sn=new String();
                
                Vector2 p=null;
                Shape shp = SpritePlacer.selectedFixture.getShape();
                if (shp.getClass() == CircleShape.class) {
                    p=((CircleShape)shp).getPosition();
                    UI.body.shapeType.setText("Circle");
                    UI.body.height.setVisible(false);
                    ((Label)UI.body.width.getUserObject()).setText("Radius");
                    UI.body.width.setText(""+(((CircleShape)shp).getRadius()*Const.BOX2WORLD));
                }

                if (shp.getClass() == PolygonShape.class) {
                    //p=((BoxShape)shp).getPosition();
                    p=BoxShape.fauxCast((PolygonShape)shp).getPosition();
                    UI.body.shapeType.setText("Box");
                    UI.body.height.setVisible(true);
                    ((Label)UI.body.width.getUserObject()).setText("Width");
                    tmpV2.set(BoxShape.fauxCast((PolygonShape)shp).getSize());
                    UI.body.width.setText(""+(tmpV2.x*Const.BOX2WORLD*2));
                    UI.body.height.setText(""+(tmpV2.y*Const.BOX2WORLD*2));
                }
                if (p!=null) {
                    UI.body.offsetX.setText(""+(p.x*Const.BOX2WORLD));
                    UI.body.offsetY.setText(""+(p.y*Const.BOX2WORLD));
                }
                UI.body.restitution.setText(""+selectedFixture.getRestitution());
                UI.body.density.setText(""+selectedFixture.getDensity());
                UI.body.friction.setText(""+selectedFixture.getFriction());
            }  
        } 
         
    }


    protected static void clearBodyGui() {
        UI.body.bodyType.setVisible(false);
        UI.body.shapeIndex.setVisible(false);
        UI.body.shapeType.setText("");
        UI.body.offsetX.setText("");
        UI.body.offsetY.setText("");
        UI.body.width.setText("");
        UI.body.height.setText("");
    }

    float accumulator;
    Array<Body> bodies = new Array<Body>();
    
	@Override
	public void render() {
		Gdx.gl.glClearColor(1, .5f, .25f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);

        if (runMode) {
            float frameTime = Math.min(Gdx.graphics.getDeltaTime(), Const.TIME_STEP);
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
                        selected.getScaleX(), selected.getScaleY(),
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

		UI.stage.act();
		UI.stage.draw();

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
