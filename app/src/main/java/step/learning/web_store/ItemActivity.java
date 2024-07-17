package step.learning.web_store;



import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import step.learning.web_store.BasketActivity;
import step.learning.web_store.R;
import step.learning.web_store.UserActivity;
import step.learning.web_store.data.UserData;
import step.learning.web_store.orm.ItemFeedback;
import step.learning.web_store.orm.ItemResponse;

public class ItemActivity extends AppCompatActivity {

    //private static final String GROUP_URL = "https://projects.heliohost.us/itemid?id=";
    //private static final String GROUP_IMG_URL = "https://projects.heliohost.us/img/item/";
    private static final String GROUP_URL = "https://webstore.is-great.net/itemid?id=";
    private static final String GROUP_IMG_URL = "https://webstore.is-great.net/img/item/";
    private static final String BASKET_URL = "https://webstore.is-great.net/basket";
    private final byte[] buffer = new byte[8096];
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Bundle arguments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.item_main), (v, insets) -> {
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

    private void onClickBuy(View view){

        if (UserData.getId() == null){
            new AlertDialog.Builder(this)
                    .setMessage("Потрібна реєстрація")
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        startActivity(new Intent(getApplicationContext(), UserActivity.class));
                        finish();
                    })
                    .show();
        }
        String itemId = (String)view.getTag();

        CompletableFuture.supplyAsync( () -> addItemToBasket(itemId), executorService )
                .thenAcceptAsync(this::basketResponseMassage);
    }

    private String addItemToBasket(String itemId){
        try {

            URL url = new URL (BASKET_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setChunkedStreamingMode( 0 );
            connection.setDoOutput( true );
            connection.setDoInput( true );
            connection.setRequestMethod( "POST" );

            connection.setRequestProperty( "Accept", "application/json" );
            connection.setRequestProperty( "Content_Type", "application/x-www-form-urlencoded" );
            connection.setRequestProperty( "Connection", "close" );

            OutputStream connectionOutput = connection.getOutputStream();
            String body = String.format(
                    "item_id=%s&user_id=%s",
                    URLEncoder.encode( itemId, StandardCharsets.UTF_8.name()),
                    URLEncoder.encode( UserData.getId(), StandardCharsets.UTF_8.name())
            );
            connectionOutput.write( body.getBytes( StandardCharsets.UTF_8 ) );

            connectionOutput.flush();

            connectionOutput.close();

            int statusCode = connection.getResponseCode();

            if( statusCode == 200 ){
                InputStream basketStream = connection.getInputStream();
                return readString(basketStream);
            }
            else{
                InputStream connectionInput = connection.getErrorStream();
                body = readString( connectionInput) ;
                connectionInput.close();
                Log.e("AddToBasketMessage", body);
            }
            connection.disconnect();
        }
        catch(Exception ex){
            Log.e("AddToBasketMessage", ex.getMessage());
        }
        return null;
    }

    private void basketResponseMassage(String jsonString){
        int status;
        try{
            JSONObject root = new JSONObject(jsonString);
            status = root.getInt("status");
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        runOnUiThread(() -> {
            if(status == 1){
                int badge = UserData.getBadgeNumber();
                badge++;
                UserData.setBadgeNumber(badge);
                new AlertDialog.Builder(this)
                        .setMessage("Товар у кошику")
                        .setCancelable(false)
                        .setPositiveButton("Ok", (dialog, which) -> finish())
                        .show();

            }
         else{
                new AlertDialog.Builder(this)
                        .setMessage("Помилка сервера")
                        .setCancelable(false)
                        .setPositiveButton("Ok", (dialog, which) -> finish())
                        .show();
            }
        });

    }

    private String loadGroups(){

        try{
            URL url  = new URL( GROUP_URL + arguments.getString("itemId"));

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

    private ItemResponse processGroupResponse (String response){

        try{

            return ItemResponse.FromJsonString( response );
        }
        catch (IllegalArgumentException ex){
            Log.e("SubgroupActivity::processGroupResponse",
                    ex.getMessage() == null ? ex.getClass().getName():ex.getMessage());
        }
        return null;
    }
    private int getIntFromDp(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
    private int getIntFromSp(int sp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    private Drawable getDrawableResource (int drawableInt){
        return AppCompatResources.getDrawable(this, drawableInt);
    }
    private void smallImageViewOnClick (View view){
        ImageView largeImage = findViewById(R.id.largeItemImgV);
        ImageView smallImage = (ImageView) view;
        largeImage.setImageDrawable(smallImage.getDrawable());
    }

    private TextView getFeatureTextView(float weight){

        TextView textView = new TextView(this);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textViewParams.weight = weight;
        textView.setLayoutParams(textViewParams);
        textView.setPadding(getIntFromDp(10), getIntFromDp(5), getIntFromDp(10), getIntFromDp(5));
        textView.setTextSize(getIntFromSp(6));

        return textView;
    }

    private void displayGroups(ItemResponse itemResponse ) {


        runOnUiThread(() -> {
//            FlexboxLayout subGroupContainer = findViewById(R.id.itemListContainer);
            Resources resources = getResources();

            LinearLayout smallImagesContainer = findViewById(R.id.small_img);
            for(String image : itemResponse.getImages()){
                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(
                        getIntFromDp(60),
                        getIntFromDp(60)
                );
                imageViewParams.setMarginEnd(getIntFromDp(10));
                imageView.setLayoutParams(imageViewParams);
                int paddingInt = getIntFromDp(5);
                imageView.setPadding(paddingInt, paddingInt, paddingInt, paddingInt);
                imageView.setBackground(getDrawableResource(R.drawable.item_small_img_background));
                String imageUrl = GROUP_IMG_URL + image;
                urlToImageView(imageUrl, imageView);
                imageView.setOnClickListener(this::smallImageViewOnClick);

                smallImagesContainer.post(()-> smallImagesContainer.addView(imageView));
            }

            ImageView largeImage = findViewById(R.id.largeItemImgV);
            String imageLargeUrl = GROUP_IMG_URL + itemResponse.getImages().get(0);
            urlToImageView(imageLargeUrl, largeImage);

            TextView title = findViewById(R.id.titleView);
            title.setText(itemResponse.getItem().getItemName());

            RatingBar mainRatingBar = findViewById(R.id.ratingBar);
            mainRatingBar.setRating((float)itemResponse.getAvgRating());

            TextView quantityReviewTv = findViewById(R.id.quantityReviewTV);
            quantityReviewTv.setText(String.format("%s відгуки", itemResponse.getQuantityReview()));

            TextView inStock = findViewById(R.id.inStockTV);
            if(itemResponse.getItem().getIsActive())
            {
                inStock.setText("Є в наявності");
                inStock.setTextColor(resources.getColor(R.color.green, null));
            }
            else
            {
                inStock.setText("Немає в наявності");
                inStock.setTextColor(resources.getColor(R.color.grey, null));
            }

            TextView priceTv = findViewById(R.id.textV_price);
            ConstraintLayout mainConstraint = findViewById(R.id.main_constraint);
            priceTv.setText(String.format("%s", (int)itemResponse.getItem().getPrice()));
            if ( itemResponse.getItem().getSalePrice() != 0){
                priceTv.setText(String.format("%s", (int)itemResponse.getItem().getPrice()));

                TextView saleTv = new TextView(this);
                ConstraintLayout.LayoutParams saleTvParams = new ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        getIntFromDp(40)
                );
                saleTvParams.setMarginStart(getIntFromDp(20));
                saleTvParams.topMargin = getIntFromDp(20);
                saleTvParams.startToEnd = priceTv.getId();
                saleTvParams.topToBottom = mainRatingBar.getId();
                saleTv.setLayoutParams(saleTvParams);
                saleTv.setGravity(Gravity.CENTER);
                saleTv.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                saleTv.setTextSize(getIntFromSp(12));
                saleTv.setTextColor(resources.getColor(R.color.red, null));
                saleTv.setText(String.format("%s", (int)itemResponse.getItem().getSalePrice()));
                mainConstraint.post(()-> mainConstraint.addView(saleTv));

                priceTv.setPaintFlags(priceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            Button buyBtn = findViewById(R.id.buyButton);
            buyBtn.setTag(itemResponse.getItem().getId());
            buyBtn.setOnClickListener(this::onClickBuy);

            TextView description = findViewById(R.id.textV_description_body);
            description.setText(itemResponse.getItem().getDescription());

            LinearLayout table = findViewById(R.id.featuresTable);
//            TableLayout table = new TableLayout(this);
            LinearLayout.LayoutParams tableParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
//            table.setLayoutParams(tableParams);

            for (int i = 0; i < itemResponse.getFeatures().size(); i++ ) {
                LinearLayout tableRow = new LinearLayout(this);
                tableRow.setLayoutParams(tableParams);
                tableRow.setOrientation(LinearLayout.HORIZONTAL);

                TextView textViewLeft = new TextView(this);
                LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                        getIntFromDp(130),
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                textViewLeft.setLayoutParams(textViewParams);

                textViewLeft.setPadding(getIntFromDp(10), getIntFromDp(5), getIntFromDp(3), getIntFromDp(5));
                textViewLeft.setTextSize(getIntFromSp(6));
                textViewLeft.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                textViewLeft.setGravity(Gravity.CENTER);

//                TextView textViewLeft = getFeatureTextView(0.5f);
                textViewLeft.setText(itemResponse.getFeatures().get(i).getFeatureName());

                TextView textViewRight = getFeatureTextView(1f);
                textViewRight.setText(itemResponse.getFeatures().get(i).getFeatureText());
                if( i%2 == 0 ){
                    textViewLeft.setBackground(getDrawableResource(R.drawable.feature_gray));
                    tableRow.setBackground(getDrawableResource(R.color.light_grey));
                  //  textViewRight.setBackground(getDrawableResource(R.color.light_grey));
                }
                else
                {
                    textViewLeft.setBackground(getDrawableResource(R.drawable.feature_white));
                }
//                   table.addView(textViewLeft);
//                    table.addView(textViewRight);
//                    table.addView(tableRow);
                table.post(()-> {
                    tableRow.addView(textViewLeft);
                    tableRow.addView(textViewRight);
                    table.addView(tableRow);
                });
            }
            LinearLayout reviewMainContainer = findViewById(R.id.container_review);
            for(ItemFeedback feedback : itemResponse.getFeedbacks()) {
                ConstraintLayout reviewContainer = new ConstraintLayout(this);
                ConstraintLayout.LayoutParams reviewContainerParams = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                );
                int reviewMargin = getIntFromDp(15);
                int reviewPadding = getIntFromDp(10);
                reviewContainerParams.setMargins(reviewMargin, reviewMargin, reviewMargin, reviewMargin);
                reviewContainer.setLayoutParams(reviewContainerParams);
                reviewContainer.setPadding(reviewPadding, reviewPadding, reviewPadding, reviewPadding);
                reviewContainer.setBackground(getDrawableResource(R.drawable.review_background));

                TextView reviewTextView = new TextView(this);
                ConstraintLayout.LayoutParams reviewTvParams = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                );
                reviewTvParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                reviewTvParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                reviewTvParams.setMargins(getIntFromDp(10), getIntFromDp(15), getIntFromDp(10), 0);
                reviewTextView.setLayoutParams(reviewTvParams);
                reviewTextView.setId(View.generateViewId());
                reviewTextView.setText(feedback.getDate());

                RatingBar reviewRatingBar = new RatingBar(this, null, android.R.attr.ratingBarStyleIndicator);
                ConstraintLayout.LayoutParams reviewRbParams = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        getIntFromDp(40)
                );
                reviewRbParams.setMarginStart(getIntFromDp(4));
                reviewRbParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                reviewRbParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                reviewRatingBar.setLayoutParams(reviewRbParams);
                reviewRatingBar.setRating((float)feedback.getScore());
                reviewRatingBar.setScaleX(0.5f);
                reviewRatingBar.setScaleY(0.5f);
                reviewRatingBar.setProgressTintList(ColorStateList.valueOf(resources.getColor(R.color.yellow, null)));

                TextView reviewComTv = new TextView(this);
                ConstraintLayout.LayoutParams reviewComTvParams = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                );
                reviewComTvParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                reviewComTvParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                reviewComTvParams.topToBottom = reviewTextView.getId();
                reviewComTvParams.setMargins(getIntFromDp(10), getIntFromDp(10), getIntFromDp(10), getIntFromDp(15));
                reviewComTv.setLayoutParams(reviewComTvParams);
                reviewComTv.setText(feedback.getComment());
                reviewComTv.setTextSize(getIntFromSp(6));
                reviewComTv.setTextAlignment(View.TEXT_ALIGNMENT_INHERIT);


                reviewMainContainer.post(()->{
                    reviewContainer.addView(reviewTextView);
                    reviewContainer.addView(reviewRatingBar);
                    reviewContainer.addView(reviewComTv);
                    reviewMainContainer.addView(reviewContainer);
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