package edu.usc.sunset.team7.www.parkhere.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.usc.sunset.team7.www.parkhere.R;
import edu.usc.sunset.team7.www.parkhere.Utils.Consts;
import edu.usc.sunset.team7.www.parkhere.Utils.Tools;
import edu.usc.sunset.team7.www.parkhere.objectmodule.Listing;
import edu.usc.sunset.team7.www.parkhere.objectmodule.ResultsPair;

/**
 * Created by kunal on 10/26/16.
 */
public class ListingDetailsActivity extends AppCompatActivity {

    @BindView(R.id.listing_name) TextView listingNameTextView;
    @BindView(R.id.listing_details) TextView listingDetailsTextView;
    @BindView(R.id.provider_name) TextView providerNameTextView;
    @BindView(R.id.parking_image) ImageView parkingImageView;
    @BindView(R.id.book_listing_button) Button bookListingButton;
    @BindView(R.id.listing_details_toolbar) Toolbar postListingToolbar;
    @BindView(R.id.edit_listing_button) AppCompatButton editListingButton;
    @BindView(R.id.delete_listing_button) AppCompatButton deleteListingButton;

    private ResultsPair listingResultPair;
    private Listing listingResult;
    private String providerFirstName;
    private static final String TAG = "ListingDetailsActivity";
    private boolean myOwnListing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_details);
        ButterKnife.bind(this);

        setSupportActionBar(postListingToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Temporary string, should replace with title of listing later
            getSupportActionBar().setTitle("Listing details");
        }

        myOwnListing = getIntent().getBooleanExtra(Consts.MY_OWN_LISTING_EXTRA, true);

        if (myOwnListing) {
            listingResult = (Listing) getIntent().getSerializableExtra(Consts.LISTING_EXTRA);
            providerNameTextView.setVisibility(View.GONE);
            bookListingButton.setVisibility(View.GONE);
            deleteListingButton.setVisibility(View.GONE); //assume inactive initially


            //check if the listing is active
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Listings").child(listingResult.getProviderID())
                    .child(Consts.ACTIVE_LISTINGS).child(listingResult.getListingID());
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                 @Override
                 public void onDataChange(DataSnapshot dataSnapshot) {
                     if(dataSnapshot.exists()) { //if it is active, allow delete
                        deleteListingButton.setVisibility(View.VISIBLE);
                     }
                 }

                 @Override
                 public void onCancelled(DatabaseError databaseError) {}
             });

        } else {
            listingResultPair = (ResultsPair) getIntent().getSerializableExtra(Consts.LISTING_RESULT_EXTRA);
            listingResult = listingResultPair.getListing();
            editListingButton.setVisibility(View.GONE);
            deleteListingButton.setVisibility(View.GONE);
        }

        ValueEventListener databaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!myOwnListing) {
                    providerFirstName = (String) dataSnapshot.getValue();
                    listingNameTextView.setText(providerFirstName);
                }
                listingDetailsTextView.setText(listingDetailsString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadProviderName:onCancelled", databaseError.toException());
            }
        };
        String providerID;
        if (myOwnListing) {
            providerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            providerID = listingResultPair.getListing().getProviderID();
        }
        DatabaseReference providerNameRef = FirebaseDatabase.getInstance().getReference().child(Consts.USERS_DATABASE)
                .child(providerID).child(Consts.USER_FIRSTNAME);
        providerNameRef.addListenerForSingleValueEvent(databaseListener);
    }

    private String listingDetailsString() {
        Listing listing = listingResult;
        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append("Name of Listing: " + listing.getName());
        descriptionBuilder.append("\nListing Description: "  + listing.getDescription());
        descriptionBuilder.append("\nStart Time: " + Tools.convertUnixTimeToDateString(listing.getStartTime()));
        descriptionBuilder.append("\nEnd Time: " + Tools.convertUnixTimeToDateString(listing.getStopTime()));
        if (!myOwnListing) {
            descriptionBuilder.append("\nDistance Away: " + listingResultPair.getDistance());
        }
        descriptionBuilder.append("\nListing provider: " + providerFirstName);
        descriptionBuilder.append("\n\nParking Information");
        descriptionBuilder.append("\nPrice: " + listing.getPrice());
        descriptionBuilder.append("\nRefundable? " +listing.isRefundable());
        descriptionBuilder.append("\nHandicap? " + listing.isHandicap());
        descriptionBuilder.append("\nCovered? " + listing.isCovered());
        descriptionBuilder.append("\nCompact? " + listing.isCompact());

        return descriptionBuilder.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @OnClick(R.id.book_listing_button)
    protected void bookListing() {
        Intent intent = new Intent(ListingDetailsActivity.this, TransactionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Consts.LISTING_TO_BE_BOOKED, listingResultPair.getListing());
        bundle.putDouble(Consts.LISTING_DISTANCE, listingResultPair.getDistance());
        bundle.putString(Consts.LISTING_DETAILS_STRING, listingDetailsString());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @OnClick(R.id.edit_listing_button)
    protected void editListing() {
        EditListingActivity.startActivity(this, listingResult);
    }

    @OnClick(R.id.delete_listing_button)
    protected void deleteListingRequested() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Listing?")
                .setMessage("Are you sure you want to delete this listing?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        deleteListing();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @OnClick(R.id.provider_name)
    protected void displayProvider() {
        //Go to public user profile activity
        Intent intent = new Intent(ListingDetailsActivity.this, UserProfileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Consts.USER_ID, listingResult.getProviderID());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void deleteListing() {
        FirebaseDatabase.getInstance().getReference().child(listingResult.getProviderID())
                .child(Consts.ACTIVE_LISTINGS).child(listingResult.getListingID()).removeValue();
        Toast.makeText(this,
                "Listing deleted.",
                Toast.LENGTH_SHORT).show();
        finish();
    }

}
