package uk.co.bedroomcoders.placer;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.physics.box2d.BodyDef;
import java.util.Arrays;

import com.badlogic.gdx.math.Vector2;

import javax.swing.*;
import java.awt.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

// "STATIC" class for holding UI items and intitalising them

public class UI {

    public static String wraps[] = new String[3];
    private static String bodyTypes[] = new String[3];
    private static JLabel lastLabel;

	static String p = new java.io.File("").getAbsolutePath();
	static FileSystemView fsv = new DirectoryRestrictedFileSystemView(new File(p+"/data/"));
	static FileNameExtensionFilter imgfilter = new FileNameExtensionFilter("Images", "jpg", "png");
	static FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter("Levels", "xml");
	public static JFileChooser fileChooser = new JFileChooser(p+"/data/",fsv);
	

    protected static class func {
		                            
        protected static JFrame FuncWin;
        protected static JPanel content;
        protected static JButton Load, Save, Run, Remove, Add, Clone, Fixture;
    }
    
    protected static class script {

        protected static JFrame scriptWindow;
        protected static JScrollPane scrollingArea;
        protected static JPanel content;
        protected static JTextArea textArea;
	}
    
    protected static class props {

        protected static JFrame propsWindow;
        protected static JScrollPane scroll;
        protected static JPanel content;
        protected static JTextField Name,xpos,ypos,Ang,Offx,Offy,Width,Height,Twidth,Theight;
        protected static JComboBox<String> Xwrap,Ywrap;
        protected static JButton Texture;

    }

    protected static class body {

        protected static JFrame bodyWindow;
        protected static JScrollPane scroll;
        protected static JPanel content;
        protected static JComboBox<String> BodyType,ShapeIndex,IsSensor;
        protected static JTextField ShapeType,OffsetX,OffsetY,Width,Height,Friction,Restitution,Density;
        protected static JLabel radiusLabel;
    }
    
    protected static void setImgFilter() {
		fileChooser.resetChoosableFileFilters();
		fileChooser.addChoosableFileFilter(imgfilter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(imgfilter);		
	}
	
	protected static void setLevelFilter() {
		fileChooser.resetChoosableFileFilters();
		fileChooser.addChoosableFileFilter(xmlfilter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(xmlfilter);		
	}

    protected static void initialise() {


        bodyTypes[0] = new String("Static");
        bodyTypes[1] = new String("Kinematic");
        bodyTypes[2] = new String("Dynamic");
         
        wraps[Texture.TextureWrap.MirroredRepeat.ordinal()]="Mirror";
        wraps[Texture.TextureWrap.ClampToEdge.ordinal()]="Clamp";
        wraps[Texture.TextureWrap.Repeat.ordinal()]="Repeat";

		
		Events.handler = new Events();



        script.textArea = new JTextArea(12,40);
        script.scrollingArea = new JScrollPane(script.textArea);
        
        script.content = new JPanel();
        script.content.setLayout(new BorderLayout());
        script.content.add(script.scrollingArea, BorderLayout.CENTER);


		script.scriptWindow = new JFrame();
		script.scriptWindow.setContentPane(script.content);
        script.scriptWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        script.scriptWindow.pack();
        script.scriptWindow.setVisible(true);



        func.FuncWin = new JFrame("");
        func.content = new JPanel(new SpringLayout());
        func.FuncWin.setContentPane(func.content);
        func.Load = addUnlabeledButton(func.content,"Load");
        func.Save = addUnlabeledButton(func.content,"Save");
        func.Run = addUnlabeledButton(func.content,"Run");
        func.Remove = addUnlabeledButton(func.content,"Remove");
        func.Add = addUnlabeledButton(func.content,"Add");
        func.Clone = addUnlabeledButton(func.content,"Clone");
        func.Fixture = addUnlabeledButton(func.content,"+Shape");
 
 // 2 dummy items to make a "square" grid       
        func.content.add(new JLabel(""));func.content.add(new JLabel(""));

 
 
		func.FuncWin.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        func.FuncWin.pack();
        func.FuncWin.setSize(260,128);

		SpringUtilities.makeCompactGrid(func.content,
						3, 3, 		//rows, cols
						6, 6,        //initX, initY
						6, 6);       //xPad, yPad
               		
        func.FuncWin.setVisible(true);

     

        
        props.content = new JPanel(new SpringLayout());
        props.scroll = new JScrollPane(props.content);

		props.propsWindow = new JFrame();
		props.propsWindow.setContentPane(props.scroll);
        props.propsWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		props.Name = addTextField(props.content, "Name");
		props.xpos = addTextField(props.content, "X");
		props.ypos = addTextField(props.content, "Y");
		props.Ang = addTextField(props.content, "Angle");
		props.Offx = addTextField(props.content, "Off X");
		props.Offy = addTextField(props.content, "Off Y");
		props.Width = addTextField(props.content, "Width");
		props.Height = addTextField(props.content, "Height");
		props.Texture = addJButton(props.content, "Texture");
		props.Twidth = addTextField(props.content, "T width");
		props.Theight = addTextField(props.content, "T height");
		
		props.Xwrap = addComboBox(props.content,"X wrap",wraps);
		props.Ywrap = addComboBox(props.content,"Y wrap",wraps);

		SpringUtilities.makeCompactGrid(props.content,
                                13, 2, 		//rows, cols
                                6, 6,        //initX, initY
                                6, 6);       //xPad, yPad

		props.propsWindow.setSize(240,240);
        props.propsWindow.setVisible(true);
        


        body.content = new JPanel(new SpringLayout());
        body.scroll = new JScrollPane(body.content);

		body.bodyWindow = new JFrame();
		body.bodyWindow.setContentPane(body.scroll);
        body.bodyWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
     
		body.BodyType = addComboBox(body.content, "Body type", bodyTypes);
		body.ShapeIndex = addComboBox(body.content, "Shape", new String[]{});
		body.IsSensor = addComboBox(body.content, "Sensor", new String[]{"false","true"});
		body.ShapeType = addTextField(body.content,"Shape type"); body.ShapeType.setEditable(false);
		body.OffsetX = addTextField(body.content,"Offset X");
		body.OffsetY = addTextField(body.content,"Offset Y");
		body.Width = addTextField(body.content,"Width");
		body.radiusLabel = lastLabel;
		body.Height = addTextField(body.content,"Height");
		body.Friction = addTextField(body.content,"Friction");
		body.Restitution = addTextField(body.content,"Restitution");
		body.Density = addTextField(body.content,"Density");

		SpringUtilities.makeCompactGrid(body.content,
                                11, 2, 		//rows, cols
                                6, 6,        //initX, initY
                                6, 6);       //xPad, yPad
		
		body.bodyWindow.setSize(240,240);
        body.bodyWindow.setVisible(true);
      

        body.bodyWindow.setIconImage(new ImageIcon("icons/phys.png").getImage());
        func.FuncWin.setIconImage(new ImageIcon("icons/funcs.png").getImage());
        props.propsWindow.setIconImage(new ImageIcon("icons/props.png").getImage());
        script.scriptWindow.setIconImage(new ImageIcon("icons/script.png").getImage());
        
        props.propsWindow.setLocation(
					props.propsWindow.getLocation().x,
					func.FuncWin.getHeight()+func.FuncWin.getLocation().y+8);
        body.bodyWindow.setLocation(
					props.propsWindow.getLocation().x,
					props.propsWindow.getHeight()+props.propsWindow.getLocation().y+8);
    }


	private static JTextField addTextField(JPanel parent, String labelText) {
		JLabel l = new JLabel(labelText,JLabel.TRAILING);
		lastLabel=l;
		parent.add(l);
		JTextField textField = new JTextField(4);
		l.setLabelFor(textField); // can't reverse look up this (ie from textField get label?)
		parent.add(textField);
		textField.addKeyListener(Events.handler);
		textField.addFocusListener(Events.handler);
		return textField;		
	}
	
	private static JComboBox<String> addComboBox(JPanel parent, String labelText, String[] items) {
		JLabel l = new JLabel(labelText, JLabel.TRAILING);
		parent.add(l);
		JComboBox<String> jcb = new JComboBox<String>(items);
		l.setLabelFor(jcb);
		parent.add(jcb);
		jcb.setEditable(false);
		jcb.addActionListener(Events.handler);
		return jcb;
	}
	
	private static JButton addJButton(JPanel parent, String labelText) {
		JLabel l = new JLabel(labelText, JLabel.TRAILING);
		parent.add(l);
		JButton jb = new JButton(labelText);
		l.setLabelFor(jb);
		parent.add(jb);
		jb.addActionListener(Events.handler);
		return jb;
	}

	private static JButton addUnlabeledButton(JPanel parent, String labelText) {
		JButton jb = new JButton(labelText);
		parent.add(jb);
		jb.addActionListener(Events.handler);
		return jb;
	}
	
	public static class DirectoryRestrictedFileSystemView extends FileSystemView {
		private final File[] rootDirectories;

		DirectoryRestrictedFileSystemView(File rootDirectory) { this.rootDirectories = new File[] {rootDirectory}; }
		DirectoryRestrictedFileSystemView(File[] rootDirectories) { this.rootDirectories = rootDirectories; }

		@Override
		public File createNewFolder(File containingDir) //throws IOException
		{       
			//throw new UnsupportedOperationException("Unable to create directory");
			return null;
		}

		@Override
		public File[] getRoots() { return rootDirectories; }
		

		@Override
		public boolean isRoot(File file) {
			for (File root : rootDirectories) if (root.equals(file)) return true;
			return false;
		}
		
		// only allow access to the roots
		@Override
		public Boolean isTraversable(File file) { return (Boolean)isRoot(file); }
	}
	

}
