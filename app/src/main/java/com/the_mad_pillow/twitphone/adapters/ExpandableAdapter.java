package com.the_mad_pillow.twitphone.adapters;

import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.the_mad_pillow.twitphone.R;
import com.the_mad_pillow.twitphone.activities.MainActivity;
import com.the_mad_pillow.twitphone.twitter.MyUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ExpandableAdapter extends BaseExpandableListAdapter {
    private MainActivity activity;
    private List<String> groups;
    private SparseArray<List<MyUser>> children;

    public ExpandableAdapter(MainActivity activity, List<String> groups, SparseArray<List<MyUser>> children) {
        this.activity = activity;
        this.groups = groups;
        this.children = children;
    }

    @Override
    public int getGroupCount() {
        return children.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return children.get(i).size();
    }

    @Override
    public String getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public MyUser getChild(int groupPosition, int childPosition) {
        return children.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return -1;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return -1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private TextView getGroupGenericView() {
        AbsListView.LayoutParams param = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 128);

        TextView textView = new TextView(activity);
        textView.setLayoutParams(param);

        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        textView.setPadding(72, 0, 0, 0);
        textView.setTextSize(20);

        return textView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup viewGroup) {
        TextView textView = getGroupGenericView();
        textView.setText(getGroup(groupPosition));

        return textView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isExpanded, View convertView, ViewGroup viewGroup) {
        View view;
        if (convertView == null) {
            view = activity.getLayoutInflater().inflate(R.layout.user_list_item, viewGroup, false);
        } else {
            view = convertView;
        }

        final MyUser myUser = getChild(groupPosition, childPosition);
        if (myUser == null) {
            return view;
        }

        final CircleImageView imageView = view.findViewById(R.id.listItemUserImage);
        final TextView nameText = view.findViewById(R.id.listItemUserName);
        final TextView screenNameText = view.findViewById(R.id.listItemUserScreenName);
        final ImageView statusView = view.findViewById(R.id.listItemUserStatus);

        Glide.with(view).load(myUser.getUser().profileImageUrlHttps.replace("normal", "bigger")).into(imageView);
        nameText.setText(myUser.getUser().name);
        screenNameText.setText(activity.getString(R.string.screenName, myUser.getUser().screenName));
        if (myUser.isOnline()) {
            statusView.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_insert_emoticon_black_24dp));
        } else {
            statusView.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_do_not_disturb_alt_black_24dp));
        }

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}