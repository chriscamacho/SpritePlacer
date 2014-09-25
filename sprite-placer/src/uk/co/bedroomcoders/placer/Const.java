package uk.co.bedroomcoders.placer;

import com.badlogic.gdx.math.Vector2;

public class Const {

    public static final float   PI180           =   ((float)Math.PI) / 180.0f;
    public static final float   I80PI           =   180.0f / ((float)Math.PI);

    public static final float   BOX2WORLD       =   32f;
    public static final float   WORLD2BOX       =   1f/BOX2WORLD;

    public static final float   TIME_STEP       =   1f/30f;
    public static final float   MAXFRAMETIME    =   TIME_STEP*4f;//0.25f;
    public static final int     VEL_ITER        =   6;
    public static final int     POS_ITER        =   2;

    public static final Vector2 GRAVITY         =   new Vector2(0f,-9.81f);

}
