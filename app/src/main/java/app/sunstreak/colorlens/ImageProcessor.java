package app.sunstreak.colorlens;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author Han Li
 *         Date: 8/1/2015.
 */
public class ImageProcessor {

    private static final int MAX_HEIGHT = 1024;
    private static final int MAX_WIDTH = 1024;

    public static String getClosestColor(int red, int green, int blue, int colorOption)
    {
        int redMultiple = 1;
        int greenMultiple = 1;
        int blueMultiple = 1;
        if(red>=green && red>= blue)
            redMultiple = 3;
        else if(green>=red && green >=blue)
            greenMultiple = 3;
        else
            blueMultiple = 3;
        double currDifference = Double.MAX_VALUE;
        String ret = "";
        try {
            JSONArray d;
            if(colorOption==0)
                d = new JSONArray(DataFile.getPrimaryColors());
            else
                d = new JSONArray(DataFile.getAllColors());


            for(int i = 0; i< d.length(); i++)
            {
                JSONObject curr = d.optJSONObject(i);

//                double avg = calculateDifferenceWithRGB(red, green, blue, curr, redMultiple, greenMultiple, blueMultiple);
                //Test Code with Hue instead of RGB
                double avg = calculateDifferenceWithHue(red, green, blue, curr);
                if(avg<currDifference)
                {
                    currDifference = avg;
                    ret = curr.optString("label");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }
    public static double getHueFromRGB(int red, int green, int blue)
    {
        float[] HSV = new float[3];
        Color.RGBToHSV(red, green, blue, HSV);
        return HSV[0];
//        double r = red/255.0;
//        double g = green/255.0;
//        double b = blue/255.0;
//        double[] a = {r, g, b};
//        Arrays.sort(a);
//        double min = a[0];
//        double max = a[2];
//        System.out.println(max-min);
//        if(max-min == 0)
//            return 0;
//        double hue=0;
//        if(r>=g && r>=b)
//        {
//            hue = (g-b)/(max-min);
//        }
//        else if(g>=r && g>=b)
//        {
//            hue = 2+(b-r)/(max-min);
//        }
//        else if(b>=r & b >=g)
//        {
//            hue = 4+(r-g)/(max-min);
//        }
//        hue *=60;
//        while(hue<0) {
//            hue += 360;
//        }
//        return hue;
    }
    private static double calculateDifferenceWithRGB(int red, int green, int blue, JSONObject curr,
                                                     int redMultiple, int greenMultiple, int blueMultiple)
    {
        int redDiff = Math.abs(red - curr.optInt("x"));
        int greenDiff = Math.abs(green - curr.optInt("y"));
        int blueDiff = Math.abs(blue-curr.optInt("z"));
        double avg = (redDiff* redMultiple + greenDiff * greenMultiple + blueDiff * blueMultiple)/3;
        return avg;
    }
    private static double calculateDifferenceWithHue(int red, int green, int blue, JSONObject curr)
    {
        double actualHue = getHueFromRGB(red, green, blue);
        double currHue = getHueFromRGB(curr.optInt("x"), curr.optInt("y"), curr.optInt("z"));
        System.out.println(actualHue);
        double avg = Math.abs(actualHue-currHue);
        return avg;
    }
    public static String getHexFromPixel(int pixel)
    {
        String hex = String.format("#%02X%02X%02X", Color.red(pixel), Color.green(pixel), Color.blue(pixel));
        return hex;
    }
    public static Bitmap decodeSampledBitmap(Context context, Uri selectedImage)
            throws IOException {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);
        img= rotateImageIfRequired(context, img, selectedImage);

//        img = applySaturationFilter(img, 2);
        return img;
    }

    /**
     * Rotate an image if required.
     * @param img
     * @param selectedImage
     * @return
     */
    private static Bitmap rotateImageIfRequired(Context context,Bitmap img, Uri selectedImage) {

        // Detect rotation
        int rotation = getRotation(context, selectedImage);
        System.out.println(rotation);
        if(rotation!=0){
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
            img.recycle();
            return rotatedImg;
        }else{
            return img;
        }
    }

    /**
     * Get the rotation of the last image added.
     * @param context
     * @param selectedImage
     * @return
     */
    private static int getRotation(Context context,Uri selectedImage) {
//        int rotation =0;
//        ContentResolver content = context.getContentResolver();
//
//
//        Cursor mediaCursor = content.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                new String[] { "orientation", "date_added" },null, null,"date_added desc");
//
//        if (mediaCursor != null && mediaCursor.getCount() !=0 ) {
//            while(mediaCursor.moveToNext()){
//                rotation = mediaCursor.getInt(0);
//                break;
//            }
//        }
//        mediaCursor.close();
//        return rotation;

//        return 90;

//        Cursor cursor = context.getContentResolver().query(selectedImage,
//                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);
//
//        if(cursor == null)
//        {
//            return 90;
//        }
//        cursor.moveToFirst();
//        return cursor.getInt(0);

        try {
            ExifInterface exif = new ExifInterface(selectedImage.getPath());
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);
            System.out.println(rotationInDegrees);
            return rotationInDegrees;
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally
        {
            return 0;
        }
    }
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }
    public static Bitmap applySaturationFilter(Bitmap source, int level) {
        // get original image size
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        float[] HSV = new float[3];
        // get pixel array from source image
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        int index = 0;
        // iteration through all pixels
        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // convert to HSV
                Color.colorToHSV(pixels[index], HSV);
                // increase Saturation level
                HSV[1] *= level;
                HSV[1] = (float) Math.max(0.0, Math.min(HSV[1], 1.0));
                // take color back
                pixels[index] = Color.HSVToColor(HSV);
            }
        }
        // output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

}
