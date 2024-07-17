package step.learning.web_store.adaptor;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import step.learning.web_store.R;
import step.learning.web_store.orm.MainGroupItem;

public class MainGroupAdaptor extends ArrayAdapter<MainGroupItem> {

    private LayoutInflater inflater;
    private int layout;
   // private Context context;
    private ArrayList<MainGroupItem> mainGroups;

    public MainGroupAdaptor(@NonNull Context context, int resource, @NonNull ArrayList<MainGroupItem> mainGroups) {
        super(context, resource, mainGroups);
        this.mainGroups = mainGroups;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
    }
//    public MainGroupAdaptor(@NonNull Context context, @NonNull ArrayList<MainGroupItem> mainGroups) {
//        super(context, R.layout.list_main_group, mainGroups);
//        this.mainGroups = mainGroups;
//        this.context = context;
//       // this.inflater = LayoutInflater.from(context);
//    }
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        View view = inflater.inflate(this.layout, parent, false);
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View view = inflater.inflate(R.layout.list_main_group, parent, false);
//        ImageView imageView = view.findViewById(R.id.main_group_imgV);
//        TextView textView = view.findViewById(R.id.main_group_textV);

        MainGroupItem mainGroup = mainGroups.get(position);

        String url = "https://projects.heliohost.us/img/maingroup/" + mainGroup.getImg_mame();

//        try ( java.io.InputStream is = new URL(url).openConnection().getInputStream() ) {
//            imageView.setImageBitmap(BitmapFactory.decodeStream( is ));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        textView.setText(mainGroup.getGroupName());
        return view;
    }
}
