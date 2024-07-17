package step.learning.web_store.orm;

import org.json.JSONObject;

public class ItemFeedback {
    private String id;
    private double score;
    private String comment;
    private String date;
    private String itemId;
    public static ItemFeedback fromJSON(JSONObject jsonObject) {
        try {
            String id = jsonObject.getString("id");
            double score = jsonObject.getDouble("score");
            String comment = jsonObject.getString("comment");
            String date = jsonObject.getString("date");
            String itemId = jsonObject.getString("item_id");

            ItemFeedback feedback = new ItemFeedback();
            feedback.setId(id);
            feedback.setScore(score);
            feedback.setComment(comment);
            feedback.setDate(date);
            feedback.setItemId(itemId);

            return feedback;

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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}
