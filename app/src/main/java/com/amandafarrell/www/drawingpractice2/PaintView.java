package com.amandafarrell.www.drawingpractice2;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class PaintView extends View {

    public static int backgroundColor = Color.WHITE;
    public static final float TOUCH_TOLERANCE = 10;

    private Bitmap mBitmap;
    private Canvas mBitmapCanvas;
    private Paint mPaintScreen;
    private Paint mPaintLine;
    private HashMap<Integer, Path> mPathHashMap;
    private HashMap<Integer, Point> mPreviousPointMap;

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        mPaintScreen = new Paint();

        mPaintLine = new Paint();
        mPaintLine.setAntiAlias(true);
        mPaintLine.setColor(Color.BLACK);
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setStrokeWidth(7);
        mPaintLine.setStrokeCap(Paint.Cap.ROUND);

        mPathHashMap = new HashMap<>();
        mPreviousPointMap = new HashMap<>();
    }

    public void setDrawingColor(int color) {
        mPaintLine.setColor(color);
    }

    public int getDrawingColor() {
        return mPaintLine.getColor();
    }

    public void setLineWidth(int width) {
        mPaintLine.setStrokeWidth(width);
    }

    public int getLineWidth() {
        return (int) mPaintLine.getStrokeWidth();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mBitmap);
        mBitmap.eraseColor(backgroundColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //starts at the top left corner
        canvas.drawBitmap(mBitmap, 0, 0, mPaintScreen);

        for (Integer key : mPathHashMap.keySet()) {
            canvas.drawPath(mPathHashMap.get(key), mPaintLine);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked(); //event type
        int actionIndex = event.getActionIndex(); // pointer

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
            touchStarted(event.getX(actionIndex), event.getY(actionIndex),
                    event.getPointerId(actionIndex));
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            touchEnded(event.getPointerId(actionIndex));
        } else {
            touchMoved(event);
        }

        invalidate(); // redraw the screen
        return true;
    }

    private void touchMoved(MotionEvent event) {

        for (int i = 0; i < event.getPointerCount(); i++) {
            int pointerId = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerId);

            if (mPathHashMap.containsKey(pointerId)) {
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                Path path = mPathHashMap.get(pointerId);
                Point point = mPreviousPointMap.get(pointerId);

                //Calculate how far the user moved from the last update
                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);

                //checks for significant movement distance, removed so dots can be drawn
                //if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {

                //move to the new location
                //quadTo draws a curve between the points using a quadratic line
                path.quadTo(point.x, point.y, (newX + point.x) / 2, (newY + point.y) / 2);

                //store the new coordinates
                point.x = (int) newX;
                point.y = (int) newY;
                //}
            }
        }
    }

    public void clear() {
        mPathHashMap.clear(); // removes all the paths
        mPreviousPointMap.clear();
        mBitmap.eraseColor(backgroundColor);
        invalidate(); // refresh the screen
    }

    private void touchEnded(int pointerId) {
        Path path = mPathHashMap.get(pointerId); // get the corresponding path
        mBitmapCanvas.drawPath(path, mPaintLine); // draw to bitmapCanvas
        path.reset();
    }

    private void touchStarted(float x, float y, int pointerId) {
        Path path; // store the path for given touch
        Point point; // store the last point in path

        if (mPathHashMap.containsKey(pointerId)) {
            path = mPathHashMap.get(pointerId);
            point = mPreviousPointMap.get(pointerId);
        } else {
            path = new Path();
            mPathHashMap.put(pointerId, path);
            point = new Point();
            mPreviousPointMap.put(pointerId, point);
        }

        //move the path to the coordinates of the touch
        path.moveTo(x, y);
        point.x = (int) x;
        point.y = (int) y;
    }

    public void saveImage() {
        String filename = "Paint" + System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");

        //get a URI for the location to save the file
        Uri uri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try {
            OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri);

            //copy the bitmap to the outputStream
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 200, outputStream); // this is the image

            try {
                outputStream.flush();
                outputStream.close();

                Toast messageSave = Toast.makeText(getContext(), "Image Saved", Toast.LENGTH_SHORT);
                messageSave.setGravity(Gravity.CENTER, messageSave.getXOffset() / 2, messageSave.getYOffset() / 2);
                messageSave.show();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();

            Toast messageNotSaved = Toast.makeText(getContext(), "Error Saving Image", Toast.LENGTH_SHORT);
            messageNotSaved.setGravity(Gravity.CENTER, messageNotSaved.getXOffset() / 2, messageNotSaved.getYOffset() / 2);
            messageNotSaved.show();
        }
    }
}
