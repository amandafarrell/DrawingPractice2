package com.amandafarrell.www.drawingpractice2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

    private PaintView mPaintView;
    private AlertDialog.Builder mCurrentAlertDialog;

    private AlertDialog mDialogLineWidth;
    private ImageView mLineWidthImageView;
    private SeekBar mLineWidthSeekBar;

    private AlertDialog mDialogColorChange;
    private SeekBar mAlphaSeekBar;
    private SeekBar mRedSeekBar;
    private SeekBar mGreenSeekBar;
    private SeekBar mBlueSeekBar;

    private View mColorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPaintView = findViewById(R.id.paint_view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.undo:
                mPaintView.undo();
                return true;
            case R.id.erase:
                return true;
            case R.id.color_id:
                showColorDialog();
                return true;
            case R.id.line_width:
                showLineWidthDialog();
                return true;
            case R.id.save:
                mPaintView.saveImage();
                return true;
            case R.id.clear_all:
                mPaintView.clear();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showColorDialog() {
        mCurrentAlertDialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_color_change, null);
        mAlphaSeekBar = view.findViewById(R.id.seek_bar_alpha);
        mRedSeekBar = view.findViewById(R.id.seek_bar_red);
        mGreenSeekBar = view.findViewById(R.id.seek_bar_green);
        mBlueSeekBar = view.findViewById(R.id.seek_bar_blue);
        mColorView = view.findViewById(R.id.color_view);

        mAlphaSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        mRedSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        mGreenSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        mBlueSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);

        int color = mPaintView.getDrawingColor();
        mAlphaSeekBar.setProgress(Color.alpha(color));
        mRedSeekBar.setProgress(Color.red(color));
        mGreenSeekBar.setProgress(Color.green(color));
        mBlueSeekBar.setProgress(Color.blue(color));

        Button setColorButton = view.findViewById(R.id.button_set_color);
        setColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPaintView.setDrawingColor(Color.argb(
                        mAlphaSeekBar.getProgress(),
                        mRedSeekBar.getProgress(),
                        mGreenSeekBar.getProgress(),
                        mBlueSeekBar.getProgress()
                ));

                mDialogColorChange.dismiss();
            }
        });

        mCurrentAlertDialog.setView(view);
        mDialogColorChange = mCurrentAlertDialog.create();
        mDialogColorChange.setTitle("Choose Color");
        mDialogColorChange.show();
    }

    public void showLineWidthDialog() {
        mCurrentAlertDialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_stroke_width, null);
        mLineWidthSeekBar = view.findViewById(R.id.seek_bar_line_width);
        mLineWidthImageView = view.findViewById(R.id.width_image_view);
        Button setLineWidthButton = view.findViewById(R.id.width_dialog_button);

        //set the seek bar to match current preference
        mLineWidthSeekBar.setProgress(mPaintView.getLineWidth());

        //initialize the image view with line at current stroke width
        Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(mPaintView.getDrawingColor());
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mPaintView.getLineWidth());

        bitmap.eraseColor(PaintView.backgroundColor);
        canvas.drawLine(50, 50, 300, 50, paint);
        mLineWidthImageView.setImageBitmap(bitmap);

        setLineWidthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPaintView.setLineWidth(mLineWidthSeekBar.getProgress());
                mDialogLineWidth.dismiss();
                mCurrentAlertDialog = null;
            }
        });

        mLineWidthSeekBar.setOnSeekBarChangeListener(widthSeekBarChangeListener);

        mCurrentAlertDialog.setView(view);
        mDialogLineWidth = mCurrentAlertDialog.create();
        mDialogLineWidth.setTitle("Set Line Width");
        mDialogLineWidth.show();
    }

    private SeekBar.OnSeekBarChangeListener colorSeekBarChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mPaintView.setBackgroundColor(Color.argb(
                    mAlphaSeekBar.getProgress(),
                    mRedSeekBar.getProgress(),
                    mGreenSeekBar.getProgress(),
                    mBlueSeekBar.getProgress()
            ));

            //display current color
            mColorView.setBackgroundColor(Color.argb(
                    mAlphaSeekBar.getProgress(),
                    mRedSeekBar.getProgress(),
                    mGreenSeekBar.getProgress(),
                    mBlueSeekBar.getProgress()
            ));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private SeekBar.OnSeekBarChangeListener widthSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Paint paint = new Paint();
            paint.setColor(mPaintView.getDrawingColor());
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(progress);

            bitmap.eraseColor(PaintView.backgroundColor);
            canvas.drawLine(50, 50, 300, 50, paint);
            mLineWidthImageView.setImageBitmap(bitmap);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
}
