package com.obeidareda37.foodapp.activities.ui.comments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.obeidareda37.foodapp.R;
import com.obeidareda37.foodapp.adapter.CommentAdapter;
import com.obeidareda37.foodapp.callback.ICommentCallbackListener;
import com.obeidareda37.foodapp.common.Common;
import com.obeidareda37.foodapp.model.CommentModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import dmax.dialog.SpotsDialog;

public class CommentFragment extends BottomSheetDialogFragment implements ICommentCallbackListener {

    private CommentViewModel commentViewModel;
    private AlertDialog dialog;
    private ICommentCallbackListener listener;
    private RecyclerView recyclerComment;

    public CommentFragment() {
        listener = this;
    }

    private static CommentFragment instance;

    public static CommentFragment getInstance() {
        if (instance == null) {
            instance = new CommentFragment();
        }
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View itemView = LayoutInflater.from(getContext())
                .inflate(R.layout.bottom_sheet_comment_fragment, container, false);
        recyclerComment = itemView.findViewById(R.id.recycler_comment);
        initViews();
        loadCommentFragmentFirebase();
        commentViewModel.getMutableLiveDataCommentModel().observe(getViewLifecycleOwner(), commentModels -> {
            CommentAdapter adapter = new CommentAdapter(getContext(), commentModels);
            recyclerComment.setAdapter(adapter);
        });

        return itemView;
    }

    private void loadCommentFragmentFirebase() {
        dialog.show();
        List<CommentModel> commentModels = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
                .child(Common.selectFood.getId())
                .orderByChild("serverTimeStamp")
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            CommentModel commentModel = dataSnapshot.getValue(CommentModel.class);
                            commentModels.add(commentModel);
                        }
                        listener.onCommentLoadSuccess(commentModels);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onCommentLoadFailed(error.getMessage());


                    }
                });
    }

    private void initViews() {
        commentViewModel = new ViewModelProvider(this).get(CommentViewModel.class);
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

        recyclerComment.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                RecyclerView.VERTICAL, true);
        recyclerComment.setLayoutManager(layoutManager);
        recyclerComment.addItemDecoration(new DividerItemDecoration(getContext()
                , layoutManager.getOrientation()));
    }

    @Override
    public void onCommentLoadSuccess(List<CommentModel> commentModels) {
        dialog.dismiss();
        commentViewModel.setMutableLiveDataCommentModel(commentModels);
        Log.d("Commmmment", commentModels.get(1).getName());
    }

    @Override
    public void onCommentLoadFailed(String message) {
        dialog.dismiss();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        Log.d("errorFirebase", message);

    }
}
