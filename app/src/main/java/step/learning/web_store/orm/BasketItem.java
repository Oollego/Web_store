package step.learning.web_store.orm;

import org.json.JSONObject;

public class BasketItem extends Item{
    private int quantity;
    private String image_name;

    public static BasketItem fromJSON(JSONObject jsonObject) {
        try {
            Item item = Item.fromJSON(jsonObject);

            int quantity = jsonObject.getInt("quantity");
            String image_name  = jsonObject.getString("image_name");

            BasketItem basketItem = new BasketItem();
            basketItem.setId(item.getId());
            basketItem.setItemName(item.getItemName());
            basketItem.setDescription(item.getDescription());
            basketItem.setItemCode(item.getItemCode());
            basketItem.setPrice(item.getPrice());
            basketItem.setSalePrice(item.getSalePrice());
            basketItem.setGroupId(item.getGroupId());
            basketItem.setIsActive(item.getIsActive());
            basketItem.setQuantity(quantity);
            basketItem.setImage_name(image_name);

            return basketItem;

        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }
}
