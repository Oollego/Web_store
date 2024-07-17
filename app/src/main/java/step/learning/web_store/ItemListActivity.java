package step.learning.web_store;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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
import step.learning.web_store.orm.ItemListItem;
import step.learning.web_store.orm.ItemListResponse;

public class ItemListActivity extends AppCompatActivity {

    //private static final String GROUP_URL = "https://projects.heliohost.us//itemlist?subgroup=";
    //private static final String GROUP_IMG_URL = "https://projects.heliohost.us/img/item/";
    private static final String GROUP_URL = "https://webstore.is-great.net/itemlist?subgroup=";
    private static final String GROUP_IMG_URL = "https://webstore.is-great.net/img/item/";
    private final byte[] buffer = new byte[8096];
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Bundle arguments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.items_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        arguments = getIntent().getExtras();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewItemList);
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
    private void onClickItem(View view){
        ItemListItem item = (ItemListItem)view.getTag();
        Intent intent = new Intent(this, ItemActivity.class);
        intent.putExtra("itemId", item.getId());
        startActivity(intent);
    }
    private String loadGroups(){

        try{
            URL url  = new URL( GROUP_URL + arguments.getString("subGroup"));

            URLConnection con = url.openConnection();

            InputStream groupStream = con.getInputStream();

            return readString(groupStream);
        }
        catch (Exception ex ){
            Log.e("ItemListActivity::loadGroup()",
                    ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
        }
        return null;
    }

    private ItemListResponse processGroupResponse (String response){

        try{

            return ItemListResponse.FromJsonString( response );
        }
        catch (IllegalArgumentException ex){
            Log.e("SubgroupActivity::processGroupResponse",
                    ex.getMessage() == null ? ex.getClass().getName():ex.getMessage());
        }
        return null;
    }

    private void displayGroups(ItemListResponse ItemsResponse ) {


        runOnUiThread(() -> {
            FlexboxLayout subGroupContainer = findViewById(R.id.itemListContainer);
            Resources resources = getResources();
//            Drawable mainBackground = AppCompatResources.getDrawable(
//                    getApplicationContext(),
//                    R.drawable.main_card_background);

            for (ItemListItem item : ItemsResponse.getItemList()) {

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
                itemContainer.setTag(item);
                itemContainer.setLayoutParams(itemContainerParams);

                itemContainer.setOnClickListener(this::onClickItem);

                ImageView groupImgView = new ImageView(this);
                String url = GROUP_IMG_URL + item.getImg();

                urlToImageView(url, groupImgView);

                LinearLayout.LayoutParams ImgViewParams = new LinearLayout.LayoutParams(
                        (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics()),
                        (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics())
                );
                groupImgView.setLayoutParams(ImgViewParams);

                RatingBar ratingBar = new RatingBar(this, null, android.R.attr.ratingBarStyleSmall);
                LinearLayout.LayoutParams ratingBarParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ratingBarParams.topMargin = ((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
                ratingBar.setLayoutParams(ratingBarParams);

                ratingBar.setRating((float)item.getFeedback_avg());
                ratingBar.setNumStars(5);
                ratingBar.setStepSize(0.1f);

                ratingBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yellow)));


                TextView textView = new TextView(this);
                LinearLayout.LayoutParams textViewParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                textViewParam.topMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                textView.setTextColor(resources.getColor(R.color.teal, null));
                textView.setLayoutParams(textViewParam);
                textView.setText(item.getItemName());


                LinearLayout priceContainer = new LinearLayout(this);
                LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                priceParams.bottomMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                priceParams.gravity = Gravity.CENTER;
                priceContainer.setOrientation(LinearLayout.HORIZONTAL);
                priceContainer.setLayoutParams(priceParams);

                TextView priceTv = new TextView(this);
                LinearLayout.LayoutParams saleTvParams = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                saleTvParams.weight = 1;
                priceTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                priceTv.setLayoutParams(saleTvParams);
                priceTv.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                priceTv.setTextSize(getIntFromSp(8));
                priceTv.setTextColor(resources.getColor(R.color.black, null));
                priceTv.setText(String.format("%s ₴", (int)item.getPrice()));


                if ( item.getSalePrice() != 0){

                    TextView saleTv = new TextView(this);

                    saleTvParams.setMarginStart(getIntFromDp(4));
                    saleTv.setLayoutParams(saleTvParams);
                    saleTv.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                    saleTv.setTextSize(getIntFromSp(9));
                    saleTv.setTextColor(resources.getColor(R.color.red, null));
                    saleTv.setText(String.format("%s", (int)item.getSalePrice()));

                    priceContainer.post(()-> priceContainer.addView(saleTv));
                    priceTv.setPaintFlags(priceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }

                subGroupContainer.post(()->{
                    itemContainer.addView(groupImgView);
                    itemContainer.addView(ratingBar);
                    itemContainer.addView(textView);
                    priceContainer.addView(priceTv);
                    itemContainer.addView(priceContainer);
                    subGroupContainer.addView(itemContainer);
                });

            }

        });
    }

    private int getIntFromDp(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
    private int getIntFromSp(int sp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
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