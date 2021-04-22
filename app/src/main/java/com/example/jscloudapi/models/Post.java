package com.example.jscloudapi.models;

public class Post {
    String _id,authorName,authorID;
    long publishedOn,modifiedOn;
    long likesCount=1010932;
    String postContent="This is a default post content";
    String[] tags={"#nothingyet","#nodata"};
    String imgURL="http://defaultImageUrl.com/defImage.jpeg";

    public String getpID() {
        return _id;
    }

    public void setpID(String pID) {
        this._id = pID;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    public long getPublishedOn() {
        return publishedOn;
    }

    public void setPublishedOn(long publishedOn) {
        this.publishedOn = publishedOn;
    }

    public long getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(long modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

}
