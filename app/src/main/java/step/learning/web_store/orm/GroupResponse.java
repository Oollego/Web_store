package step.learning.web_store.orm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupResponse {
private int status;
private ArrayList<MainGroupItem> mainGroups;

public static GroupResponse FromJsonString(String jsonString){
    try{
        JSONObject root = new JSONObject(jsonString);
        int status = root.getInt("status");
        JSONArray arrJson = root.getJSONArray("data");

        ArrayList<MainGroupItem> mGroup = new ArrayList<>();

        for( int i = 0; i< arrJson.length(); i++ ){
            mGroup.add(MainGroupItem.fromJSON(arrJson.getJSONObject(i)));
        }
        GroupResponse groupResponse = new GroupResponse();
        groupResponse.setStatus(status);
        groupResponse.setMainGroups(mGroup);
        return groupResponse;

    }catch(Exception ex){
        throw new IllegalArgumentException(ex.getMessage());
    }

}
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ArrayList<MainGroupItem> getMainGroups() {
        return mainGroups;
    }

    public void setMainGroups(ArrayList<MainGroupItem> mainGroups) {
        this.mainGroups = mainGroups;
    }
}
