package step.learning.web_store;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import step.learning.web_store.data.DataTools;
import step.learning.web_store.data.UserData;

public class UserActivity extends AppCompatActivity {

    private static final String AVT_URL = "https://webstore.is-great.net/auth";
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_user_btn);

        if(UserData.getBadgeNumber() > 0){
            BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.bottom_basket_btn);
            badge.setVisible(true);
            badge.setNumber(UserData.getBadgeNumber());
        }

        Button regBtn = findViewById(R.id.regActivityButton);
        regBtn.setOnClickListener(this::registrationBtnClick);

        ConstraintLayout mainConstraint = findViewById(R.id.user);
        mainConstraint.setOnClickListener((v) -> hideSoftInput());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.bottom_home_btn){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_basket_btn) {
                startActivity(new Intent(getApplicationContext(), BasketActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_user_btn) {
                return true;
            }

            return false;
        });

        Button authBtn = findViewById(R.id.avtButton);
        authBtn.setOnClickListener(this::avtBtnClick);
    }
    private void hideSoftInput(){
        View focusedView = getCurrentFocus();
        if(focusedView != null){
            InputMethodManager manager = (InputMethodManager)
                    getSystemService( Context.INPUT_METHOD_SERVICE );
            manager.hideSoftInputFromWindow( focusedView.getWindowToken(), 0 );
            focusedView.clearFocus();
        }
    }
  private void avtBtnClick (View view){
      TextInputEditText emailTextField = findViewById(R.id.emailTextField);
      TextInputEditText passTextField = findViewById(R.id.passwordTextField);

      String email;
      String password;
      if(emailTextField.getText() != null && passTextField.getText() != null){
          email = emailTextField.getText().toString();
          password = passTextField.getText().toString();
      }
      else {
          return;
      }

      CompletableFuture
              .supplyAsync( ()-> userLogIn( email, password ), executorService)
              .thenAcceptAsync(this::processLogIn);

    }
  private void registrationBtnClick(View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
  }

   private String userLogIn(String email, String password){
       String respond;
       try{
           String urlString = AVT_URL + String.format(
                   "?email=%s&password=%s",
                   URLEncoder.encode( email, StandardCharsets.UTF_8.name()),
                   URLEncoder.encode( password, StandardCharsets.UTF_8.name())
           );
           URL url = new URL(urlString);

           HttpURLConnection connection = (HttpURLConnection) url.openConnection();
           connection.setChunkedStreamingMode( 0 );
           connection.setDoOutput( true );
           connection.setDoInput( true );
           connection.setRequestMethod( "PATCH" );

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

   private void processLogIn(String respond){
       try{

           JSONObject root = new JSONObject(respond);
           UserData.setId(root.getString("id"));
           UserData.setAvatar(root.getString("avatar"));
           redirect();
       } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
       }
   }
   private void redirect(){
       Intent intent = new Intent(this, MainActivity.class);
       startActivity(intent);
   }

}