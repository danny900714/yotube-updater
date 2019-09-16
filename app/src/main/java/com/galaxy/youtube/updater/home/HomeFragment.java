package com.galaxy.youtube.updater.home;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.galaxy.youtube.updater.R;
import com.galaxy.youtube.updater.data.app.AppManager;
import com.galaxy.youtube.updater.data.cluster.ClustersManager;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private HomeAppsRecyclerAdapter mAdapter;

    private RecyclerView mRclApps;

    public HomeFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        } */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // init views
        mRclApps = getView().findViewById(R.id.homeRclApps);

        // init recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRclApps.setLayoutManager(layoutManager);
        mRclApps.setNestedScrollingEnabled(false);
        ClustersManager.getInstance(manager -> {
            mAdapter = new HomeAppsRecyclerAdapter(getContext(), manager, adapterListener);
            mRclApps.setAdapter(mAdapter);
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private HomeAppsRecyclerAdapter.OnInteractionListener adapterListener = new HomeAppsRecyclerAdapter.OnInteractionListener() {
        @Override
        public void onAppLayoutClick(View view, AppManager manager) {
            mListener.OnAppInClusterClick(view, manager);
        }

        @Override
        public void onRecyclerViewItemClick(View itemView, ClustersManager item) {
            mListener.OnClusterClick(itemView, item);
        }
    };

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void OnClusterClick(View view, ClustersManager manager);
        void OnAppInClusterClick(View view, AppManager manager);
    }
}
