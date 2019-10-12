package com.nova_smartlock.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nova_smartlock.R;
import com.nova_smartlock.db.LockDetails;
import com.nova_smartlock.model.Key;
import com.nova_smartlock.utils.DisplayUtil;
import com.nova_smartlock.utils.SharePreferenceUtility;

import java.util.List;

import static com.nova_smartlock.activity.NearbyLockActivity.curKey;
import static com.nova_smartlock.utils.Const.KEY_VALUE;
import static com.nova_smartlock.utils.Const.USER_KEY_VALUE;

public class Nearby_Adapter extends RecyclerView.Adapter<Nearby_Adapter.Nearby_holder> {
    Activity activity;
    List<Key> nearby_models;
    List<LockDetails> lockDetails;
    boolean from_user;

    public Nearby_Adapter(Activity activity, List<Key> nearby_models) {
        this.activity = activity;
        this.nearby_models = nearby_models;
    }

    public Nearby_Adapter(Activity activity, List<LockDetails> lockDetails, boolean from_user) {
        this.activity = activity;
        this.lockDetails = lockDetails;
        this.from_user = from_user;
    }

    @NonNull
    @Override
    public Nearby_holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rv_nearby_locks, viewGroup, false);
        Nearby_holder nearbt_holder = new Nearby_holder(view);
        return nearbt_holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Nearby_holder nearby_holder, int i) {
        if (from_user) {
            nearby_holder.name.setText(lockDetails.get(i).getLockAlis_value());
        } else {
            nearby_holder.name.setText(nearby_models.get(i).getLockAlias());
        }
    }

    @Override
    public int getItemCount() {
        if (from_user) {
            return lockDetails.size();
        } else {
            return nearby_models.size();
        }
    }

    public class Nearby_holder extends RecyclerView.ViewHolder {
        TextView name;

        public Nearby_holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NewApi")
                @Override
                public void onClick(View v) {
                    if (from_user) {
                        SharePreferenceUtility.saveUserLock(activity.getBaseContext(), USER_KEY_VALUE, lockDetails.get(getAdapterPosition()));

                        Intent intent = new Intent(activity, MainActivity.class);
                        intent.putExtra("isAdmin", true);
                        intent.putExtra("from_near_by_activity", true);
                        activity.startActivity(intent);
                        activity.overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
                        activity.finish();

                    } else {
                        curKey = nearby_models.get(getAdapterPosition());
                        SharePreferenceUtility.saveObjectPreferences(activity.getBaseContext(), KEY_VALUE, nearby_models.get(getAdapterPosition()));
                        if (curKey.isAdmin()) {
                            Intent intent = new Intent(activity, MainActivity.class);
                            intent.putExtra("isAdmin", true);
                            intent.putExtra("from_near_by_activity", true);
                            activity.startActivity(intent);
                            activity.overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
                            activity.finish();
                        } else {
                            Intent intent = new Intent(activity, MainActivity.class);
                            intent.putExtra("isAdmin", false);
                            intent.putExtra("from_near_by_activity", true);
                            activity.startActivity(intent);
                            activity.overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
                            activity.finish();
                            DisplayUtil.showMessageDialog(activity, activity.getString(R.string.words_not_admin), activity.getDrawable(R.drawable.ic_iconfinder_143_attention_183267));
                            //Toast.makeText(activity, activity.getString(R.string.words_not_admin), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        }
    }
}