
package step.learning.web_store;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import step.learning.web_store.data.UserData;


public class SearchBarFragment extends Fragment {

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UserData.getAvatar() != null) {

        }
    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//                super.onViewCreated(view, savedInstanceState);
//        if(UserData.getBadgeNumber() > 0){
//            BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
//            BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.bottom_basket_btn);
//            badge.setVisible(true);
//            badge.setNumber(UserData.getBadgeNumber());
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       return inflater.inflate(R.layout.fragment_search_bar, container, false);
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
                    imageView.post( ()-> imageView.setImageBitmap(b) )
                );
    }

    @Override
    public void onDestroy() {
        executorService.shutdownNow();
        super.onDestroy();
    }
}