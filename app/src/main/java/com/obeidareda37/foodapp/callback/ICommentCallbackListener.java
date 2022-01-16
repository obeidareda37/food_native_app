package com.obeidareda37.foodapp.callback;

import com.obeidareda37.foodapp.model.CommentModel;

import java.util.List;

public interface ICommentCallbackListener {
    void onCommentLoadSuccess(List<CommentModel> commentModels);
    void onCommentLoadFailed(String message);
}
