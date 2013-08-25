package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.graphics.OrthographicCamera;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;

import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.Iterator;

import java.io.OutputStream;
import java.io.BufferedOutputStream;

public class SpritePlacer implements ApplicationListener, EventListener, InputProcessor {

	private SpriteBatch batch;
	private OrthographicCamera camera;
	private TextButton upButton,downButton,leftButton,rightButton,newButton,saveButton;
	private TextField nameEd,xEd,yEd,sxEd,syEd,angEd,oxEd,oyEd,wEd,hEd,textureEd;
	private Window win,butWin;
	public Stage stage;
	private Skin skin;
	private Pixy CurrentPixy=null;
	
	@Override
	public void create() {		
		
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(w,h);
		
		batch = new SpriteBatch();
		
		// TODO libgdx compatible file dialog - ie no swing on android.		
		LevelLoader ll = new LevelLoader("data/level1.xml");
		ll = null;
		
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		Gdx.input.setInputProcessor(stage);

		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(multiplexer);

		butWin = new Window("",skin);
		butWin.setSize(150,100);

		upButton = addButton(butWin,52,58," Up ");
		leftButton = addButton(butWin,10,33,"Left");
		rightButton = addButton(butWin,95,33,"Right");
		downButton = addButton(butWin,52,8,"Down");
				
		win = new Window("Props",skin);
		win.setSize(200,344);
		
		nameEd = addPropItem(11,"Name");
		nameEd.setWidth(120);
		xEd = addPropItem(10,"Xpos");
		yEd = addPropItem(9,"Ypos");
		wEd = addPropItem(8,"width");
		hEd = addPropItem(7,"height");
		angEd = addPropItem(6,"angle");
		oxEd = addPropItem(5,"offsetX");
		oyEd = addPropItem(4,"offsetY");
		sxEd = addPropItem(3,"scaleX");
		syEd = addPropItem(2,"scaleY");
		textureEd = addPropItem(1,"texture");
		textureEd.setWidth(120);
		
		newButton = addButton(win,4,8,"new.");
		saveButton = addButton(win,52,8,"save");
		
		
		stage.addActor(win);
		win.setPosition(8,110);
		stage.addActor(butWin);
		butWin.setPosition(8,8);
	}

	private TextField addPropItem(int y, String ltxt)
	{
		y = y * 26 + 8;
		Label l = new Label(ltxt,skin);
		win.addActor(l);
		l.setPosition(8,y);
		TextField tf = new TextField("",skin);
		tf.addListener(this);
		win.addActor(tf);
		tf.setPosition(60,y);
		tf.setWidth(60);
		return tf;
	}

	private TextButton addButton(Group parent, int x, int y, String txt)
	{
		TextButton button = new TextButton(txt,skin);
		parent.addActor(button);
		button.setPosition(x,y);
		button.addListener(this);
		return button;
	}

	@Override
	public void dispose() {
		batch.dispose();
	}
	
	// converts an input string to a float, if the conversion fails the string
	// is replaced with a string of the existing property value.
	float parseFloatString(TextField tf, float v)
	{
		float f;
		try
		{
			f = Float.parseFloat(tf.getText());
		}
		catch (Exception e)
		{
			f = v;
			tf.setText(Float.toString(v));
		}
		return f;
	}

	// updates if enter of focus lost
	private void updateProperty( Event event)
	{
		if (event.getTarget() == nameEd) CurrentPixy.name = nameEd.getText();
		if (event.getTarget() == xEd) CurrentPixy.x = parseFloatString(xEd,CurrentPixy.x);
		if (event.getTarget() == yEd) CurrentPixy.y = parseFloatString(yEd,CurrentPixy.y);
		if (event.getTarget() == sxEd) CurrentPixy.scaleX = parseFloatString(sxEd,CurrentPixy.scaleX);
		if (event.getTarget() == syEd) CurrentPixy.scaleY = parseFloatString(syEd,CurrentPixy.scaleY);
		if (event.getTarget() == angEd) CurrentPixy.angle = parseFloatString(angEd,CurrentPixy.angle);
		if (event.getTarget() == wEd)
		{
			CurrentPixy.width = (int)parseFloatString(wEd,CurrentPixy.width);
			CurrentPixy.originX = CurrentPixy.width / 2;
		}
		if (event.getTarget() == hEd) 
		{
			CurrentPixy.height = (int)parseFloatString(hEd,CurrentPixy.height);
			CurrentPixy.originY = CurrentPixy.height / 2;
		}
		if (event.getTarget() == oxEd) 
			CurrentPixy.textureOffsetX = (int)parseFloatString(oxEd,CurrentPixy.textureOffsetX);
		if (event.getTarget() == oyEd) 
			CurrentPixy.textureOffsetY = (int)parseFloatString(oyEd,CurrentPixy.textureOffsetX);
		if (event.getTarget() == textureEd)
		{
			CurrentPixy.textureFileName = textureEd.getText();
			try
			{
				CurrentPixy.texture = new Texture(Gdx.files.internal("data/"+CurrentPixy.textureFileName));
			} 
			catch (Exception e)
			{
				CurrentPixy.texture = Pixy.brokenTexture;
				textureEd.setText("missing!");
			}
		}

	}
	
	
	// TODO can events be handled in just one place (so you know where they
	// all are) without having to rely on runtime cast failure...
	 
	@Override
	public boolean handle(Event event) 
	{
		if (CurrentPixy!=null) {
			if (event.toString().equals("keyUp"))
			{
				if (((InputEvent)event).getKeyCode() == Keys.ENTER)
				{
					updateProperty(event);
				}
			}
		}

		ChangeEvent ie=null;
		try{
			ie = (ChangeEvent)event; // change events toString behaves differently...
		} 
		catch(Exception e) 
		{
			// cast exception - so its not a change event
		}
		
		float tx=0,ty=0;
		if (ie!=null) // ie its only a changeEvent ? - TODO clicked event ??
		{
			if (event.getTarget() == newButton) 
			{
				CurrentPixy = new Pixy(0,0,0,0,32,32,1,1,0,"missing.png","new");
				// setting gui done is 2 places should really only be done in one place
				// and called in 2 places....
				nameEd.setText(CurrentPixy.name);
				xEd.setText(""+CurrentPixy.x);
				yEd.setText(""+CurrentPixy.y);
				sxEd.setText(""+CurrentPixy.scaleX);
				syEd.setText(""+CurrentPixy.scaleY);
				angEd.setText(""+CurrentPixy.angle);
				oxEd.setText(""+CurrentPixy.textureOffsetX);
				oyEd.setText(""+CurrentPixy.textureOffsetY);
				wEd.setText(""+CurrentPixy.width);
				hEd.setText(""+CurrentPixy.height);
				textureEd.setText(CurrentPixy.textureFileName);
			}
			// TODO prefs variable step amount
			if (event.getTarget() == downButton) ty=-16;
			if (event.getTarget() == upButton) ty=16;
			if (event.getTarget() == leftButton) tx=-16;
			if (event.getTarget() == rightButton) tx=16;
			if (event.getTarget() == saveButton)
			{
				saveLevel();
			}
			camera.translate(tx,ty,0);
			return true;
		}
				
		FocusEvent fe=null;
		try
		{
			fe=(FocusEvent)event;
		} 
		catch(Exception e) {  }
		
		if (fe!=null)
		{
			updateProperty(event);
			return true;
		}
		
		return false;
	}
	
	private void saveLevel()
	{
		OutputStream os = Gdx.files.local("data/level1.xml").write(false);
		try 
		{
			os.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<level>\n".getBytes());
			Iterator<Pixy> itr = Pixy.pixies.iterator();
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
	
	public boolean scrolled(int amount) 
	{
		return false;
	}
	
	public boolean mouseMoved(int screenX, int screenY)
	{
		return false;
	}
	
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}
	
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		return false;
	}
	
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
 	{
		// TODO whats the best way to change selection if same coord clicked 
		// multiple times where there are multiple pixies in same place.
		Vector3 cursor = new Vector3(screenX,screenY,0);
		camera.unproject(cursor);
		Vector3 tmp = new Vector3();
		Iterator<Pixy> itr = Pixy.pixies.iterator();
		float dist = 1000000.0f;
		Pixy Nearest = null;
		while(itr.hasNext())
		{
			Pixy p = itr.next();
			tmp.set(cursor);
			if (p.pointIntersects(tmp)) Nearest = p; 
		}	

		if (Nearest!=null)
		{
			nameEd.setText(Nearest.name);
			xEd.setText(""+Nearest.x);
			yEd.setText(""+Nearest.y);
			sxEd.setText(""+Nearest.scaleX);
			syEd.setText(""+Nearest.scaleY);
			angEd.setText(""+Nearest.angle);
			oxEd.setText(""+Nearest.textureOffsetX);
			oyEd.setText(""+Nearest.textureOffsetY);
			wEd.setText(""+Nearest.width);
			hEd.setText(""+Nearest.height);
			textureEd.setText(Nearest.textureFileName);

			CurrentPixy = Nearest;
		} else {
			nameEd.setText("");
			xEd.setText("");
			yEd.setText("");
			sxEd.setText("");
			syEd.setText("");
			angEd.setText("");
			oxEd.setText("");
			oyEd.setText("");
			wEd.setText("");
			hEd.setText("");
			textureEd.setText("");
			
			CurrentPixy = null;
		}
	
		return true;
	}

	public boolean keyUp(int keycode)
	{
		return false;
	}
	
	public boolean keyDown(int keycode)
	{
		return false;
	}
	
	public boolean keyTyped(char character)
	{
		return false;
	}
	
	@Override
	public void render() {
		Gdx.gl.glClearColor(1, .5f, .25f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		
		batch.begin();
		Pixy.drawAll(batch);
		batch.end();

		stage.act();
		stage.draw();
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
