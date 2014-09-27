package simpzan.android.lyrics;

import android.util.Log;

/**
 * Created by simpzan on 9/16/14.
 */
public class Utils {
    public static void print(Object obj) {
        Log.e("simpzan", obj.toString());
    }

    public static String fileWithExtensionSubstituted(String file, String ext) {
        int pos = file.lastIndexOf(".");
        if (pos < 0) return null;
        return file.substring(0, pos) + "." + ext;
    }
}
