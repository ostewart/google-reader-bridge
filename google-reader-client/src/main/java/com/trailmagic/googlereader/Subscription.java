package com.trailmagic.googlereader;

import java.util.List;

/**
 * Created by: oliver on Date: Dec 11, 2009 Time: 12:50:46 AM
 */
public class Subscription {
    private String id;
    private String title;
    private String sortid;
    private List<Category> categories;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public String getSortid() {
        return sortid;
    }

    public void setSortid(String sortid) {
        this.sortid = sortid;
    }
}
