package step.learning.web_store.orm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ItemResponse {
    private int status;
    private Item item;
    private ArrayList<ItemFeature> features;
    private ArrayList<ItemFeedback> feedbacks;
    private ArrayList<String> images;
    private double avgRating;
    private int quantityReview;

    public ItemResponse() {
    }

    public static ItemResponse FromJsonString(String jsonString) {
        try {
            JSONObject root = new JSONObject(jsonString);
            int status = root.getInt("status");
            JSONObject itemJson = root.getJSONObject("data");
            JSONArray arrJsonFeature = itemJson.getJSONArray("features");
            JSONArray arrJsonFeedbacks = itemJson.getJSONArray("feedbacks");
            JSONArray arrJsonImages = itemJson.getJSONArray("imgs");


            ArrayList<ItemFeature> features = new ArrayList<ItemFeature>();
            ArrayList<ItemFeedback> feedbacks = new ArrayList<ItemFeedback>();
            ArrayList<String> images = new ArrayList<String>();

            Item item = Item.fromJSON(itemJson.getJSONObject("data"));
            for (int i = 0; i < arrJsonFeature.length(); i++) {
                features.add(ItemFeature.fromJSON(arrJsonFeature.getJSONObject(i)));
            }
            double amountRating = 0;
            for (int i = 0; i < arrJsonFeedbacks.length(); i++) {
                feedbacks.add(ItemFeedback.fromJSON(arrJsonFeedbacks.getJSONObject(i)));
                amountRating += feedbacks.get(i).getScore();
            }

            for (int i = 0; i < arrJsonImages.length(); i++) {
                images.add(arrJsonImages.getJSONObject(i).getString("file_name"));
            }
            ItemResponse itemResponse = new ItemResponse();
            itemResponse.setStatus(status);
            itemResponse.setItem(item);
            itemResponse.setFeatures(features);
            itemResponse.setFeedbacks(feedbacks);
            itemResponse.setImages(images);
            itemResponse.setAvgRating( amountRating / arrJsonFeedbacks.length() );
            itemResponse.setQuantityReview(arrJsonFeedbacks.length());

            return itemResponse;

        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public int getQuantityReview() {
        return quantityReview;
    }

    public void setQuantityReview(int quantityReview) {
        this.quantityReview = quantityReview;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public ArrayList<ItemFeature> getFeatures() {
        return features;
    }

    public void setFeatures(ArrayList<ItemFeature> features) {
        this.features = features;
    }

    public ArrayList<ItemFeedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(ArrayList<ItemFeedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }
}
