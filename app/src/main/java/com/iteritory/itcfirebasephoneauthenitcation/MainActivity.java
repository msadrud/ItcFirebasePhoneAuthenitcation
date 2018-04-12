package com.iteritory.itcfirebasephoneauthenitcation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    Toolbar mToolbar;
    Drawer mDrawerResult;
    AccountHeader mHeaderResult;
    ProfileDrawerItem mProfileDrawerItem;
    PrimaryDrawerItem mItemLogin, mItemLogout;
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();
        intstantiateUser();

        instantiateMenuItems();
        setupNavigationDrawerWithHeader();
    }

    private void setupToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.idToolbarMain);
        setSupportActionBar(mToolbar);
    }

    private void instantiateMenuItems(){
        mItemLogin = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.login_menu_item).withIcon(getResources().getDrawable(R.mipmap.ic_login_black_48dp));
        mItemLogout = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.logout_menu_item).withIcon(getResources().getDrawable(R.mipmap.ic_logout_black_48dp));;
    }

    private AccountHeader setupAccountHeader(){
        mProfileDrawerItem = new ProfileDrawerItem().withIcon(getResources().getDrawable(R.mipmap.ic_account_circle_black_48dp));
        mHeaderResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(mProfileDrawerItem)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                }).withSelectionListEnabledForSingleProfile(false)
                .build();
        return mHeaderResult;
    }

    private void setupNavigationDrawerWithHeader(){
        //Depending on user is logged in or not, decide whether to show Log In menu or Log Out menu
        if (!isUserSignedIn()){
            ((TextView)findViewById(R.id.idContent)).setText(R.string.default_nouser_signin);
            mDrawerResult = new DrawerBuilder()
                    .withActivity(this)
                    .withAccountHeader(setupAccountHeader())
                    .withToolbar(mToolbar)
                    .addDrawerItems(mItemLogin)
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            onNavDrawerItemSelected((int)drawerItem.getIdentifier());
                            return true;
                        }
                    })
                    .build();
            mDrawerResult.deselect(mItemLogin.getIdentifier());
        }else{
            ((TextView)findViewById(R.id.idContent)).setText(R.string.welcome_on_signin);
            mDrawerResult = new DrawerBuilder()
                    .withActivity(this)
                    .withAccountHeader(setupAccountHeader())
                    .withToolbar(mToolbar)
                    .addDrawerItems(mItemLogout)
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            onNavDrawerItemSelected((int)drawerItem.getIdentifier());
                            return true;
                        }
                    })
                    .build();
        }
        mDrawerResult.closeDrawer();
    }

    private void onNavDrawerItemSelected(int drawerItemIdentifier){
        switch (drawerItemIdentifier){
            //Sign In
            case 1:
                Toast.makeText(this, "Login menu selected", Toast.LENGTH_LONG).show();
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().
                                setDefaultCountryIso("in").build()))
                        .setLogo(R.mipmap.ic_account_circle_black_48dp)
                        .setIsSmartLockEnabled(true)
                        .build(), RC_SIGN_IN);
                break;
            //Sign Out
            case 2:
                signOutUser();
                Toast.makeText(this, "Logout menu selected", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void intstantiateUser(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
    }

    private boolean isUserSignedIn(){
        if (mFirebaseUser == null){
            return false;
        }else{
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.login_success, Toast.LENGTH_LONG).show();
                signInUser();
                return;
            }else{
                //User pressed back button
                if (response == null) {
                    Toast.makeText(this, R.string.login_failed, Toast.LENGTH_LONG).show();
                    mDrawerResult.deselect(mItemLogin.getIdentifier());
                    return;
                }
                //No internet connection.
                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, R.string.no_connectivity, Toast.LENGTH_LONG).show();
                    return;
                }
                //Unknown error
                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, R.string.login_unknown_Error, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }

    private void signInUser(){
        intstantiateUser();
        //mCurrentProfile = checkCurrentProfileStatus();
        mDrawerResult.updateItemAtPosition(mItemLogout,1);
        mDrawerResult.deselect(mItemLogout.getIdentifier());
        ((TextView)findViewById(R.id.idContent)).setText(R.string.welcome_on_signin);
        mDrawerResult.closeDrawer();
    }

    private void signOutUser(){
        //Sign out
        mFirebaseAuth.signOut();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (!isUserSignedIn()) {
            mDrawerResult.updateItemAtPosition(mItemLogin,1);
            mDrawerResult.deselect(mItemLogin.getIdentifier());
            ((TextView)findViewById(R.id.idContent)).setText(R.string.default_nouser_signin);
            mDrawerResult.closeDrawer();
        }else{
            //check if internet connectivity is there or any other error handling
        }
    }
}
