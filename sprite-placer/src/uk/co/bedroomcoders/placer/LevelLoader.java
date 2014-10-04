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

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.math.Vector2;

// level loader parses xml into a new pixy which
// adds it to the static pixy list.
public class LevelLoader
{
	static XMLReader xmlReader;
	static SAXParser sp;

    static final Vector2 tmpV2 = new Vector2();
	
	LevelLoader(final String pAssetFilePath) {
		final SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			sp = spf.newSAXParser();

			levelDefaultHandler handler = new levelDefaultHandler();
        	InputStream inputStream = Gdx.files.local(pAssetFilePath).read();
        	sp.parse(inputStream,handler);
        } catch(Exception e) { e.printStackTrace(); }

	}
	
	public class levelDefaultHandler extends DefaultHandler {
		float x,y,angle;
		int ox,oy,width,height,wx,wy,tw,th;
		String texture,name,tex;
        Pixy px=null;
        String shpType;
        float shpX,shpY,shpRadius,shpWidth,shpHeight,shpRestitution,shpDensity,shpFriction;
        long uid;
        String bodyType,script;
        
		public void startElement(String uri, String localName, String qName, Attributes attributes)
												throws SAXException 
		{
            //System.out.println("start element >"+uri+"< >"+localName+"< >"+qName+"<");

            for(int i=0;i<attributes.getLength();i++) 
			{
                //System.out.println("attribute "+attributes.getQName(i)); 
                if (qName.equalsIgnoreCase("pixy")) {
                    if (attributes.getQName(i).equalsIgnoreCase("x"))
                        x = Float.valueOf(attributes.getValue(i)).floatValue();
                    if (attributes.getQName(i).equalsIgnoreCase("uid"))
                        uid = Long.valueOf(attributes.getValue(i)).longValue();
                    if (attributes.getQName(i).equalsIgnoreCase("y"))
                        y = Float.valueOf(attributes.getValue(i)).floatValue();
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
                if (qName.equalsIgnoreCase("body")) {
                    if (attributes.getQName(i).equalsIgnoreCase("type"))
                        bodyType=attributes.getValue(i);
                }
                if (qName.equalsIgnoreCase("shape")) {
                    if (attributes.getQName(i).equalsIgnoreCase("type"))
                        shpType=attributes.getValue(i);
                    if (attributes.getQName(i).equalsIgnoreCase("x"))
                        shpX=Float.valueOf(attributes.getValue(i)).floatValue();
                    if (attributes.getQName(i).equalsIgnoreCase("y"))
                        shpY=Float.valueOf(attributes.getValue(i)).floatValue();
                    if (attributes.getQName(i).equalsIgnoreCase("radius")) {
                        shpRadius=Float.valueOf(attributes.getValue(i)).floatValue();
                        //System.out.println("radius from xml "+shpRadius);
                    }
                    if (attributes.getQName(i).equalsIgnoreCase("width"))
                        shpWidth=Float.valueOf(attributes.getValue(i)).floatValue();
                    if (attributes.getQName(i).equalsIgnoreCase("height"))
                        shpHeight=Float.valueOf(attributes.getValue(i)).floatValue();
                    if (attributes.getQName(i).equalsIgnoreCase("restitution"))
                        shpRestitution=Float.valueOf(attributes.getValue(i)).floatValue();
                    if (attributes.getQName(i).equalsIgnoreCase("density"))
                        shpDensity=Float.valueOf(attributes.getValue(i)).floatValue();
                    if (attributes.getQName(i).equalsIgnoreCase("friction"))
                        shpFriction=Float.valueOf(attributes.getValue(i)).floatValue();

                }
                if (qName.equalsIgnoreCase("level")) {
                    if (attributes.getQName(i).equalsIgnoreCase("script"))
                        script=attributes.getValue(i);
                }
			}

            if (qName.equalsIgnoreCase("pixy")) {
                px=new Pixy(x,y,ox,oy,width,height,angle,texture,name,wx,wy,tw,th);
                if (uid==0) uid=SpritePlacer.getUID();
                px.setUID(uid);

                x=0;y=0;ox=0;oy=0;angle=0;width=0;height=0;texture="";name="";wx=0;wy=0;tw=0;th=0;
                uid=0;
            }

            if (qName.equalsIgnoreCase("shape")) {
                Fixture fx=null;
                tmpV2.set(shpX*Const.WORLD2BOX,shpY*Const.WORLD2BOX);
                if (shpType.equalsIgnoreCase("circle")) {
                    // circle
                    fx=px.addCircleShape();
                    CircleShape cshp=(CircleShape)fx.getShape();
                    cshp.setPosition(tmpV2);
                    cshp.setRadius(shpRadius*Const.WORLD2BOX);
                } else {
                    // box
                    fx=px.addBoxShape();
                    BoxShape bs=BoxShape.fauxCast((PolygonShape)fx.getShape());
                    bs.setPosition(tmpV2);
                    bs.setWidth((shpWidth*Const.WORLD2BOX)/2f);
                    bs.setHeight((shpHeight*Const.WORLD2BOX)/2f);
                    bs.update();
                }
                fx.setRestitution(shpRestitution);
                fx.setDensity(shpDensity);
                fx.setFriction(shpFriction);
                px.updateBodyTransform();
                SpritePlacer.selected=null;
                shpType="";shpX=0;shpY=0;shpRadius=0;shpWidth=0;shpHeight=0;shpRestitution=0;shpDensity=0;shpFriction=0;
            }

			if(qName.equalsIgnoreCase("level")) SpritePlacer.levelScript=script;
		}

		public void endElement(String uri, String localName, String qName)
												throws SAXException 
		{
			//System.out.println("end element "+uri+" "+localName+" "+qName);
            if (qName.equalsIgnoreCase("body")) {
                if (bodyType.equalsIgnoreCase("dynamic"))
                    px.body.setType(BodyType.DynamicBody);
                if (bodyType.equalsIgnoreCase("kinematic"))
                    px.body.setType(BodyType.KinematicBody);
                if (bodyType.equalsIgnoreCase("static"))
                    px.body.setType(BodyType.StaticBody);
            }
		}

		public void characters(char ch[], int start, int length)
												throws SAXException 
		{
			//if (length>1) System.out.println("len "+length+"  characters ch : " + new String(ch, start, length));
		}
	}
	
}
