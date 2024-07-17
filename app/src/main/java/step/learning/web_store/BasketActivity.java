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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;
import org.w3c.dom.Text;

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

import step.learning.web_store.data.DataTools;
import step.learning.web_store.data.UserData;
import step.learning.web_store.orm.BasketItem;
import step.learning.web_store.orm.BasketResponse;
import step.learning.web_store.orm.ItemFeedback;
import step.learning.web_store.orm.ItemResponse;

public class BasketActivity extends AppCompatActivity {

    private static final String BASKET_GET_URL = "https://webstore.is-great.net/basket?sessionid=";
    private static final String BASKET_IMG_URL = "https://webstore.is-great.net/img/item/";
    private static final String BASKET_URL = "https://webstore.is-great.net/basket";

    private final byte[] buffer = new byte[8096];
    private int textViewQuantityId;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_basket);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.basket), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_basket_btn);

        if(UserData.getBadgeNumber() > 0){
            BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.bottom_basket_btn);
            badge.setVisible(true);
            badge.setNumber(UserData.getBadgeNumber());
        }

        if(UserData.getId() == null){
            displayEmptyBasket();
        }else{
            CompletableFuture.supplyAsync( this::loadBasket, executorService )
                    .thenApplyAsync(this::processBasketResponse )
                    .thenAcceptAsync(this::displayBasket);
        }

        if(UserData.getBadgeNumber() > 0){
            BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.bottom_basket_btn);
            badge.setVisible(true);
            badge.setNumber(UserData.getBadgeNumber());
        }


        bottomNavigationView.setOnItemSelectedListener(item ->

        {
            if(item.getItemId() == R.id.bottom_home_btn){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_basket_btn) {

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

    private String loadBasket(){

        try{
            URL url  = new URL( BASKET_GET_URL + UserData.getId());

            URLConnection con = url.openConnection();

            InputStream groupStream = con.getInputStream();

            return DataTools.readString(groupStream);
        }
        catch (Exception ex ){
            Log.e("BasketActivity::loadGroup()",
                    ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
        }
        return null;
    }

    private BasketResponse processBasketResponse (String response){

        try{

            return BasketResponse.FromJsonString( response );
        }
        catch (IllegalArgumentException ex){
            Log.e("BasketActivity::processBasketResponse",
                    ex.getMessage() == null ? ex.getClass().getName():ex.getMessage());
        }
        return null;
    }

    private void deleteBasketItemClick(View view){

        String itemId = (String)view.getTag();
        new AlertDialog.Builder(this)
                .setMessage("Видалити товар?")
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) ->{
                    CompletableFuture.supplyAsync( () -> deleteBasketItem(itemId), executorService )
                            .thenAcceptAsync(this::deleteResponds);
                    finish();
                })
                .setNegativeButton("No", ((dialog, which) -> finish()))
                .show();

    }

    private String deleteBasketItem(String itemId){
        String respond;
        try{
            String urlString = BASKET_URL + String.format(
                    "?item_id=%s&user_id=%s",
                    URLEncoder.encode( itemId, StandardCharsets.UTF_8.name()),
                    URLEncoder.encode( UserData.getId(), StandardCharsets.UTF_8.name())
            );
            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setChunkedStreamingMode( 0 );
            connection.setDoOutput( true );
            connection.setDoInput( true );
            connection.setRequestMethod( "DELETE" );

            connection.setRequestProperty( "Accept", "application/json" );
            connection.setRequestProperty( "Content_Type", "application/x-www-form-urlencoded" );
            connection.setRequestProperty( "Connection", "close" );

            OutputStream  connectionOutput = connection.getOutputStream();

            connectionOutput.flush();

            connectionOutput.close();

            int statusCode = connection.getResponseCode();

            if( statusCode == 200 ){
                InputStream connectionInput = connection.getInputStream();

                respond = DataTools.readString(connectionInput);
                connectionInput.close();
            }
            else{
                InputStream connectionInput = connection.getErrorStream();
                respond = DataTools.readString( connectionInput) ;
                connectionInput.close();
                Log.e("LogInMessage", respond);
            }
            connection.disconnect();
            return respond;
        }
        catch(Exception ex){
            Log.e("LogInMessage", ex.getMessage());
        }
        return null;
    }

    private void deleteResponds(String jsonString){
        int status;
        try{
            JSONObject root = new JSONObject(jsonString);
            status = root.getInt("status");
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        runOnUiThread(() -> {
            if(status == 1) {
                startActivity(new Intent(getApplicationContext(), BasketActivity.class));
                finish();
            }
        });
    }
    private void removeOnClick(View view){

        textViewQuantityId = (int)view.getTag("textViewId".hashCode());
        String itemId = (String)view.getTag("itemId".hashCode());
//        //ConstraintLayout basketItemContainer =  btnView.getRootView().findViewWithTag("quantityTv");
//        TextView textV = btnView.getRootView().findViewWithTag("quantityTv");
        TextView textV = findViewById(textViewQuantityId);
        String quantity = textV.getText().toString();
        int i = Integer.parseInt(quantity);
        if(i == 0){
            return;
        }
        CompletableFuture.supplyAsync(() -> addOrRemove(itemId, "sub"), executorService )
                .thenAcceptAsync(this::addOrRemoveResponse);
    }
    private void addOnClick(View view){

        textViewQuantityId = (int)view.getTag("textViewId".hashCode());
        String itemId = (String)view.getTag("itemId".hashCode());

        CompletableFuture.supplyAsync( ()-> addOrRemove(itemId, "add"), executorService )
                .thenAcceptAsync(this::addOrRemoveResponse);
    }

    private String addOrRemove(String itemId, String method){
        String respond;
        try{
            String urlString = BASKET_URL + String.format(
                    "?item_id=%s&user_id=%s&AddOrSub=%s",
                    URLEncoder.encode( itemId, StandardCharsets.UTF_8.name()),
                    URLEncoder.encode( UserData.getId(), StandardCharsets.UTF_8.name()),
                    URLEncoder.encode( method, StandardCharsets.UTF_8.name())
            );
            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setChunkedStreamingMode( 0 );
            connection.setDoOutput( true );
            connection.setDoInput( true );
            connection.setRequestMethod( "PUT" );

            connection.setRequestProperty( "Accept", "application/json" );
            connection.setRequestProperty( "Content_Type", "application/x-www-form-urlencoded" );
            connection.setRequestProperty( "Connection", "close" );

            OutputStream  connectionOutput = connection.getOutputStream();

            connectionOutput.flush();

            connectionOutput.close();

            int statusCode = connection.getResponseCode();

            if( statusCode == 200 ){
                InputStream connectionInput = connection.getInputStream();

                respond = DataTools.readString(connectionInput);
                connectionInput.close();
            }
            else{
                InputStream connectionInput = connection.getErrorStream();
                respond = DataTools.readString( connectionInput) ;
                connectionInput.close();
                Log.e("LogInMessage", respond);
            }
            connection.disconnect();
            return respond;
        }
        catch(Exception ex){
            Log.e("LogInMessage", ex.getMessage());
        }
        return null;
    }
    private void addOrRemoveResponse(String jsonString){
        int status;
        String method;
        try{
            JSONObject root = new JSONObject(jsonString);
            status = root.getInt("status");
            method = root.getString("method");
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        if(status == 1){
            runOnUiThread(() -> {
//                LinearLayout mainBasketContainer = findViewById(R.id.basket_items_container);
               // ConstraintLayout basketItemContainer = (ConstraintLayout) btnView.getRootView();
                TextView textV = findViewById(textViewQuantityId);
                String quantity = textV.getText().toString();
                int i = Integer.parseInt(quantity);
                if(method.equals("add")){
                   i++;
                   textV.setText(String.format("%s", i));
                }
                if(method.equals("sub")){
                    if( i==0 )
                    {
                        return;
                    }
                    i--;
                    textV.setText(String.format("%s", i));
                }

            });
        }

    }
    private void displayBasket(BasketResponse BasketResponse ) {

        if(BasketResponse.getBasketItems().isEmpty()){
            displayEmptyBasket();
            return;
        }
        runOnUiThread(() -> {
            Resources resources = getResources();
            UserData.setBadgeNumber(BasketResponse.getBasketItems().size());
            for(BasketItem item : BasketResponse.getBasketItems()) {
                ConstraintLayout basketItemContainer = new ConstraintLayout(this);
                LinearLayout.LayoutParams basketItemParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                int dp20 = getIntFromDp(20);
                int dp10 = getIntFromDp(10);
                basketItemParams.setMargins(dp20, dp10, dp20, dp10);
                basketItemContainer.setLayoutParams(basketItemParams);
                basketItemContainer.setPadding(dp10, dp10, dp10, dp10);
                basketItemContainer.setBackground(getDrawableResource(R.drawable.review_background));
                basketItemContainer.setTag(item.getId());

                ImageView imageView = new ImageView(this);
                ConstraintLayout.LayoutParams imageParams = new ConstraintLayout.LayoutParams(
                        getIntFromDp(100),
                        getIntFromDp(100)
                );
                imageParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                imageParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                imageParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                imageView.setLayoutParams(imageParams);
                imageView.setId(View.generateViewId());
                String imageLargeUrl = BASKET_IMG_URL + item.getImage_name();
                urlToImageView(imageLargeUrl, imageView);

                TextView itemNameTextView = new TextView(this);
                itemNameTextView.setId(View.generateViewId());
                ConstraintLayout.LayoutParams itemNameParams = new ConstraintLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                itemNameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                itemNameParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                itemNameParams.startToEnd = imageView.getId();
                itemNameParams.setMarginStart(dp10);
                itemNameTextView.setLayoutParams(itemNameParams);
                itemNameTextView.setText(item.getItemName());
                itemNameTextView.setTextColor(resources.getColor(R.color.teal, null));

                int dp25 = getIntFromDp(25);
                int dp5 = getIntFromDp(5);

                int quantityTvId = View.generateViewId();
                Button addBtn = new Button(this);
                addBtn.setId(View.generateViewId());
                ConstraintLayout.LayoutParams addBtnParams = new ConstraintLayout.LayoutParams(
                        dp25,
                        dp25
                );
                addBtnParams.startToEnd = imageView.getId();
                addBtnParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                addBtnParams.setMarginStart(getIntFromDp(15));
                addBtnParams.bottomMargin = dp5;
                addBtn.setLayoutParams(addBtnParams);
                addBtn.setBackground(getDrawableResource(R.drawable.btn_add));
                addBtn.setTag("itemId".hashCode(), item.getId());
                addBtn.setTag("textViewId".hashCode(), quantityTvId);
                addBtn.setOnClickListener(this::addOnClick);

                TextView quantityTv = new TextView(this);
                quantityTv.setTag("quantityTv");
                quantityTv.setId(quantityTvId);
                ConstraintLayout.LayoutParams quantityTvParams = new ConstraintLayout.LayoutParams(
                        getIntFromDp(50),
                        dp25
                );
                quantityTvParams.startToEnd = addBtn.getId();
                quantityTvParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                quantityTvParams.bottomMargin = dp5;
                quantityTv.setLayoutParams(quantityTvParams);
                quantityTv.setGravity(Gravity.CENTER);
                quantityTv.setText(String.format("%s", item.getQuantity()));

                Button removeBtn = new Button(this);
                ConstraintLayout.LayoutParams removeBtnParams = new ConstraintLayout.LayoutParams(
                        dp25,
                        dp25
                );
                removeBtnParams.startToEnd = quantityTv.getId();
                removeBtnParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                removeBtnParams.bottomMargin = dp5;
                removeBtn.setLayoutParams(removeBtnParams);
                removeBtn.setBackground(getDrawableResource(R.drawable.btn_remove));
                removeBtn.setTag("itemId".hashCode(), item.getId());
                removeBtn.setTag("textViewId".hashCode(), quantityTvId);
                removeBtn.setOnClickListener(this::removeOnClick);

                Button itemDeleteBtn = new Button(this);
                ConstraintLayout.LayoutParams itemDeleteParams = new ConstraintLayout.LayoutParams(
                        dp25,
                        dp25
                );
                itemDeleteParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                itemDeleteParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                itemDeleteParams.setMargins(dp5, dp5, dp5, dp5);
                itemDeleteBtn.setLayoutParams(itemDeleteParams);
                itemDeleteBtn.setBackground(getDrawableResource(R.drawable.delete_24));
                itemDeleteBtn.setTag(item.getId());
                itemDeleteBtn.setOnClickListener(this::deleteBasketItemClick);

                LinearLayout priceContainer = new LinearLayout(this);
                ConstraintLayout.LayoutParams priceParams = new ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                priceParams.setMarginStart(dp10);
                priceParams.topMargin = dp5;
                priceParams.startToEnd = imageView.getId();
                priceParams.topToBottom = itemNameTextView.getId();
                //priceParams.bottomMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                //priceParams.gravity = Gravity.CENTER;
                priceContainer.setOrientation(LinearLayout.HORIZONTAL);
                priceContainer.setLayoutParams(priceParams);

                TextView priceTv = new TextView(this);
                LinearLayout.LayoutParams saleTvParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                priceTv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                priceTv.setLayoutParams(saleTvParams);
                priceTv.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                priceTv.setTextSize(getIntFromSp(6));
                priceTv.setTextColor(resources.getColor(R.color.black, null));
                priceTv.setText(String.format("%s ₴", (int)item.getPrice()));

                priceContainer.post(()-> priceContainer.addView(priceTv));
                if ( item.getSalePrice() != 0){

                    TextView saleTv = new TextView(this);

                    saleTvParams.setMarginStart(getIntFromDp(6));
                    saleTv.setLayoutParams(saleTvParams);
                    saleTv.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                    saleTv.setTextSize(getIntFromSp(7));
                    saleTv.setTextColor(resources.getColor(R.color.red, null));
                    saleTv.setText(String.format("%s", (int)item.getSalePrice()));

                    priceContainer.post(()-> priceContainer.addView(saleTv));
                    priceTv.setPaintFlags(priceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }

                LinearLayout mainBasketContainer = findViewById(R.id.basket_items_container);
                mainBasketContainer.post(()->{
                    basketItemContainer.addView(imageView);
                    basketItemContainer.addView(itemNameTextView);
                    basketItemContainer.addView(addBtn);
                    basketItemContainer.addView(quantityTv);
                    basketItemContainer.addView(removeBtn);
                    basketItemContainer.addView(itemDeleteBtn);
                    basketItemContainer.addView(priceContainer);
                    mainBasketContainer.addView(basketItemContainer);
                });
            }
        });
    }
    private void displayEmptyBasket(){

        ImageView emptyIv = new ImageView(this);
        LinearLayout.LayoutParams emptyIvParams = new LinearLayout.LayoutParams(
                getIntFromDp(200),
                getIntFromDp(200)
        );
        emptyIvParams.gravity = Gravity.CENTER;
        emptyIvParams.topMargin = getIntFromDp(100);
        emptyIv.setLayoutParams(emptyIvParams);
        emptyIv.setImageDrawable(getDrawableResource(R.raw.basket_emp));
        LinearLayout mainBasketContainer = findViewById(R.id.basket_items_container);
        mainBasketContainer.post(()-> mainBasketContainer.addView( emptyIv));
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

}