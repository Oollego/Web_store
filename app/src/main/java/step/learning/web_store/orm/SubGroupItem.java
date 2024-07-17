package step.learning.web_store.orm;

import org.json.JSONObject;

public class SubGroupItem {
    private String id;
    private String subGroupName;
    private String uriName;
    private String img_mame;

    public static SubGroupItem fromJSON(JSONObject jsonObject) {
        try {
            String id = jsonObject.getString("id");
            String subGroupName = jsonObject.getString("sub_name");
            String uri_name = jsonObject.getString("uri_name");
            String imgName = jsonObject.getString("img");

            SubGroupItem subGroup = new SubGroupItem();
            subGroup.setId(id);
            subGroup.setSubGroupName(subGroupName);
            subGroup.setUriName(uri_name);
            subGroup.setImg_mame(imgName);
            return subGroup;

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

    public String getSubGroupName() {
        return subGroupName;
    }

    public void setSubGroupName(String subGroupName) {
        this.subGroupName = subGroupName;
    }

    public String getUriName() {
        return uriName;
    }

    public void setUriName(String uriName) {
        this.uriName = uriName;
    }

    public String getImg_mame() {
        return img_mame;
    }

    public void setImg_mame(String img_mame) {
        this.img_mame = img_mame;
    }


}
