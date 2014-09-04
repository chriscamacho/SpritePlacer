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
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

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
	private TextButton loadButton,saveButton,removeButton,newButton,cloneButton;
	private TextField nameEd,xEd,yEd,sxEd,syEd,angEd,oxEd,oyEd,wEd,hEd,textureEd,twEd,thEd;
	private SelectBox<String> xwrapEd,ywrapEd;
	private Window win,butWin;
	private ScrollPane sPane;
	private Table propTable;
	public Stage stage;
	private Skin skin;
	private Pixy selected=null;
    private ShapeRenderer shapes; // selection hilight

	private String wraps[] = new String[3];

    private fileDialog fd=null;
    private boolean saveMode;

    private static final Color selCols[] = { Color.RED, Color.GREEN, Color.BLUE,
                                                Color.WHITE, Color.BLACK, Color.YELLOW,
                                                Color.PURPLE }; 
    private int selCol = 0;
    private int coltick = 0;

    private Vector2 tmpV2 = new Vector2();
    private Vector3 tmpV3 = new Vector3();
    
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
        shapes = new ShapeRenderer();
		
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(multiplexer);

		butWin = new Window("Functions",skin);
        butWin.pad(6);
        butWin.padTop(24);

		newButton = addButton(butWin,false,"New");
        cloneButton = addButton(butWin,false,"Clone");
		removeButton = addButton(butWin,true,"Remove");
		loadButton = addButton(butWin,false,"Load");
		saveButton = addButton(butWin,false,"Save");
        butWin.pack();
				
		win = new Window("Properties",skin);
        win.setWidth(160);
		win.setResizeBorder(8);
        propTable = new Table(skin);
		sPane = new ScrollPane(propTable);
        sPane.setFillParent(true);
        win.add(sPane).fill().expand();
		
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
        // TODO uses fileDialog for texture selection
		textureEd = addTextCell(new TextField("",skin),"texure");
        
		xwrapEd = addSelect(new SelectBox<String>(skin), wraps, "Xwrap");
		ywrapEd = addSelect(new SelectBox<String>(skin), wraps, "Ywrap");

        propTable.padTop(24);
        win.setResizable(true);
        		
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
		if (selected!=null) { // only if selected
			if (event.getTarget() == nameEd) selected.name = nameEd.getText();
			if (event.getTarget() == twEd) selected.textureWidth = (int)parseFloatString(twEd,selected.textureWidth);
			if (event.getTarget() == thEd) selected.textureHeight = (int)parseFloatString(thEd,selected.textureHeight);
			if (event.getTarget() == xEd) selected.x = parseFloatString(xEd,selected.x);
			if (event.getTarget() == yEd) selected.y = parseFloatString(yEd,selected.y);
			if (event.getTarget() == sxEd) selected.scaleX = parseFloatString(sxEd,selected.scaleX);
			if (event.getTarget() == syEd) selected.scaleY = parseFloatString(syEd,selected.scaleY);
			if (event.getTarget() == angEd) selected.angle = parseFloatString(angEd,selected.angle);
			if (event.getTarget() == wEd) {
				selected.width = (int)parseFloatString(wEd,selected.width);
				selected.originX = selected.width / 2;
			}
			if (event.getTarget() == hEd) {
				selected.height = (int)parseFloatString(hEd,selected.height);
				selected.originY = selected.height / 2;
			}
			if (event.getTarget() == oxEd) {
				selected.textureOffsetX = (int)parseFloatString(oxEd,selected.textureOffsetX);
            }
			if (event.getTarget() == oyEd) {
				selected.textureOffsetY = (int)parseFloatString(oyEd,selected.textureOffsetX);
            }
			if (event.getTarget() == textureEd) {
				selected.textureFileName = textureEd.getText();
				try
				{
					selected.texture = new Texture(Gdx.files.internal("data/"+selected.textureFileName));
				} 
				catch (Exception e)
				{
					selected.texture = Pixy.brokenTexture;
					textureEd.setText("missing!");
				}
			}
			if (event.getTarget() == xwrapEd)
			{
				int s = xwrapEd.getSelectedIndex();
				if ( s == Texture.TextureWrap.ClampToEdge.ordinal() )
						selected.xWrap = Texture.TextureWrap.ClampToEdge.ordinal(); 
				if ( s == Texture.TextureWrap.Repeat.ordinal() )
						selected.xWrap = Texture.TextureWrap.Repeat.ordinal(); 
				if ( s == Texture.TextureWrap.MirroredRepeat.ordinal() )
						selected.xWrap = Texture.TextureWrap.MirroredRepeat.ordinal(); 
			}
			
			if (event.getTarget() == ywrapEd)
			{
				int s = ywrapEd.getSelectedIndex();
				if ( s == Texture.TextureWrap.ClampToEdge.ordinal() )
						selected.yWrap = Texture.TextureWrap.ClampToEdge.ordinal(); 
				if ( s == Texture.TextureWrap.Repeat.ordinal() )
						selected.yWrap = Texture.TextureWrap.Repeat.ordinal(); 
				if ( s == Texture.TextureWrap.MirroredRepeat.ordinal() )
						selected.yWrap = Texture.TextureWrap.MirroredRepeat.ordinal(); 
			}
			
			if (event.getTarget() == ywrapEd || event.getTarget() == xwrapEd)
			{
				selected.texture.setWrap(
						Texture.TextureWrap.values()[selected.xWrap],
						Texture.TextureWrap.values()[selected.yWrap]
					);
			}
		}
	}

	 
	@Override
	public boolean handle(Event event) 
	{
		
		if (selected!=null) {
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
				selected = new Pixy(0,0,0,0,32,32,1,1,0,"missing.png","new",0,0,32,32);
				// setting gui done is 2 places should really only be done in one place
				// and called in 2 places....
				nameEd.setText(selected.name);
				xEd.setText(""+selected.x);
				yEd.setText(""+selected.y);
				sxEd.setText(""+selected.scaleX);
				syEd.setText(""+selected.scaleY);
				angEd.setText(""+selected.angle);
				oxEd.setText(""+selected.textureOffsetX);
				oyEd.setText(""+selected.textureOffsetY);
				thEd.setText(""+selected.textureWidth);
				twEd.setText(""+selected.textureHeight);
				wEd.setText(""+selected.width);
				hEd.setText(""+selected.height);
				textureEd.setText(selected.textureFileName);
				xwrapEd.setSelectedIndex(selected.xWrap);
				ywrapEd.setSelectedIndex(selected.yWrap);
			}

            if (event.getTarget() == cloneButton) {
                if (selected!=null) {
                    Pixy c = selected;
                    Pixy p = new Pixy(c.x+8f,c.y+8f,c.textureOffsetX,c.textureOffsetY,
                                        c.width,c.height,c.scaleX,c.scaleY,c.angle,
                                        c.textureFileName,c.name+"_clone",
                                        c.xWrap,c.yWrap,c.textureWidth,c.textureHeight);
                    selected = p;
                    xEd.setText(""+selected.x);
                    yEd.setText(""+selected.y);
                }               
            }

			if (event.getTarget() == saveButton) {
                fd = new fileDialog("Select file to save", "data/", stage, skin);
                stage.addActor(fd);
                fd.addListener(this);
                saveMode=true;
			}
            
            if (event.getTarget() == loadButton) {
                fd = new fileDialog("Select file to load", "data/", stage, skin);
                stage.addActor(fd);
                fd.addListener(this);
                saveMode=false;
            }
            
			if (event.getTarget()==xwrapEd || event.getTarget()==ywrapEd) {
				updateProperty(event);
            }
			
			if (event.getTarget() == removeButton) {
				if (selected!=null) {
					Pixy.pixies.remove(selected);
					selected=null;
                    clearPropsGui();
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

    // handles drag start
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{

        // first double check the selection is still valid
        if (!selected.pointIntersects(tmpV2.set((float)screenX,(float)screenY))) {
            selected=null;
            clearPropsGui();
        }

		dragStart.x=screenX-Gdx.graphics.getWidth()/2;dragStart.y=screenY-Gdx.graphics.getHeight()/2;
		if (selected==null) { // drag the screen or selected pixy
			screenDragStart.x=camera.position.x;screenDragStart.y=camera.position.y;
		} else {
			screenDragStart.x=selected.x;screenDragStart.y=selected.y;
		}
		
		return false;
	}

    // updates the gui controls from a pixy
	private void updateGui() {
		nameEd.setText(selected.name);
		xEd.setText(""+selected.x);
		yEd.setText(""+selected.y);
		sxEd.setText(""+selected.scaleX);
		syEd.setText(""+selected.scaleY);
		angEd.setText(""+selected.angle);
		oxEd.setText(""+selected.textureOffsetX);
		oyEd.setText(""+selected.textureOffsetY);
		wEd.setText(""+selected.width);
		hEd.setText(""+selected.height);
		twEd.setText(""+selected.textureWidth);
		thEd.setText(""+selected.textureHeight);
		textureEd.setText(selected.textureFileName);
		xwrapEd.setSelectedIndex(selected.xWrap);
		ywrapEd.setSelectedIndex(selected.yWrap);		
	}

    // update positions because of drag
	Vector2 dragDelta=new Vector2();
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		dragDelta.x=dragStart.x-(screenX-Gdx.graphics.getWidth()/2);dragDelta.y=dragStart.y-(screenY-Gdx.graphics.getHeight()/2);

		if (selected==null) {	
			camera.position.x=screenDragStart.x+dragDelta.x;camera.position.y=screenDragStart.y-dragDelta.y;
			camera.update();
		} else {
			selected.x=screenDragStart.x-dragDelta.x;selected.y=screenDragStart.y+dragDelta.y;
			updateGui();
		}
		
		return false;
	}

    // selection including selecting differnet sprites in a stack via
    // repeated selection
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
 	{
        // find all pixies intersecting selection point
        // has to be vector3 for unproject...
		final Vector3 cursor = new Vector3();
        cursor.set(screenX,screenY,0);
		camera.unproject(cursor);
		Iterator<Pixy> itr = Pixy.pixies.iterator();
        ArrayList<Pixy> stack = new ArrayList<Pixy>();
		Pixy Sel = null;
		while(itr.hasNext()) {
			Pixy p = itr.next();
			if (p.pointIntersects(tmpV2.set(cursor.x,cursor.y))) {
                stack.add(p);
            }
		}

        // loop through the stack when you get to the selected item
        // choose the next one if no selected item or end of list choose first
        if (stack.size()!=0) {
            Iterator<Pixy> si = stack.iterator();
            while(si.hasNext()) {
                Pixy sp = si.next();
                if (selected==sp) {
                    if (si.hasNext()) Sel=si.next();
                }
            }
            if (Sel==null) Sel = stack.iterator().next();
        }

        // if a selection found actually select it and update the gui
        // or select nothing and update the gui
		if (Sel!=null) {
			selected = Sel;
			updateGui();
		} else {
            clearPropsGui();
			selected = null;
		}
		return true;
	}

    public void clearPropsGui() {
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

        coltick++;
        if (coltick>5) {
            coltick=0;
            selCol++;
            if (selCol==selCols.length) selCol=0;
        }
        if (selected!=null) {
            shapes.setProjectionMatrix(camera.combined);
            shapes.begin(ShapeType.Line);
            shapes.setColor(selCols[selCol]);
            // see pixy draw - offset / x,y is done this way to decouple
            // offset from position so it only works with texture...
            shapes.rect(selected.x-selected.originX, selected.y-selected.originY,
                        selected.originX, selected.originY,
                        selected.width, selected.height,
                        selected.scaleX, selected.scaleY,
                        selected.angle);
            shapes.end();
        }

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
