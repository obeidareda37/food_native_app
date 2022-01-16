package com.obeidareda37.foodapp.activities.ui.comments;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.obeidareda37.foodapp.model.CommentModel;

import java.util.List;

public class CommentViewModel extends ViewModel {
    private MutableLiveData<List<CommentModel>> mutableLiveDataCommentModel;

    public CommentViewModel() {
//        mutableLiveDataCommentModel = new MutableLiveData<>();
    }

    public MutableLiveData<List<CommentModel>> getMutableLiveDataCommentModel() {
        if (mutableLiveDataCommentModel == null) {
            mutableLiveDataCommentModel = new MutableLiveData<>();
        }
        return mutableLiveDataCommentModel;
    }

    public void  setMutableLiveDataCommentModel (List<CommentModel> commentModel){
        mutableLiveDataCommentModel.setValue(commentModel);
    }

}
