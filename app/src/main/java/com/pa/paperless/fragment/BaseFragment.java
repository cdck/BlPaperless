package com.pa.paperless.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pa.paperless.utils.LogUtil;
import com.wind.myapplication.NativeUtil;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * Created by Administrator on 2017/5/18 0018.
 */

public abstract class BaseFragment extends Fragment {
    protected NativeUtil jni = NativeUtil.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onAttach :   --> ");
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onCreate :   --> ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onActivityCreated :   --> ");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onStart :   --> ");
        super.onStart();
    }

    @Override
    public void onResume() {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onResume :   --> ");
        super.onResume();
    }

    @Override
    public void onPause() {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onPause :   --> ");
        super.onPause();
    }

    @Override
    public void onStop() {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onStop :   --> ");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onDestroyView :   --> ");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onDestroy :   --> ");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onDetach :   --> ");
        super.onDetach();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onHiddenChanged :   --> " + hidden);
        super.onHiddenChanged(hidden);
    }
}