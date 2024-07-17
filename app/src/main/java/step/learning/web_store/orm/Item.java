package step.learning.web_store.orm;

import org.json.JSONObject;

public class Item {
    private String id;
    private String itemName;
    private String description;
    private String itemCode;
    private double price;
    private double salePrice;
    private String groupId;
    private boolean isActive;

    public static Item fromJSON(JSONObject jsonObject) {
        try {
            String id = jsonObject.getString("id");
            String itemName = jsonObject.getString("item_name");
            String description = jsonObject.getString("description");
            String itemCode = jsonObject.getString("item_code");
            double price = jsonObject.getDouble("price");
            double salePrice = jsonObject.getDouble("sale_price");
            String groupId = jsonObject.getString("item_group_id");
            boolean onActive = (jsonObject.getInt("on_active") != 0 );
//            String img = jsonObject.getString("image_name");
//            double feedback_avg = jsonObject.getDouble("feedback_avg");


            Item item = new Item();
            item.setId(id);
            item.setItemName(itemName);
            item.setDescription(description);
            item.setItemCode(itemCode);
            item.setPrice(price);
            item.setSalePrice(salePrice);
            item.setGroupId(groupId);
            item.setIsActive(onActive);
//            itemList.setImg(img);
//            itemList.setFeedback_avg(feedback_avg);

            return item;

        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
