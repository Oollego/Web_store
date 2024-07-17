package step.learning.web_store.orm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ItemListResponse {
    private int status;
    private ArrayList<ItemListItem> itemList;

    public static ItemListResponse FromJsonString(String jsonString) {
        try {
            JSONObject root = new JSONObject(jsonString);
            int status = root.getInt("status");
            JSONArray arrJson = root.getJSONArray("data");

            ArrayList<ItemListItem> sItemList = new ArrayList<>();

            for (int i = 0; i < arrJson.length(); i++) {
                sItemList.add(ItemListItem.fromJSON(arrJson.getJSONObject(i)));
            }
            ItemListResponse itemListResponse = new ItemListResponse();
            itemListResponse.setStatus(status);
            itemListResponse.setItemList(sItemList);
            return itemListResponse;

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

    public ArrayList<ItemListItem> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<ItemListItem> itemList) {
        this.itemList = itemList;
    }
}
