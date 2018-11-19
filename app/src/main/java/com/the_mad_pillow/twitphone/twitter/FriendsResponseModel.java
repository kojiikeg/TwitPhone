package com.the_mad_pillow.twitphone.twitter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.models.User;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
class FriendsResponseModel {
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
}
