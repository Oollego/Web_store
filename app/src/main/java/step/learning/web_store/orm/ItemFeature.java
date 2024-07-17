package step.learning.web_store.orm;

import org.json.JSONObject;

public class ItemFeature {
    private String featureName;
    private String featureText;
    public static ItemFeature fromJSON(JSONObject jsonObject) {
        try {

            String featureName = jsonObject.getString("feature_name");
            String featureText = jsonObject.getString("feature_text");

            ItemFeature feature = new ItemFeature();
            feature.setFeatureName(featureName);
            feature.setFeatureText(featureText);

            return feature;

        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureText() {
        return featureText;
    }

    public void setFeatureText(String featureText) {
        this.featureText = featureText;
    }
}
