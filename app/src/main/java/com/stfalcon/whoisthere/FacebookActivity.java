package com.stfalcon.whoisthere;

import android.content.Intent;

import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookSdk;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.FacebookException;
import com.facebook.login.widget.ProfilePictureView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;


public class FacebookActivity extends ActionBarActivity {

    LoginButton loginButton;
    TextView TextView_Name, TextView_Id, TextView_Link;
    ProfilePictureView profilePictureView;

    CallbackManager callbackManager;

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        callbackManager.onActivityResult(requestCode, responseCode, data);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        InitializationFacebook(savedInstanceState);

        InitializationView();

        CheckUserLogin();

        LoginButtonCLick();

       /* List<String> permissionNeeds = Arrays.asList("user_photos", "email", "user_birthday", "user_location", "public_profile");

        loginButton.setReadPermissions(permissionNeeds);*/

    }


    public void InitializationFacebook(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_facebook);
    }

    public void InitializationView() {
        loginButton = (LoginButton) this.findViewById(R.id.login_button);
        TextView_Name = (TextView) this.findViewById(R.id.textView);
        TextView_Link = (TextView) this.findViewById(R.id.textViewEmail);
        TextView_Id = (TextView) this.findViewById(R.id.textViewId);
    }

    public void CheckUserLogin() {
        if (AccessToken.getCurrentAccessToken() != null) {
            User user = new User(Profile.getCurrentProfile().getName(), Profile.getCurrentProfile().getId(), Profile.getCurrentProfile().getLinkUri().toString());
            TextView_Name.setText(user.name);
            TextView_Id.setText(user.id);
            TextView_Link.setText(user.link);
            ProfileFoto(user.Get_Pass_To_Profile_Foto());
            Intent intent = new Intent(FacebookActivity.this, MapActivity.class);
            intent.putExtra("name", user.name).toString();
            intent.putExtra("id", user.id);
            intent.putExtra("pass", "http://graph.facebook.com/" + user.id + "/picture?type=large");
            startActivity(intent);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "You must Login", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void LoginButtonCLick() {
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {


                final GraphRequest request = GraphRequest.newMeRequest
                        (loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    User user = new Gson().fromJson(object.toString(), User.class);
                                    TextView_Name.setText(user.name);
                                    TextView_Id.setText(user.id);
                                    ProfileFoto(user.Get_Pass_To_Profile_Foto());
                                    TextView_Link.setText(user.link);
                                    //String url = "http://graph.facebook.com/839669416127599/picture";

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday,link");
                request.setParameters(parameters);
                request.executeAsync();
                Intent intent = new Intent(FacebookActivity.this, MapActivity.class);
                intent.putExtra("name", TextView_Name.getText().toString());
                intent.putExtra("id", TextView_Id.getText().toString());
                intent.putExtra("pass", "http://graph.facebook.com/" + TextView_Id.getText().toString() + "/picture?type=large");
                startActivity(intent);
            }

            @Override
            public void onCancel() {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "onCancel", Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "onError", Toast.LENGTH_SHORT);
                toast.show();
                Log.v("LoginActivity", exception.getCause().toString());
            }
        });
    }

    public void ProfileFoto(String pass) {
        ImageView im = (ImageView) findViewById(R.id.imageView);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(FacebookActivity.this)
                .memoryCacheExtraOptions(100, 100) // width, height
                .discCacheExtraOptions(100, 100, Bitmap.CompressFormat.PNG, 100)
                .build();

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        imageLoader.displayImage(pass, im);
    }

}