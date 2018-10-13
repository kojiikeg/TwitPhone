package com.the_mad_pillow.twitphone.twitter;

import com.twitter.sdk.android.core.models.Tweet;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ServiceListeners {
    @GET("1.1/followers/list.json")
    Call<FriendsResponseModel> getFollowerList(@Query("user_id") long id,
                                               @Query("cursor") long cursor,
                                               @Query("count") long count);

    @GET("1.1/friends/list.json")
    Call<FriendsResponseModel> getFriendList(@Query("user_id") long id,
                                             @Query("cursor") long cursor,
                                             @Query("count") long count);

    @FormUrlEncoded
    @POST("/1.1/direct_messages/new.json?" +
            "tweet_mode=extended&include_cards=true&cards_platform=TwitterKit-13")
    Call<Tweet> sendPrivateMessage(@Field("user_id") Long userId,
                                   @Field("screen_name") String screenName,
                                   @Field("text") String text);
}

