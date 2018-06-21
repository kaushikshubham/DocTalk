package com.kaushik.doctalk.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaushik.doctalk.R;
import com.kaushik.doctalk.network.dataModel.Data;
import com.kaushik.doctalk.network.dataModel.User;
import com.kaushik.doctalk.utility.Utils;
import com.squareup.picasso.Picasso;

import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private Context mContext;
    private List<User> userList;

    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener listener;

    public UsersAdapter(Context context, RecyclerView recyclerView, OnLoadMoreListener listener) {
        this.mContext = context;
        this.userList = new ArrayList<>();
        this.listener = listener;

        initOnScrollLister(recyclerView);
    }

    private void initOnScrollLister(RecyclerView recyclerView) {
        recyclerView
                .addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView,
                                           int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                                .getLayoutManager();
                        Log.v(Utils.TAG, "LinearLayout : " + linearLayoutManager);
                        if (linearLayoutManager == null)
                            return;
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager
                                .findLastVisibleItemPosition();
                        Log.v(Utils.TAG, "OnScroll : " + totalItemCount + " : " + lastVisibleItem + " : " + loading);
                        if (!loading
                                && totalItemCount <= (lastVisibleItem + visibleThreshold)) {

                            if (listener != null) {
                                listener.onLoadMore();
                            }
                            loading = true;
                        }
                    }
                });
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_row_item, parent, false);
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        final User user = userList.get(position);
        holder.name.setText(user.getLogin().toUpperCase());
        Picasso.with(mContext).load(user.getAvatarUrl())
                .error(R.mipmap.ic_launcher)
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setData(Data data) {
        Log.v(Utils.TAG,"setData : "+data.getPageNumber() + " : " + data.getList().size());
        if (data.getPageNumber() == 1) {
            this.userList = data.getList();
            notifyDataSetChanged();
        } else {
            int count = userList.size() - 1;
            userList.addAll(data.getList());
            notifyItemRangeChanged(count, data.getList().size());
        }
        loading = false;
    }

    public void onDestroy() {
        mContext = null;
        userList = null;
        listener = null;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView thumbnail;

        public UserViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            thumbnail = view.findViewById(R.id.thumbnail);
        }
    }
}
