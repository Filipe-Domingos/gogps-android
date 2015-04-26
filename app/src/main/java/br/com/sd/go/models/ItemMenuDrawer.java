package br.com.sd.go.models;

public class ItemMenuDrawer {

    private String mTitle;
    private Integer mResourceImage;

    public ItemMenuDrawer(String title, Integer resourceImage) {
        mTitle = title;
        mResourceImage = resourceImage;
    }

    public String getTitle() {
        return mTitle;
    }

    public Integer getResourceImage() {
        return mResourceImage;
    }
}
