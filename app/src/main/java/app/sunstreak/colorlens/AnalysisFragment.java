package app.sunstreak.colorlens;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AnalysisFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnalysisFragment extends Fragment {

    public final String APP_PREFERENCES = "AppPreferences";

    @Bind(R.id.fragment_analysis_container) FrameLayout container;
    @Bind(R.id.fragment_analysis_crosshairs) ImageView crossHairs;
    @Bind(R.id.fragment_analysis_imageview) ImageView image;
    @Bind(R.id.fragment_analysis_color_history) RelativeLayout historyBox;
    @Bind(R.id.fragment_analysis_retake) ImageButton retake;
    @Bind(R.id.fragment_analysis_settings) ImageButton settings;

    private Bitmap pic;
    private int imageViewWidth;
    private int imageViewHeight;
    private int bitMapWidth;
    private int bitMapHeight;
    private String colorHex;
    private SharedPreferences sharedPreferences;
    private final float zoomScale = 0.4f;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AnalysisFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AnalysisFragment newInstance() {
        AnalysisFragment fragment = new AnalysisFragment();
        return fragment;
    }

    public AnalysisFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPreferences = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        // Inflate the layout for this fragment
//        Display display = getActivity().getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        int width = size.x;
        ContentResolver cr = getActivity().getContentResolver();
        try {
//            BitmapFactory.Options options = new BitmapFactory.Options(); options.inPurgeable = true;
//            pic = MediaStore.Images.Media.getBitmap(cr, MainActivity.mImageUri);
//            pic = Bitmap.createScaledBitmap(pic, width, pic.getHeight()/(pic.getWidth()/width), false);
            pic = ImageProcessor.decodeSampledBitmap(getActivity(), MainActivity.mImageUri);
            bitMapWidth = pic.getWidth();
            bitMapHeight = pic.getHeight();
        } catch (IOException e) {
            e.printStackTrace();
            getActivity().finish();
        }

        View v = inflater.inflate(R.layout.fragment_analysis, container, false);
        ButterKnife.bind(this, v);

        image.setImageBitmap(pic);
        ViewTreeObserver vto = image.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                image.getViewTreeObserver().removeOnPreDrawListener(this);
                imageViewHeight = image.getMeasuredHeight();
                imageViewWidth = image.getMeasuredWidth();
                return true;
            }
        });
        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        image.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();
//                lp.setMargins((int) x - crossHairs.getWidth() / 2, (int) y - crossHairs.getHeight() / 2, 0, 0);
                    AnimatorSet move = new AnimatorSet();
                    ObjectAnimator moveX = ObjectAnimator.ofFloat(crossHairs, "x",
                            (int) x - crossHairs.getWidth() / 2);
                    ObjectAnimator moveY = ObjectAnimator.ofFloat(crossHairs, "y",
                            (int) y - crossHairs.getHeight() / 2);
                    move.playTogether(moveX, moveY);
                    move.setDuration(300);
                    move.setInterpolator(new FastOutSlowInInterpolator());
                    move.start();
                    if (crossHairs.getVisibility() != View.VISIBLE)
                        crossHairs.setVisibility(View.VISIBLE);
                    System.out.println(crossHairs.getX() + " to " + (x - crossHairs.getWidth() / 2) + " " + crossHairs.getY() + " to " + (y - crossHairs.getHeight() / 2));

//                crossHairs.setLayoutParams(lp);
                    float actualX = x / imageViewWidth * bitMapWidth;
                    float actualY = y / imageViewHeight * bitMapHeight;
                    int pixel = pic.getPixel((int) actualX, (int) actualY);
                    colorHex = ImageProcessor.getHexFromPixel(pixel);
                    new GetColorInfoTask().execute(Color.red(pixel), Color.green(pixel), Color.blue(pixel));
                    return true;
                }
                return false;

            }
        });

        SpringSystem springSystem = SpringSystem.create();
        final Spring retakeSpring = springSystem.createSpring();
        final Spring settingsSpring = springSystem.createSpring();
        retakeSpring.addListener(new SimpleSpringListener(){
            @Override
            public void onSpringUpdate(Spring spring) {
                // You can observe the updates in the spring
                // state by asking its current value in onSpringUpdate.
                float value = (float) spring.getCurrentValue();
                float scale = 1f + (value * zoomScale);
                retake.setScaleX(scale);
                retake.setScaleY(scale);
            }
        });
        settingsSpring.addListener(new SimpleSpringListener(){
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();
                float scale = 1f + (value * zoomScale);
                settings.setScaleX(scale);
                settings.setScaleY(scale);
            }
        });
        retake.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    retakeSpring.setEndValue(1);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    retakeSpring.setEndValue(0);
                }
                return false;
            }
        });
        settings.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    settingsSpring.setEndValue(1);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    settingsSpring.setEndValue(0);
                }
                return false;
            }
        });
        retake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).dispatchTakePictureIntent();
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder s = new AlertDialog.Builder(getActivity());
                String[] options = {"Primary (Red, Green)", "Detailed (Onyx, Mahogany)"};
                int currSelection = sharedPreferences.getInt("colorOption", 0);
                s.setTitle("Color Options").setSingleChoiceItems(options, currSelection, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("colorOption", which);
                        editor.commit();
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });

        return v;
    }
    private class GetColorInfoTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            int red = params[0];
            int green = params[1];
            int blue = params[2];
            int colorOption = sharedPreferences.getInt("colorOption",0);

            return ImageProcessor.getClosestColor(red, green, blue, colorOption);
        }

        @Override
        protected void onPostExecute(String s){
//            if(historyBox.getChildCount()!=1)
//            {
            TextView newItem = (TextView) getActivity().getLayoutInflater().inflate(R.layout.textview_color_box, historyBox, false);
            newItem.setText(colorHex + "\n" + s);
            historyBox.removeAllViews();
            historyBox.addView(newItem, 0);
//            }
//            else
//            {
//                colorName.setText(colorHex+"\n" + s);
//            }

        }
    }

}
