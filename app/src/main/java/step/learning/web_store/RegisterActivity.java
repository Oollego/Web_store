package step.learning.web_store;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import step.learning.web_store.data.HttpClient;
import step.learning.web_store.data.UserData;

public class RegisterActivity extends AppCompatActivity {

    private static final String AUTH_URL = "https://webstore.is-great.net/auth";
    String filePath;
    Bitmap bitmap;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registr);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.authMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button avatarBtn = findViewById(R.id.avatarBtn);
        Button authButton = findViewById(R.id.authButton);
        authButton.setOnClickListener(this::sendContentClick);

        ConstraintLayout mainConstraint = findViewById(R.id.authMain);
        mainConstraint.setOnClickListener((v) -> hideSoftInput());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_user_btn);
        if(UserData.getBadgeNumber() > 0){
            BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.bottom_basket_btn);
            badge.setVisible(true);
            badge.setNumber(UserData.getBadgeNumber());
        }



        avatarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                getFile.launch(intent);
            }
        });
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
    private void sendContentClick(View view){

        TextInputEditText userNameIt = findViewById(R.id.userNameTextField);
        TextInputEditText userEmailIt = findViewById(R.id.userEmailTextField);
        TextInputEditText passIt = findViewById(R.id.userPasswordTextField);
        TextInputEditText repeatPassIt = findViewById(R.id.userRepeatPasswordTextField);

        if(userNameIt.getText() == null || userEmailIt.getText() == null
                || passIt.getText() == null || repeatPassIt.getText() == null){
            return;
        }

        if(passIt.getText().equals(repeatPassIt.getText())){
            return;
        }
        String name = userNameIt.getText().toString();
        String email = userEmailIt.getText().toString();
        String pass = passIt.getText().toString();

        CompletableFuture.supplyAsync( ()->  sendRegistration(name, email, pass), executorService )
                .thenAcceptAsync(this::resultDisplay);
    }

    ActivityResultLauncher<Intent> getFile =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                Intent intent = result.getData();
                if(intent == null){
                    return;
                }
                Uri uri = intent.getData();
                if(uri == null){
                    return;
                }
                TextInputEditText pathIt = findViewById(R.id.userPathTextField);
                pathIt.setText(uri.getPath());
                filePath = uri.getPath();
                try{
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                }
                catch(IOException e){
                    Log.e("GetBitmap", e.getMessage());
                }
            }
        }
    });

    private String sendRegistration(String name, String email, String pass){

//        Bitmap b = BitmapFactory.decodeResource(RegisterActivity.this.getResources(), R.drawable.logo);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            HttpClient client = new HttpClient(AUTH_URL);
            client.connectForMultipart();
            client.addFormPart("user-name", name);
            client.addFormPart("user-email", email);
            client.addFormPart("user-password", pass);

            if(bitmap != null){
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                client.addFilePart("user-avatar", "logo.png", byteArrayOutputStream.toByteArray());
            }

            client.finishMultipart();
            return client.getResponse();
        }
        catch(Throwable t) {
            Log.e("RegistrationActivity", t.getMessage());
        }
        return null;
    }

    private void resultDisplay(String jsonString){
        runOnUiThread(() -> {
            try {
                JSONObject root = new JSONObject(jsonString);
                int status = root.getInt("status");
                JSONObject data = root.getJSONObject("data");
                String message = data.getString("message");

                if (status == 1) {
                    new AlertDialog.Builder(this)
                            .setMessage("Реєстрація успішна")
                            .setCancelable(false)
                            .setPositiveButton("Ok", (dialog, which) -> {
                                startActivity(new Intent(getApplicationContext(), UserActivity.class));
                                finish();
                            })
                            .show();
                }
                if (status == 0) {
                    new AlertDialog.Builder(this)
                            .setMessage("Реєстрація неуспішна")
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("Ok", (dialog, which) -> finish())
                            .show();
                }

            } catch (Exception ex) {
                throw new IllegalArgumentException(ex.getMessage());
            }
        });
    }
}