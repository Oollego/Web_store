package step.learning.web_store.orm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SubGroupResponse {
    private int status;
    private ArrayList<SubGroupItem> subGroups;

    public static SubGroupResponse FromJsonString(String jsonString) {
        try {
            JSONObject root = new JSONObject(jsonString);
            int status = root.getInt("status");
            JSONArray arrJson = root.getJSONArray("data");

            ArrayList<SubGroupItem> sGroup = new ArrayList<>();

            for (int i = 0; i < arrJson.length(); i++) {
                sGroup.add(SubGroupItem.fromJSON(arrJson.getJSONObject(i)));
            }
            SubGroupResponse groupResponse = new SubGroupResponse();
            groupResponse.setStatus(status);
            groupResponse.setSubGroups(sGroup);
            return groupResponse;

        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }

    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ArrayList<SubGroupItem> getSubGroups() {
        return subGroups;
    }

    public void setSubGroups(ArrayList<SubGroupItem> subGroups) {
        this.subGroups = subGroups;
    }


}
