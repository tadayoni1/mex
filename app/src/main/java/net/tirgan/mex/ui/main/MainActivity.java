package net.tirgan.mex.ui.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import net.tirgan.mex.R;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity
        extends AppCompatActivity
         {


    private static final int RC_SIGN_IN = 1;
    private FragmentManager mFragmentManager;
    private ListFragment mListFragment;
    private SupportMapFragment mMapFragment;

    @BindView(R.id.navigation)
    BottomNavigationView mBottomNavigationView;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    loadListFragment();
                    return true;
                case R.id.navigation_maps:
                    loadMapsFragment();
                    return true;
            }
            return false;
        }
    };


    private void loadListFragment() {
        mFragmentManager.beginTransaction()
                .replace(R.id.list_container, mListFragment)
                .commit();
    }

    private void loadMapsFragment() {
        mFragmentManager.beginTransaction()
                .replace(R.id.list_container, mMapFragment)
                .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mFragmentManager = getSupportFragmentManager();

        mListFragment = new ListFragment();
        mMapFragment = SupportMapFragment.newInstance();

        mFragmentManager.beginTransaction()
                .add(R.id.list_container, mListFragment)
                .commit();


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth aFirebaseAuth) {
                FirebaseUser currentUser = aFirebaseAuth.getCurrentUser();
                if (currentUser != null) {

                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()
                                    ))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
}
