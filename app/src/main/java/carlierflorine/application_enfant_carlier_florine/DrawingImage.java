package carlierflorine.application_enfant_carlier_florine;
// import
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/*Fonctionnalités :
- L'enfant peut remplir le contour de la forme en tracant à l'aide de l'objet Paint
- Si il rempli dans la forme, son pinceau est en vert
- Si il déborde, son pinceau devient rouge
- Détection si la forme est complète ou non
- Si elle est complète, alors passer à la lettre suivante
- Menu contenant deux icones : une permettant de retourner en arrière, l'autre permettant de recommencer en effacant l'écran
- Layout de félicitation quand les deux lettres sont complétés

Note : Lors du dessin de la lettre B, je ne comprend pas pourquoi mais il y a un décallage entre le geste fait et l'affiche du trait
De plus, la layout de félicitation met longtemps a charger donc une fois la forme terminé, attendre 5sec avant de pouvoir le voir apparaitre

 */

public class DrawingImage extends android.support.v7.widget.AppCompatImageView {

    private Bitmap bitmap;
    //defines how to draw
    private Paint mPaint;
    private boolean canDraw = false;
    private boolean drawAgain = true;
    private float drawingArea;
    private float drawnArea;

    private List<Point> greenPoints;
    private List<Point> dotPoints;

    public int height;
    //canvas bitmap
    private Bitmap mBitmap;

    private Canvas mCanvas;

    //drawing path
    private Path mPath;
    //defines what to draw
    private Paint mBitmapPaint;
    Context context;


    public DrawingImage(Context c, Bitmap bitmap) {
        super(c);
        context = c;
        this.bitmap = bitmap;

        dotedPart(bitmap);

        //set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(50);

        drawingArea = percentInnerPart(bitmap);
        Log.e("% ===", "" + drawingArea);

        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //create canvas of certain device size.
        super.onSizeChanged(w, h, oldw, oldh);
        // your Canvas will draw onto the defined Bitmap , create Bitmap of certain w,h
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //apply bitmap to graphic to start drawing.
        mCanvas = new Canvas(mBitmap);
    }

    // Draws the path created during the touch events
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        // draw the mPath with the mPaint on the canvas when onDra
        canvas.drawPath(mPath, mPaint);

    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    // when ACTION_DOWN start touch according to the x,y values and update the values of mX and mY
    private void touchStart(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    // when ACTION_MOVE move touch according to the x,y values
    private void touchMove(float x, float y) {
        //transform the x,y event coordinates into path moves.
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if ((dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)) {
            // Using quadTo helps to make smooth line on curve.
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;

        }
    }
    // when ACTION_UP stop touch
    private void touchUp() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        //respond to down, move and up events
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Starts a new line in the path
                touchStart(x, y);
                invalidate();
                DrawFinishListener callback = (DrawFinishListener) context;
                callback.onDrawStart();
                break;
            case MotionEvent.ACTION_MOVE: // Draws line between last point and this point
                check((int) event.getX(), (int) event.getY());
                if (drawAgain)
                    if (canDraw) {
                        touchMove(x, y);
                    } else {
                        touchUp();
                        touchStart(x, y);
                    }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();

                if (drawAgain) {
                    filledArea(mBitmap);
                    checkResult();
                }

                break;
        }
        return true;
    }

    // check if the letter is finish or not
    private void checkResult() {
        int count = 0;
        for (Point greenPoint : greenPoints) {
            if (dotPoints.contains(greenPoint)) {
                count++;
            }
        }

        DrawFinishListener mCallback = (DrawFinishListener) context;
        // Case letter A
        if(dotPoints.size()==29){
            if(count == dotPoints.size()){
                //compromise 5% area
                if (drawnArea - 5 < drawingArea) {
                    canDraw = false;
                    drawAgain = false;
                    mCallback.onDrawFinish();
                } else
                    mCallback.onDrawStop();
            } else
                mCallback.onDrawStop();
        }
        // Case letter B
        else{
            // compromise 30% because the letter is complexe to draw
            if(count +30>= dotPoints.size()){
                //compromise 20% area
                if (drawnArea - 20 < drawingArea) {
                    canDraw = false;
                    drawAgain = false;
                    mCallback.onDrawFinish();
                } else
                    mCallback.onDrawStop();
            } else
                mCallback.onDrawStop();

        }
    }
    // return the total percentage of the bitmap
    private float percentInnerPart(Bitmap bm) {
        final int width = bm.getWidth();
        final int height = bm.getHeight();

        int myColor = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = bitmap.getPixel(x, y);
                if (Color.red(pixel) == 201 &&
                        Color.red(pixel) == Color.blue(pixel) &&
                        Color.red(pixel) == Color.green(pixel)) {
                    myColor++;
                }
            }
        }
        // % % to reach for the letter to be completely filled
        return ((float) myColor * 100) / (width * height);
    }

    // Count how many dots there are in the letter
    private void dotedPart(Bitmap bm) {
        final int width = bm.getWidth();
        final int height = bm.getHeight();
        //initialisation
        dotPoints = new ArrayList<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = bm.getPixel(x, y);
                // rgb(240,240,240) is the color of the dots located at different places in the letter
                if (Color.red(pixel) == 240 &&
                        Color.red(pixel) == Color.blue(pixel) &&
                        Color.red(pixel) == Color.green(pixel)) {
                    // if the dots is found, add to the ArrayList
                    dotPoints.add(new Point(x, y));
                }
            }
        }
    }
    // check if the user draws inside the letter
    private void check(int x, int y) {
        if (bitmap != null && x >= 0 && x < bitmap.getWidth() && y >= 0 && y < bitmap.getHeight()) {
            int pixel = bitmap.getPixel(x, y);
            // the color inside the letter A (drawing area) is rgb(200,200,200) with 10% of opacity , so if r=g=b, the user is in the letter
            if (Color.red(pixel) != 0 && Color.red(pixel) == Color.blue(pixel) && Color.red(pixel) == Color.green(pixel)) {
                Log.e("Touch ===", "You're in");
                // if the user is in the letter, the color of mPaint will turn green
                mPaint.setColor(Color.GREEN);
                canDraw = true;
            } else {
                Log.e("Touch ===", "You're out");
                // else, the user is out the letter so the color of mPaint will turn red
                mPaint.setColor(Color.RED);
                // put a transparent color so that he can not draw
                mBitmap.eraseColor(Color.TRANSPARENT);
                invalidate();
                canDraw = false;
            }
        }
    }
    // count how much % the letter is filled
    private void filledArea(Bitmap bm) {
        final int width = bm.getWidth();
        final int height = bm.getHeight();

        int colorGreen = 0;
        greenPoints = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (bm.getPixel(x, y) == Color.GREEN) {
                    greenPoints.add(new Point(x, y));
                    colorGreen++;

                }
            }
        }
        // % of filled area
        drawnArea = ((float) colorGreen * 100) / (width * height);

    }

    public interface DrawFinishListener {
        void onDrawFinish();

        void onDrawStop();

        void onDrawStart();
    }
}

