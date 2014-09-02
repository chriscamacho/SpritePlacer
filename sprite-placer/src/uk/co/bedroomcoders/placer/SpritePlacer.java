package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Iterator;

import java.io.OutputStream;
import java.io.BufferedOutputStream;

import uk.co.bedroomcoders.fileDialog.fileDialog;

public class SpritePlacer implements ApplicationListener, EventListener, InputProcessor {

	private SpriteBatch batch;
	private OrthographicCamera camera;
	private TextButton loadButton,saveButton,removeButton,newButton;;
	private TextField nameEd,xEd,yEd,sxEd,syEd,angEd,oxEd,oyEd,wEd,hEd,textureEd,twEd,thEd;
	private SelectBox<String> xwrapEd,ywrapEd;
	private Window win,butWin;
	private ScrollPane sPane;
	private Table propTable;
	public Stage stage;
	private Skin skin;
	private Pixy CurrentPixy=null;

	private String wraps[] = new String[3];

    private fileDialog fd=null;
    private boolean saveMode;

	@Override
	public void create() {
        // provide a textual version of wrap types
        wraps[Texture.TextureWrap.MirroredRepeat.ordinal()]="Mirror";
        wraps[Texture.TextureWrap.Repeat.ordinal()]="Repeat";
        wraps[Texture.TextureWrap.ClampToEdge.ordinal()]="Clamp";
        
		// sets up UI controls
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(w,h);
		
		batch = new SpriteBatch();
		
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(multiplexer);

		butWin = new Window("",skin);
        butWin.pad(2);

		newButton = addButton(butWin,false,"New");
		saveButton = addButton(butWin,true,"Save");
		removeButton = addButton(butWin,false,"Remove");
		loadButton = addButton(butWin,true,"Load");
				
		win = new Window("Properties",skin);
		win.setSize(210,150);
		propTable = new Table(skin);
		propTable.setWidth(200);

		sPane = new ScrollPane(propTable);
		sPane.setSize(210,130); // same size as window minus title height
		sPane.setPosition(0,0);
		win.addActor(sPane);
		
		propTable.add(new Label("drag to scroll",skin)).colspan(2);
		propTable.row();
		nameEd = addTextCell(new TextField("",skin),"Name");
		xEd = addTextCell(new TextField("",skin),"Xpos");
		yEd = addTextCell(new TextField("",skin),"Ypos");
		wEd = addTextCell(new TextField("",skin),"width");
		hEd = addTextCell(new TextField("",skin),"height");
		angEd = addTextCell(new TextField("",skin),"angle");
		oxEd = addTextCell(new TextField("",skin),"offsetX");
		oyEd = addTextCell(new TextField("",skin),"offsetY");
		twEd = addTextCell(new TextField("",skin), "tex width");
		thEd = addTextCell(new TextField("",skin), "tex Height");
		sxEd = addTextCell(new TextField("",skin),"scaleX");
		syEd = addTextCell(new TextField("",skin),"scaleY");
		textureEd = addTextCell(new TextField("",skin),"texure");
        
		xwrapEd = addSelect(new SelectBox<String>(skin), wraps, "Xwrap");
		ywrapEd = addSelect(new SelectBox<String>(skin), wraps, "Ywrap");
				
		stage.addActor(win);
		win.setPosition(8,110);
		stage.addActor(butWin);
		butWin.setPosition(8,8);       
	}

    /* 
     *      convenience functions to add widgets (for properties)
     */
    
	private SelectBox<String> addSelect(SelectBox<String> w, String[] list,String label)
	{
		Label nameLabel = new Label(label, skin);
		propTable.add(nameLabel).width(60);
		propTable.add(w).width(120);
		w.addListener(this);
        w.setItems(list);
		propTable.row();
		return w;
	}

	private TextField addTextCell(TextField w,String label)
	{
		Label nameLabel = new Label(label, skin);
		propTable.add(nameLabel).width(60);
		propTable.add(w).width(120);
		w.addListener(this);
		propTable.row();
		return w;
	}

    /*
     *	add a text button to a table/window
     */
    private TextButton addButton(Table parent, boolean row, String text) {
        TextButton button = new TextButton(text, skin);
        parent.add(button).width(60);
        if (row) parent.row();
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

	// updates pixy properties depending on current UI widget
	private void updateProperty( Event event)
	{
		if (CurrentPixy!=null) { // only if selected
			if (event.getTarget() == nameEd) CurrentPixy.name = nameEd.getText();
			if (event.getTarget() == twEd) CurrentPixy.textureWidth = (int)parseFloatString(twEd,CurrentPixy.textureWidth);
			if (event.getTarget() == thEd) CurrentPixy.textureHeight = (int)parseFloatString(thEd,CurrentPixy.textureHeight);
			if (event.getTarget() == xEd) CurrentPixy.x = parseFloatString(xEd,CurrentPixy.x);
			if (event.getTarget() == yEd) CurrentPixy.y = parseFloatString(yEd,CurrentPixy.y);
			if (event.getTarget() == sxEd) CurrentPixy.scaleX = parseFloatString(sxEd,CurrentPixy.scaleX);
			if (event.getTarget() == syEd) CurrentPixy.scaleY = parseFloatString(syEd,CurrentPixy.scaleY);
			if (event.getTarget() == angEd) CurrentPixy.angle = parseFloatString(angEd,CurrentPixy.angle);
			if (event.getTarget() == wEd) {
				CurrentPixy.width = (int)parseFloatString(wEd,CurrentPixy.width);
				CurrentPixy.originX = CurrentPixy.width / 2;
			}
			if (event.getTarget() == hEd) {
				CurrentPixy.height = (int)parseFloatString(hEd,CurrentPixy.height);
				CurrentPixy.originY = CurrentPixy.height / 2;
			}
			if (event.getTarget() == oxEd) {
				CurrentPixy.textureOffsetX = (int)parseFloatString(oxEd,CurrentPixy.textureOffsetX);
            }
			if (event.getTarget() == oyEd) {
				CurrentPixy.textureOffsetY = (int)parseFloatString(oyEd,CurrentPixy.textureOffsetX);
            }
			if (event.getTarget() == textureEd) {
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
			if (event.getTarget() == xwrapEd)
			{
				int s = xwrapEd.getSelectedIndex();
				if ( s == Texture.TextureWrap.ClampToEdge.ordinal() )
						CurrentPixy.xWrap = Texture.TextureWrap.ClampToEdge.ordinal(); 
				if ( s == Texture.TextureWrap.Repeat.ordinal() )
						CurrentPixy.xWrap = Texture.TextureWrap.Repeat.ordinal(); 
				if ( s == Texture.TextureWrap.MirroredRepeat.ordinal() )
						CurrentPixy.xWrap = Texture.TextureWrap.MirroredRepeat.ordinal(); 
			}
			
			if (event.getTarget() == ywrapEd)
			{
				int s = ywrapEd.getSelectedIndex();
				if ( s == Texture.TextureWrap.ClampToEdge.ordinal() )
						CurrentPixy.yWrap = Texture.TextureWrap.ClampToEdge.ordinal(); 
				if ( s == Texture.TextureWrap.Repeat.ordinal() )
						CurrentPixy.yWrap = Texture.TextureWrap.Repeat.ordinal(); 
				if ( s == Texture.TextureWrap.MirroredRepeat.ordinal() )
						CurrentPixy.yWrap = Texture.TextureWrap.MirroredRepeat.ordinal(); 
			}
			
			if (event.getTarget() == ywrapEd || event.getTarget() == xwrapEd)
			{
				CurrentPixy.texture.setWrap(
						Texture.TextureWrap.values()[CurrentPixy.xWrap],
						Texture.TextureWrap.values()[CurrentPixy.yWrap]
					);
			}
		}
	}

	 
	@Override
	public boolean handle(Event event) 
	{
		
		if (CurrentPixy!=null) {
			if (event.toString().equals("keyUp")) {  // enter key updates pixy property
				if (((InputEvent)event).getKeyCode() == Keys.ENTER) {
					updateProperty(event);
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
                    if (saveMode) { // the ok button on a dialog is either save or load
                        saveLevel(fd.getChosen());
                    } else {
                        LevelLoader ll = new LevelLoader(fd.getChosen());
                    }
                }
                fd=null; // ok done or cancel
            }

            if (event.getTarget() == newButton) { // create a new pixy with default values
				CurrentPixy = new Pixy(0,0,0,0,32,32,1,1,0,"missing.png","new",0,0,32,32);
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
				thEd.setText(""+CurrentPixy.textureWidth);
				twEd.setText(""+CurrentPixy.textureHeight);
				wEd.setText(""+CurrentPixy.width);
				hEd.setText(""+CurrentPixy.height);
				textureEd.setText(CurrentPixy.textureFileName);
				xwrapEd.setSelectedIndex(CurrentPixy.xWrap);
				ywrapEd.setSelectedIndex(CurrentPixy.yWrap);
			}

			if (event.getTarget() == saveButton) {
                fd = new fileDialog("Select file to save", "data/", skin);
                stage.addActor(fd);
                fd.addListener(this);
                saveMode=true;
			}
            
            if (event.getTarget() == loadButton) {
                fd = new fileDialog("Select file to load", "data/", skin);
                stage.addActor(fd);
                fd.addListener(this);
                saveMode=false;
            }
            
			if (event.getTarget()==xwrapEd || event.getTarget()==ywrapEd) {
				updateProperty(event);
            }
			
			if (event.getTarget() == removeButton) {
				if (CurrentPixy!=null) {
					Pixy.pixies.remove(CurrentPixy);
					CurrentPixy=null;
				}
			}
			
			camera.translate(tx,ty,0);
			return true;
		}
				
		if (event.getClass().equals(FocusEvent.class)) {
			updateProperty(event);
			return true;
		}
		
		return false;
	}

    // iterate all pixies making them dump themselves to xml
	private void saveLevel(String fname)
	{
		OutputStream os = Gdx.files.local(fname).write(false);
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
	
	private Vector2 dragStart=new Vector2(),screenDragStart=new Vector2();

    // handles selection and drag start
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
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
		{   // TODO make a list of all selected if currently selected is in 
			// the list find its position and goto the next in the list (or first)
			Pixy p = itr.next();
			tmp.set(cursor);
			if (p.pointIntersects(tmp)) Nearest = p; 
		}	

		if (Nearest!=null)
		{
			CurrentPixy = Nearest;
			updateGui();
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
			twEd.setText("");
			thEd.setText("");
			textureEd.setText("");
			xwrapEd.setSelectedIndex(0);
			ywrapEd.setSelectedIndex(0);
			
			CurrentPixy = null;
		}
		
		dragStart.x=screenX-Gdx.graphics.getWidth()/2;dragStart.y=screenY-Gdx.graphics.getHeight()/2;
		if (CurrentPixy==null) { // drag the screen or selected pixy
			screenDragStart.x=camera.position.x;screenDragStart.y=camera.position.y;
		} else {
			screenDragStart.x=CurrentPixy.x;screenDragStart.y=CurrentPixy.y;
		}
		
		return false;
	}

    // updates the gui controls from a pixy
	private void updateGui() {
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
		twEd.setText(""+CurrentPixy.textureWidth);
		thEd.setText(""+CurrentPixy.textureHeight);
		textureEd.setText(CurrentPixy.textureFileName);
		xwrapEd.setSelectedIndex(CurrentPixy.xWrap);
		ywrapEd.setSelectedIndex(CurrentPixy.yWrap);		
	}

    // update positions because of drag
	Vector2 dragDelta=new Vector2();
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		dragDelta.x=dragStart.x-(screenX-Gdx.graphics.getWidth()/2);dragDelta.y=dragStart.y-(screenY-Gdx.graphics.getHeight()/2);

		if (CurrentPixy==null) {	
			camera.position.x=screenDragStart.x+dragDelta.x;camera.position.y=screenDragStart.y-dragDelta.y;
			camera.update();
		} else {
			CurrentPixy.x=screenDragStart.x-dragDelta.x;CurrentPixy.y=screenDragStart.y+dragDelta.y;
			updateGui();
		}
		
		return false;
	}
	
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
 	{
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
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
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
