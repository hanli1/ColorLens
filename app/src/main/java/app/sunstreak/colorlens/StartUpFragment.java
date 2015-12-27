package app.sunstreak.colorlens;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StartUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartUpFragment extends Fragment {

    @Bind(R.id.fragment_start_up_activate_camera)
    ImageButton launchCamera;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StartUpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StartUpFragment newInstance() {
        StartUpFragment fragment = new StartUpFragment();
        return fragment;
    }

    public StartUpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_start_up, container, false);
        ButterKnife.bind(this, view);
        launchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).dispatchTakePictureIntent();
            }
        });
        return view;
    }


}
