package step.learning.web_store.orm;

import org.json.JSONObject;

public class MainGroupItem {
    private String id;
    private String groupName;
    private String uriName;
    private String logo;
    private String img_mame;

    public MainGroupItem(){}
    public MainGroupItem(String id, String groupName, String uriName, String logo, String img_mame) {
        this.id = id;
        this.groupName = groupName;
        this.uriName = uriName;
        this.logo = logo;
        this.img_mame = img_mame;
    }

    public static MainGroupItem fromJSON(JSONObject jsonObject){
        try {
            String id = jsonObject.getString("id");
            String groupName = jsonObject.getString("main_group_name");
            String uri_name = jsonObject.getString("uri_name");
            String logo = jsonObject.getString("logo");
            String imgName = jsonObject.getString("img_name");

            MainGroupItem group = new MainGroupItem();
            group.setId(id);
            group.setGroupName(groupName);
            group.setUriName(uri_name);
            group.setLogo(logo);
            group.setImg_mame(imgName);
            return group;

        }
        catch (Exception ex){
            throw new IllegalArgumentException( ex.getMessage() );
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getUriName() {
        return uriName;
    }

    public void setUriName(String uriName) {
        this.uriName = uriName;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getImg_mame() {
        return img_mame;
    }

    public void setImg_mame(String img_mame) {
        this.img_mame = img_mame;
    }
}
