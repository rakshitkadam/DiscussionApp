package com.stabstudio.discussionapp.UI;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stabstudio.discussionapp.R;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class GoogleSignInActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    public FirebaseAuth mAuth;
    public GoogleApiClient mGoogleApiClient;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @BindView(R.id.status) TextView mStatusTextView;
    @BindView(R.id.detail) TextView mDetailTextView;
    @BindView(R.id.sign_in_button) SignInButton signInButton;
    @BindView(R.id.sign_out_button) Button signOutButton;
    @BindView(R.id.disconnect_button) Button disconnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);
        ButterKnife.bind(this);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revokeAccess();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        Log.d(TAG, "90"  );


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        Log.d(TAG, "97"  );

        mAuth = FirebaseAuth.getInstance();

        pref = getSharedPreferences("MetaData", Context.MODE_PRIVATE);
        editor = pref.edit();
        editor.putBoolean("LoggedIn", false);
        editor.commit();
        Log.d(TAG, "104"  );

        FirebaseUser currentUser = mAuth.getCurrentUser();
////.................................................................
        if(currentUser != null){
            //Not a first time user
            editor.putBoolean("LoggedIn", true);
            editor.commit();
            Log.d(TAG, "110"  );

            Intent in = new Intent(this, HomeScreenActivity.class);
            startActivity(in);
        }
        Log.d(TAG, "115"  );

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "130"  );

            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Log.d(TAG, "135"  );
            }

            else {
                Log.d(TAG, "139"  );
                hideProgressDialog();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("Users");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            String id = firebaseUser.getUid();
                            String place_id = null;
                            String photoUrl = firebaseUser.getPhotoUrl().toString();
                            String first_name = firebaseUser.getDisplayName();

                            String last_name = firebaseUser.getDisplayName();
                            //String notificationToken = SharedPreferenceManager.getInstance().getFcmToken();
                            String notificationToken = "nULl";

                            Map<String, Object> updateValues = new HashMap<>();
                            updateValues.put("id", id);
                            updateValues.put("place_id", place_id);
                            updateValues.put("photoUrl", photoUrl);
                            updateValues.put("first_name", first_name);
                            updateValues.put( "last_name", last_name);
                            updateValues.put("notificationToken", notificationToken);
                            updateValues.put("placeSet",false);
                            dRef.child(firebaseUser.getUid()).updateChildren(updateValues);
                            Log.w(TAG, "175");

                            Intent in = new Intent(getApplicationContext(), PlaceAutocompleteActivity.class);
                            startActivity(in);
                            Log.w(TAG,"DDD");
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(GoogleSignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            hideProgressDialog();
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void signOut() {
        mAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        hideProgressDialog();
                    }
                }
        );
    }

    private void revokeAccess() {
        mAuth.signOut();
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        hideProgressDialog();
                    }
                }
        );
    }
    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }


}