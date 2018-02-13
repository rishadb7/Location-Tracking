package com.example.user.location;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ListOnline extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener{

    //firebase
    private DatabaseReference onlineRef, currentUserRef,counterRef,locations;
    FirebaseRecyclerAdapter<User,ListOnlineViewHolder>adapter;

    //view
    RecyclerView listonline;
    RecyclerView.LayoutManager layoutManager;

    //Location

    private static final int MY_PERMISSION_REQUEST_CODE=7171;
    private static final int PLAY_SERVICES_RES_REQUEST=7172;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private static int UPDATE_INTERVAL=5000;
    private static int FASTEST_INTERVAL=3000;
    private static int DISTANCE=10;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_online);

        //init View

        listonline=(RecyclerView)findViewById(R.id.listonline);
        listonline.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        listonline.setLayoutManager(layoutManager);

        //toolbar logout //join menu
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Location");
        setSupportActionBar(toolbar);

//Firebase
        onlineRef= FirebaseDatabase.getInstance().getReference().child(".info/connected");
        counterRef=FirebaseDatabase.getInstance().getReference("lastOnline");
        currentUserRef=FirebaseDatabase.getInstance().getReference("lastOnline").child(FirebaseAuth.getInstance().getCurrentUser().getUid());


        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED  ){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION

            },MY_PERMISSION_REQUEST_CODE);
        }
        else{
            if(checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();

            }
        }
        setupSystem();
        //load users from counterRef and display in recyclerView
        //online list
        updatelist();



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
            {
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
            }
        }
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED  ){
return;
        }
        mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation !=null){

            try{
                locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new Tracking(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                        FirebaseAuth.getInstance().getUid(),String.valueOf(mLastLocation.getLatitude()),
                        String.valueOf(mLastLocation.getLongitude())));
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            //update to Firebase


        }
        else
        {
         //   Toast.makeText(this,"couldn't get the location",Toast.LENGTH_SHORT).show();
            Log.d("Test","couldn't load location");
        }
    }

    private void createLocationRequest() {
        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISTANCE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient=new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();{
        }
    }

    private boolean checkPlayServices() {
        int resultCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode!= ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RES_REQUEST).show();
            }
            else{
                Toast.makeText(this, "this device is not supported",Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void updatelist() {
        adapter=new FirebaseRecyclerAdapter<User, ListOnlineViewHolder>(
                User.class,
                R.layout.user_layout,
                ListOnlineViewHolder.class,
                counterRef
        ) {
            @Override
            protected void populateViewHolder(ListOnlineViewHolder viewHolder, final User model, int position) {
                viewHolder.txtEmail.setText(model.getEmail());
                // implement item click for recycler view

                viewHolder.txtEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                           // Toast.makeText(v.getContext(),String.valueOf(mLastLocation.getLatitude()),Toast.LENGTH_LONG).show();

                         try{
                             Intent map=new Intent(ListOnline.this, MapTracking.class);
                             map.putExtra("Email",model.getEmail());
                             map.putExtra("lat",mLastLocation.getLatitude());
                             map.putExtra("lng",mLastLocation.getLongitude());
                             startActivity(map);

                         }
                         catch (Exception e)
                         {
                             e.printStackTrace();
                         }





                      /*  if(!model.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                            Intent map=new Intent(ListOnline.this, MapTracking.class);
                            map.putExtra("Email",model.getEmail());
                            map.putExtra("lat",mLastLocation.getLatitude());
                            map.putExtra("lng",mLastLocation.getLongitude());
                            startActivity(map);
                        }*/

                    }
                });


                viewHolder.itemClickListener=new ItemClickListener() {
                    @Override
                    public void onClick(View view, int postion) {
                     if(!model.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                         Intent map=new Intent(ListOnline.this, MapTracking.class);
                         map.putExtra("Email",model.getEmail());
                         map.putExtra("lat",mLastLocation.getLatitude());
                         map.putExtra("lng",mLastLocation.getLongitude());
                         startActivity(map);
                     }
                    }
                };


            }
        };
        adapter.notifyDataSetChanged();
        listonline.setAdapter(adapter);
    }

    private void setupSystem() {
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(Boolean.class)){
                    currentUserRef.onDisconnect().removeValue();
                    counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(),"Online"));
                    adapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        counterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                     User user=postSnapshot.getValue(User.class);
                    Log.d("LOG", ""+user.getEmail()+"is"+user.getStatus());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_join:
                counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(),"Online"));
break;
            case R.id.action_logout:
                currentUserRef.removeValue();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED  ){
return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        displayLocation();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!=null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkPlayServices();

    }
}
