package com.the_mad_pillow.twitphone.twitter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.models.User;

import java.util.ArrayList;
import java.util.List;

public class FriendsResponseModel {
    @SerializedName("previous_cursor")
    @Expose
    private Integer previousCursor;

    @SerializedName("previous_cursor_str")
    @Expose
    private String previousCursorStr;

    @SerializedName("next_cursor")
    @Expose
    private Long nextCursor;

    @SerializedName("next_cursor_str")
    @Expose
    private String nextCursorStr;

    @SerializedName("users")
    @Expose
    private List<User> results = new ArrayList<>();

    public Integer getPreviousCursor() {
        return previousCursor;
    }

    public String getPreviousCursorStr() {
        return previousCursorStr;
    }

    public String getNextCursorStr() {
        return nextCursorStr;
    }

    public List<User> getResults() {
        return results;
    }

    public Long getNextCursor() {
        return nextCursor;
    }
}
