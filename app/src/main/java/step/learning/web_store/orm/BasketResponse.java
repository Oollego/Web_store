package step.learning.web_store.orm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class BasketResponse {
    private int status;
    private ArrayList<BasketItem> BasketItems;

    public static BasketResponse FromJsonString(String jsonString){
        try{
            JSONObject root = new JSONObject(jsonString);
            int status = root.getInt("status");
            JSONArray arrJson = root.getJSONArray("data");

            ArrayList<BasketItem> basket = new ArrayList<>();

            for( int i = 0; i< arrJson.length(); i++ ){
                basket.add(BasketItem.fromJSON(arrJson.getJSONObject(i)));
            }
            BasketResponse basketResponse = new BasketResponse();
            basketResponse.setStatus(status);
            basketResponse.setBasketItems(basket);
            return basketResponse;

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

    public ArrayList<BasketItem> getBasketItems() {
        return BasketItems;
    }

    public void setBasketItems(ArrayList<BasketItem> basketItems) {
        BasketItems = basketItems;
    }
}
