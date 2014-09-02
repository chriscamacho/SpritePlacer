package uk.co.bedroomcoders.placer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
 
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.lang.Float;
import java.lang.Integer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

// level loader parses xml into a new pixy which
// adds it to the static pixy list.
public class LevelLoader
{
	static XMLReader xmlReader;
	static SAXParser sp;
	
	LevelLoader(final String pAssetFilePath) {
		final SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			sp = spf.newSAXParser();
			levelDefaultHandler handler = new levelDefaultHandler();
        	InputStream inputStream = Gdx.files.local(pAssetFilePath).read();
        	sp.parse(inputStream,handler);
        } catch(Exception e) { System.out.println(e); }

	}
	
	public class levelDefaultHandler extends DefaultHandler {
		float x,y,sx=1,sy=1,angle;
		int ox,oy,width,height,wx,wy,tw,th;
		String texture,name;
		
		public void startElement(String uri, String localName, String qName, Attributes attributes)
												throws SAXException 
		{
			for(int i=0;i<attributes.getLength();i++) 
			{
				if (attributes.getQName(i).equalsIgnoreCase("x"))
					x = Float.valueOf(attributes.getValue(i)).floatValue();
				if (attributes.getQName(i).equalsIgnoreCase("y"))
					y = Float.valueOf(attributes.getValue(i)).floatValue();
				if (attributes.getQName(i).equalsIgnoreCase("sx"))
					sx = Float.valueOf(attributes.getValue(i)).floatValue();
				if (attributes.getQName(i).equalsIgnoreCase("sy"))
					sy = Float.valueOf(attributes.getValue(i)).floatValue();
				if (attributes.getQName(i).equalsIgnoreCase("ox"))
					ox = Integer.valueOf(attributes.getValue(i)).intValue();
				if (attributes.getQName(i).equalsIgnoreCase("oy"))
					oy = Integer.valueOf(attributes.getValue(i)).intValue();
				if (attributes.getQName(i).equalsIgnoreCase("width"))
					width = Integer.valueOf(attributes.getValue(i)).intValue();
				if (attributes.getQName(i).equalsIgnoreCase("height"))
					height = Integer.valueOf(attributes.getValue(i)).intValue();
				if (attributes.getQName(i).equalsIgnoreCase("angle"))
					angle = Float.valueOf(attributes.getValue(i)).floatValue();
				if (attributes.getQName(i).equalsIgnoreCase("texture"))
					texture = attributes.getValue(i);
				if (attributes.getQName(i).equalsIgnoreCase("name"))
					name = attributes.getValue(i);
				if (attributes.getQName(i).equalsIgnoreCase("xwrap"))
					wx = Integer.valueOf(attributes.getValue(i)).intValue();
				if (attributes.getQName(i).equalsIgnoreCase("ywrap"))
					wy = Integer.valueOf(attributes.getValue(i)).intValue();
				if (attributes.getQName(i).equalsIgnoreCase("twidth"))
					tw = Integer.valueOf(attributes.getValue(i)).intValue();
				if (attributes.getQName(i).equalsIgnoreCase("theight"))
					th = Integer.valueOf(attributes.getValue(i)).intValue();
			}
			//if(qName.equalsIgnoreCase("level")) levelScript=script;
		}

		public void endElement(String uri, String localName, String qName)
												throws SAXException 
		{
			//System.out.println(uri+" "+localName+" "+qName);
			Pixy px=null;
			if(qName.equalsIgnoreCase("pixy")) 
			{
				px=new Pixy(x,y,ox,oy,width,height,sx,sy,angle,texture,name,wx,wy,tw,th);
			}
			// MUST reset to default values here
			x=0;y=0;ox=0;oy=0;angle=0;width=0;height=0;texture="";name="";sx=1;sy=1;wx=0;wy=0;tw=0;th=0;
		}

		public void characters(char ch[], int start, int length)
												throws SAXException 
		{
			//if (length>1) System.out.println("len "+length+"  characters ch : " + new String(ch, start, length));
		}
	}
	
}
