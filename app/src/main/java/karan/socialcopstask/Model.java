package karan.socialcopstask;

import android.graphics.Bitmap;

/**
 * Created by stpl on 12/23/2016.
 */

public class Model {
    Bitmap bitmap;
    String Url;

    public Model(Bitmap bitmap, String url) {
        this.bitmap = bitmap;
        Url = url;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }


    public String getUrl() {
        return Url;
    }


}
