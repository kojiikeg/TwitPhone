package com.the_mad_pillow.twitphone.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.the_mad_pillow.twitphone.R;
import com.the_mad_pillow.twitphone.twitter.MyUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends ArrayAdapter<MyUser> {
    private LayoutInflater inflater;

    public UserListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<MyUser> objects) {
        super(context, resource, objects);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = inflater.inflate(R.layout.user_list_item, parent, false);
        } else {
            view = inflater.inflate(R.layout.user_list_item, parent, false);
        }

        final MyUser myUser = getItem(position);
        if (myUser == null) {
            return view;
        }

        final CircleImageView imageView = view.findViewById(R.id.listItemUserImage);
        TextView textView = view.findViewById(R.id.listItemUserName);

        Glide.with(view).load(myUser.getUser().get400x400ProfileImageURLHttps()).into(imageView);
        textView.setText(myUser.getUser().getName());

        return view;
    }
}
