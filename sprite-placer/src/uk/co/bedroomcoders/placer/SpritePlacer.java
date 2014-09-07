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

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.InputMultiplexer;

import java.util.ArrayList;
import java.util.Iterator;

import java.io.OutputStream;

import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;

public class SpritePlacer implements ApplicationListener { 

	private SpriteBatch batch;
	protected OrthographicCamera camera;
	protected TextButton loadButton,saveButton,removeButton,newButton;
    protected TextButton fixtButton,cloneButton;
	protected TextField nameEd,xEd,yEd,sxEd,syEd,angEd,oxEd,oyEd,wEd,hEd;
    protected TextField textureEd,twEd,thEd;
	protected SelectBox<String> xwrapEd,ywrapEd;
	private Window win,butWin,fixtWin;
	private ScrollPane sPane;
	private Table propTable;
	protected Stage stage;
	protected Skin skin;
	protected Pixy selected=null;
    private ShapeRenderer shpBatch; // selection hilight

	private String wraps[] = new String[3];
    private static final Color selCols[] = { Color.RED, Color.GREEN, Color.BLUE,
                                                Color.WHITE, Color.BLACK, Color.YELLOW,
                                                Color.PURPLE }; 
    private int selCol = 0; int physCol = selCols.length/2;
    private int coltick = 0;

    private Events handler;
    protected World world;
    
	@Override
	public void create() {

        world = new World(Const.GRAVITY, false);
        // provide a textual version of wrap types
        wraps[Texture.TextureWrap.MirroredRepeat.ordinal()]="Mirror";
        wraps[Texture.TextureWrap.Repeat.ordinal()]="Repeat";
        wraps[Texture.TextureWrap.ClampToEdge.ordinal()]="Clamp";
        
		// sets up UI controls
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(w,h);
		
		batch = new SpriteBatch();
        shpBatch = new ShapeRenderer();
		
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		handler = new Events(this);
		stage = new Stage();

		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(handler);
		Gdx.input.setInputProcessor(multiplexer);

		butWin = new Window("Functions",skin);
        //butWin.pad(6);
        //butWin.padTop(24);

		newButton = addButton(butWin,false,"New");
        cloneButton = addButton(butWin,false,"Clone");
		removeButton = addButton(butWin,true,"Remove");
		loadButton = addButton(butWin,false,"Load");
		saveButton = addButton(butWin,true,"Save");
        fixtButton = addButton(butWin,false,"+Shape");
        butWin.pack();
				
		win = new Window("Properties",skin);
        win.setWidth(190);
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
        w.addListener(handler);
        w.setItems(list);
		propTable.row();
		return w;
	}

	private TextField addTextCell(TextField w,String label)
	{
		Label nameLabel = new Label(label, skin);
		propTable.add(nameLabel).width(60);
		propTable.add(w).width(120);
        w.addListener(handler);
		propTable.row();
		return w;
	}

    /*
     *	add a text button to a table/window used for the function button window
     */
    private TextButton addButton(Table parent, boolean row, String text) {
        TextButton button = new TextButton(text, skin);
        parent.add(button).width(60);
        if (row) parent.row();
        button.addListener(handler);
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
		try {
			f = Float.parseFloat(tf.getText());
		} catch (Exception e) {
			f = v;
			tf.setText(Float.toString(v));
		}
		return f;
	}

	// updates pixy properties depending on current UI widget
	protected void updateProperty( Event event)
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
                    selected.texture = new Texture(Gdx.files.internal(selected.textureFileName));
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

    // iterate all pixies making them dump themselves to xml
	protected void saveLevel(String fname)
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

    // updates the gui controls from a pixy
	protected void updateGui() {
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
        if (coltick>6) {
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
            shpBatch.rect(selected.x-selected.originX, selected.y-selected.originY,
                        selected.originX, selected.originY,
                        selected.width, selected.height,
                        selected.scaleX, selected.scaleY,
                        selected.angle);

            // Overlay the selected objects physics shapes


            shpBatch.end();

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
