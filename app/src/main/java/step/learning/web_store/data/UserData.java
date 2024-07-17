package step.learning.web_store.data;

import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import step.learning.web_store.R;

public class UserData {
    static private String id;
    static private String avatar;
    static private int badgeNumber = 0;


    public static int getBadgeNumber() {
        return badgeNumber;
    }

    public static void setBadgeNumber(int badgeNumber) {
        UserData.badgeNumber = badgeNumber;
    }

    public static String getAvatar() {
        return avatar;
    }

    public static void setAvatar(String avatar) {
        UserData.avatar = avatar;

    }

    public static String getId() {
        return id;
    }

    public static void setId(String id) {
        UserData.id = id;
    }
}
