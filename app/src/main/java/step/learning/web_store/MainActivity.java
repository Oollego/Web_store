package step.learning.web_store;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import step.learning.web_store.adaptor.MainGroupAdaptor;
import step.learning.web_store.data.UserData;
import step.learning.web_store.orm.GroupResponse;
import step.learning.web_store.orm.MainGroupItem;

public class MainActivity extends AppCompatActivity {

//    private static final String GROUP_URL = "https://projects.heliohost.us/maingroup";
//    private static final String GROUP_IMG_URL = "https://projects.heliohost.us/img/maingroup/";
    private static final String GROUP_URL = "https://webstore.is-great.net/maingroup";
    private static final String GROUP_IMG_URL = "https://webstore.is-great.net/img/maingroup/";


    private final byte[] buffer = new byte[8096];
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    //private ArrayList<MainGroupItem> mainGroups;

//    GroupResponse groupResponse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home_btn);

        if(UserData.getBadgeNumber() > 0){
            BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.bottom_basket_btn);
            badge.setVisible(true);
            badge.setNumber(UserData.getBadgeNumber());
        }


        CompletableFuture.supplyAsync( this::loadGroups, executorService )
        .thenApplyAsync(this::processGroupResponse )
        .thenAcceptAsync(this::displayGroups);

//        <!--        <ImageView-->
//<!--            android:id="@+id/avatar"-->
//<!--            android:layout_width="46dp"-->
//<!--            android:layout_marginStart="10dp"-->
//<!--            android:layout_height="46dp"-->
//<!--            android:layout_gravity="center"-->
//<!--            android:gravity="center"-->
//<!--            android:layout_marginEnd="10dp"-->
//<!--            app:cornerRadius = "50dp"-->
//<!--            />-->
        if(UserData.getAvatar() != null){
            String AVATAR_IMG = "https://webstore.is-great.net/avatar/";
            findViewById(R.id.fragmentSearch).post(()->{
                ImageView avatarView = new ImageView(this);
                LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(
                        (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics()),
                        (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics())
                );
                avatarParams.setMarginStart((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
                avatarParams.setMarginEnd((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
                avatarParams.gravity =Gravity.CENTER;
                avatarView.setLayoutParams(avatarParams);
                String avatarUrl = AVATAR_IMG + UserData.getAvatar();

                urlToImageView(avatarUrl, avatarView);
                LinearLayout container = findViewById(R.id.linearLayoutSearch);
                container.post(() -> container.addView(avatarView));
            } );
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.bottom_home_btn){
                return true;
            } else if (item.getItemId() == R.id.bottom_basket_btn) {
                startActivity(new Intent(getApplicationContext(), BasketActivity.class ));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_user_btn) {
                startActivity(new Intent(getApplicationContext(), UserActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }

            return false;
        });
    }

//    private void displayGroups(GroupResponse groupResponse) {
//        ListView groupsList = findViewById(R.id.groupListView);
//
//        MainGroupAdaptor groupAdapter = new MainGroupAdaptor(
//                this,
//                R.layout.list_main_group,
//                groupResponse.getMainGroups()
//        );
//
//        groupsList.setAdapter(groupAdapter);
//    }

    private String loadGroups(){
//        try(InputStream groupStream = new URL( GROUP_URL ).openStream()){
//
//            return readString(groupStream);
//        }
            try{
            URL url  = new URL( GROUP_URL );

//            InputStream groupStream = url.openStream();

            URLConnection con = url.openConnection();

            //con.addRequestProperty("User-Agent", "Android");
            //con.addRequestProperty("Content-Type", "application/json");
            

            InputStream groupStream = con.getInputStream();

                return readString(groupStream);
        }
        catch (Exception ex ){
            Log.e("MainActivity::loadGroup()",
                    ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
        }
        return null;
    }

    private GroupResponse processGroupResponse (String response){

//        CompletableFuture.supplyAsync( this::loadGroups, executorService )
//                .thenApplyAsync(this::processGroupResponse )
//                .thenAcceptAsync(this::displayGroups);

        try{

            return GroupResponse.FromJsonString( response );
        }
        catch (IllegalArgumentException ex){
            Log.e("MainActivity::processGroupResponse",
                    ex.getMessage() == null ? ex.getClass().getName():ex.getMessage());
        }
        return null;
    }

    private void displayGroups(GroupResponse groupResponse) {
        //    private GroupResponse groupResponse;
//        ListView groupsList = findViewById(R.id.groupListView);

        //       groupsList = findViewById(R.id.groupListView);
//        runOnUiThread(() -> {
//                    MainGroupAdaptor groupAdapter = new MainGroupAdaptor(
//                            getApplicationContext(),
//                            R.layout.list_main_group,
//                            groupResponse.getMainGroups()
//                    );
//            groupsList.setAdapter(groupAdapter);

//                });
       // mainGroups = groupResponse.getMainGroups();
runOnUiThread(() -> {
        LinearLayout groupContainer = findViewById(R.id.groupContainer);
        Resources resources = getResources();
    Drawable mainBackground = AppCompatResources.getDrawable(
            getApplicationContext(),
            R.drawable.main_card_background);

        for (MainGroupItem mainGroup : groupResponse.getMainGroups()) {

                LinearLayout itemContainer = new LinearLayout(this);
                itemContainer.setOrientation(LinearLayout.VERTICAL);
                itemContainer.setBackground(mainBackground);
                itemContainer.setTag(mainGroup);
                //itemContainer.setBackgroundColor(resources.getColor(R.color.light_orange, null));
                itemContainer.setGravity(Gravity.CENTER);
                  LinearLayout.LayoutParams itemContainerParams = new LinearLayout.LayoutParams(
                          ViewGroup.LayoutParams.MATCH_PARENT,
                          ViewGroup.LayoutParams.WRAP_CONTENT
                  );

                    itemContainerParams.setMarginStart(10);
                    itemContainerParams.setMarginEnd(10);
                    itemContainerParams.bottomMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                    itemContainer.setLayoutParams(itemContainerParams);

                    itemContainer.setOnClickListener(this::onClickItemGroup);

                    ImageView groupImgView = new ImageView(this);
                    String url = GROUP_IMG_URL + mainGroup.getImg_mame();
                    urlToImageView(url, groupImgView);
                    LinearLayout.LayoutParams ImgViewParams = new LinearLayout.LayoutParams(
                          ViewGroup.LayoutParams.MATCH_PARENT,
                          (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 155, getResources().getDisplayMetrics())
                          );
                  groupImgView.setLayoutParams(ImgViewParams);


                  TextView textView = new TextView(this);
                  LinearLayout.LayoutParams textViewParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                  textViewParam.topMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                  textViewParam.bottomMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                  textView.setTextColor(resources.getColor(R.color.teal, null));
                  textView.setLayoutParams(textViewParam);
                  textView.setText(mainGroup.getGroupName());


            itemContainer.post(
                    ()-> itemContainer.addView(groupImgView)
            );
            itemContainer.post(
                    ()-> itemContainer.addView(textView)
            );
            groupContainer.post(
                    ()-> groupContainer.addView(itemContainer)
            );
//                    itemContainer.addView(groupImgView);
//                    itemContainer.addView(textView);
//                    groupContainer.addView(itemContainer);
            }
//    itemContainer.addView(groupImgView)
    });


//        ImageView imageView = mainGroupView.findViewById(R.id.main_group_imgV);
//        TextView textView = mainGroupView.findViewById(R.id.main_group_textV);
//
//
//
//        String url = "https://projects.heliohost.us/img/maingroup/" + mainGroup.getImg_mame();

    }
    private void onClickItemGroup(View view){
        MainGroupItem mainGroup = (MainGroupItem)view.getTag();
        Intent intent = new Intent(this, SubgroupActivity.class);
        intent.putExtra("mainGroup", mainGroup.getUriName());
        startActivity(intent);
    }
    private String readString (InputStream stream) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        int len;
        while((len = stream.read(buffer)) != -1){
            byteBuilder.write(buffer, 0, len);
        }
        String res = byteBuilder.toString();
        byteBuilder.close();
        return res;
    }
    private void urlToImageView(String url, ImageView imageView) {
        CompletableFuture
                .supplyAsync( () -> {
                    try ( java.io.InputStream is = new URL(url).openConnection().getInputStream() ) {
                        return BitmapFactory.decodeStream( is );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, executorService )
                .thenAccept( (b)->
                    runOnUiThread(() ->
                        imageView.post(
                                ()-> imageView.setImageBitmap(b)
                        )
                    )
                 );
    }

    @Override
    protected void onDestroy() {
        executorService.shutdownNow();
        super.onDestroy();
    }
}