package step.learning.web_store;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import step.learning.web_store.data.UserData;
import step.learning.web_store.orm.MainGroupItem;
import step.learning.web_store.orm.SubGroupItem;
import step.learning.web_store.orm.SubGroupResponse;

public class SubgroupActivity extends AppCompatActivity {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    //private static final String GROUP_URL = "https://projects.heliohost.us/subgroup?maingroup=";
    //private static final String GROUP_IMG_URL = "https://projects.heliohost.us/img/subgroup/";
    private static final String GROUP_URL = "https://webstore.is-great.net/subgroup?maingroup=";
    private static final String GROUP_IMG_URL = "https://webstore.is-great.net/img/subgroup/";
    private final byte[] buffer = new byte[8096];
    private Bundle arguments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_subgroup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainSub), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        arguments = getIntent().getExtras();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewSubgroup);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home_btn);

        if(UserData.getBadgeNumber() > 0){
            BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.bottom_basket_btn);
            badge.setVisible(true);
            badge.setNumber(UserData.getBadgeNumber());
        }

        CompletableFuture.supplyAsync( this::loadGroups, executorService )
                .thenApplyAsync(this::processGroupResponse )
                .thenAcceptAsync(this::displayGroups);



        bottomNavigationView.setOnItemSelectedListener(item ->
                {
                    if (item.getItemId() == R.id.bottom_home_btn) {
                        return true;
                    } else if (item.getItemId() == R.id.bottom_basket_btn) {
                        startActivity(new Intent(getApplicationContext(), BasketActivity.class));
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
    private void onClickItemSubGroup(View view){
        SubGroupItem subGroup = (SubGroupItem)view.getTag();
        Intent intent = new Intent(this, ItemListActivity.class);
        intent.putExtra("subGroup", subGroup.getUriName());
        startActivity(intent);
    }
    private String loadGroups(){

        try{
            URL url  = new URL( GROUP_URL + arguments.getString("mainGroup"));

            URLConnection con = url.openConnection();

            InputStream groupStream = con.getInputStream();

            return readString(groupStream);
        }
        catch (Exception ex ){
            Log.e("SubGroupActivity::loadGroup()",
                    ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
        }
        return null;
    }

    private SubGroupResponse processGroupResponse (String response){

        try{

            return SubGroupResponse.FromJsonString( response );
        }
        catch (IllegalArgumentException ex){
            Log.e("SubgroupActivity::processGroupResponse",
                    ex.getMessage() == null ? ex.getClass().getName():ex.getMessage());
        }
        return null;
    }

    private void displayGroups(SubGroupResponse groupResponse ) {


        runOnUiThread(() -> {
            FlexboxLayout subGroupContainer = findViewById(R.id.subGroupContainer);
            Resources resources = getResources();
//            Drawable mainBackground = AppCompatResources.getDrawable(
//                    getApplicationContext(),
//                    R.drawable.main_card_background);

            for (SubGroupItem subGroup : groupResponse.getSubGroups()) {

                LinearLayout itemContainer = new LinearLayout(this);
                itemContainer.setOrientation(LinearLayout.VERTICAL);

               // itemContainer.setBackground(mainBackground);

                //itemContainer.setBackgroundColor(resources.getColor(R.color.light_orange, null));
                itemContainer.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams itemContainerParams = new LinearLayout.LayoutParams(
                        (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics()),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                itemContainerParams.topMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
                itemContainer.setTag(subGroup);
                itemContainer.setLayoutParams(itemContainerParams);

                itemContainer.setOnClickListener(this::onClickItemSubGroup);

                ImageView groupImgView = new ImageView(this);
                String url = "https://projects.heliohost.us/img/subgroup/" + subGroup.getImg_mame();

                urlToImageView(url, groupImgView);

                LinearLayout.LayoutParams ImgViewParams = new LinearLayout.LayoutParams(
                        (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics()),
                        (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics())
                );
                groupImgView.setLayoutParams(ImgViewParams);


                TextView textView = new TextView(this);
                LinearLayout.LayoutParams textViewParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                textViewParam.topMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                textViewParam.bottomMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                textView.setTextColor(resources.getColor(R.color.teal, null));
                textView.setLayoutParams(textViewParam);
                textView.setText(subGroup.getSubGroupName());


                subGroupContainer.post(()->{
                    itemContainer.addView(groupImgView);
                    itemContainer.addView(textView);
                    subGroupContainer.addView(itemContainer);
                });

            }

        });
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
                        imageView.post( ()-> imageView.setImageBitmap(b) )
                    )
                 );
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
}