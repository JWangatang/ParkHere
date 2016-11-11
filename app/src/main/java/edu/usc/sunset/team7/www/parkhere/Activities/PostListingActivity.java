package edu.usc.sunset.team7.www.parkhere.Activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Hashtable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.usc.sunset.team7.www.parkhere.R;
import edu.usc.sunset.team7.www.parkhere.Utils.Consts;
import edu.usc.sunset.team7.www.parkhere.Utils.Tools;
import edu.usc.sunset.team7.www.parkhere.objectmodule.Listing;

/**
 * Created by kunal on 10/23/16.
 */

public class PostListingActivity extends AppCompatActivity {

    private static final String TAG = "PostListingActivity";

    @BindView(R.id.name_textinputlayout) TextInputLayout parkingNameTextInputLayout;
    @BindView(R.id.description_textinputlayout) TextInputLayout descriptionTextInputLayout;
    @BindView(R.id.price_textinputlayout) TextInputLayout priceTextInputLayout;

    @BindView(R.id.name_edittext) AppCompatEditText parkingNameEditText;
    @BindView(R.id.description_edittext) AppCompatEditText descriptionEditText;
    @BindView(R.id.price_edittext) AppCompatEditText priceEditText;

    private String nameString, descriptionString;
    private double price;

    @BindView(R.id.myRadioGroup) RadioGroup radioGroup;
    @BindView(R.id.refundable_rButton) RadioButton refundableRButton;
    @BindView(R.id.nonrefundable_rButton) RadioButton nonrefundableRButton;

    private static Hashtable<Integer, String> cancellationIds;

    //Parking image controls
    @BindView(R.id.upload_parking_button) Button uploadParkingImageButton;

    @BindView(R.id.parkingImage)
    ImageView parkingImageView;

    //Parking Type Buttons
    @BindView(R.id.handicap_button_control) SwitchCompat handicapSwitch;
    @BindView(R.id.compact_button_control) SwitchCompat compactSwitch;
    @BindView(R.id.covered_button_control) SwitchCompat coveredSwitch;

    // Date selector pieces
    @BindView(R.id.start_date_inputlayout) TextInputLayout startDateLayout;
    @BindView(R.id.stop_date_inputlayout) TextInputLayout stopDateLayout;
    @BindView(R.id.start_time_edittext) AppCompatEditText startDateEditText;
    @BindView(R.id.stop_time_edittext) AppCompatEditText stopDateEditText;

    @BindView(R.id.post_listing_toolbar) Toolbar postListingToolbar;

    private DatePickerDialog startDatePicker;
    private DatePickerDialog stopDatePicker;

    private TimePickerDialog startTimePicker;
    private TimePickerDialog stopTimePicker;

    private int startYear, startMonth, startDay, startHour, startMinute;

    private int stopYear, stopMonth, stopDay, stopHour, stopMinute;

    private long startDate, stopDate;

    boolean isCompact,isHandicap, isCovered, isRefundable;

    @BindView(R.id.upload_listing_button)
    Button uploadListingButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;
    private Uri sourceImageUri = null;
    private String firebaseImageURL = "";

    //NEED TO GET LONGITUDE AND LATITUDE
    private double longitude, latitude;

    private Place locationSelected;

    public static void startActivity(Context context) {
        Intent i = new Intent(context, PostListingActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_listing);
        ButterKnife.bind(this);

        setSupportActionBar(postListingToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Post a Listing");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.post_listing_autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    locationSelected = place;
                    latitude = locationSelected.getLatLng().latitude;
                    longitude = locationSelected.getLatLng().longitude;
                    Log.i(TAG, "Place: " + place.getName());
                }

                @Override
                public void onError(Status status) {// TODO: Handle the error.
                    Log.i(TAG, "An error occurred: " + status);
                }
            });
        }

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();
        parkingImageView.setImageResource(R.mipmap.empty_parking);

        nameString = "";
        descriptionString = "";
        price=0.0;
        startDate = 0;
        stopDate = 0;
        latitude = -1;
        longitude = -1;

        cancellationIds = new Hashtable<Integer, String>();
        cancellationIds.put(R.id.refundable_rButton, Consts.REFUNDABLE);
        cancellationIds.put(R.id.nonrefundable_rButton, Consts.NONREFUNDABLE);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

    }

    @OnClick(R.id.upload_parking_button)
    protected void uploadImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 1);
    }

    //Method called when user selects a picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Make sure the gallery Intent called this method
        if(requestCode==1 && resultCode==RESULT_OK && data!=null ){
            sourceImageUri = data.getData();
            parkingImageView.setImageURI(sourceImageUri);
        }
    }

    @OnClick(R.id.upload_listing_button)
    protected void submitListing() {
        if(checkFields()) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            String uid = currentUser.getUid();
            String listingID = mDatabase.child(Consts.LISTINGS_DATABASE).push().getKey();
            final DatabaseReference newListingRef = mDatabase.child(Consts.LISTINGS_DATABASE).child(uid).child(Consts.ACTIVE_LISTINGS).child(listingID);
            newListingRef.child(Consts.LISTING_NAME).setValue(nameString);
            newListingRef.child(Consts.LISTING_DESCRIPTION).setValue(descriptionString);
            newListingRef.child(Consts.LISTING_REFUNDABLE).setValue(isRefundable);
            newListingRef.child(Consts.LISTING_PRICE).setValue(price);
            newListingRef.child(Consts.LISTING_COMPACT).setValue(isCompact);
            newListingRef.child(Consts.LISTING_COVERED).setValue(isCovered);
            newListingRef.child(Consts.LISTING_HANDICAP).setValue(isHandicap);
            newListingRef.child(Consts.LISTING_LATITUDE).setValue(latitude);
            newListingRef.child(Consts.LISTING_LONGITUDE).setValue(longitude);
            newListingRef.child(Consts.LISTING_START_TIME).setValue(startDate);
            newListingRef.child(Consts.LISTING_END_TIME).setValue(stopDate);

            StorageReference storageRef = storage.getReferenceFromUrl(Consts.STORAGE_URL);
            StorageReference parkingRef = storageRef.child(Consts.STORAGE_PARKING_SPACES);
            //Best way to store the data?
            if (sourceImageUri == null) {
                newListingRef.child(Consts.LISTING_IMAGE).setValue(Consts.DEFAULT_PARKING_IMAGE);
            } else {
                //compress image
                InputStream imageStream = null;
                try {
                    imageStream = getContentResolver().openInputStream(sourceImageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Bitmap bmp = BitmapFactory.decodeStream(imageStream);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), bmp,
                        "Title", null);
                sourceImageUri = Uri.parse(path);

                try {
                    stream.close();
                    stream = null;
                } catch (IOException e) {

                    e.printStackTrace();
                }


                UploadTask uploadTask = parkingRef.child(listingID).putFile(sourceImageUri);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.d(TAG, exception.toString());
                        Toast.makeText(PostListingActivity.this, "Unable to upload the image. Please check your internet connection and try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        firebaseImageURL = taskSnapshot.getDownloadUrl().toString();
                        newListingRef.child(Consts.LISTING_IMAGE).setValue(firebaseImageURL);
                    }
                });
            }
            finish();
        }
    }

    @OnClick(R.id.start_time_edittext)
    protected void selectStartTime() {
        Calendar c = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener startTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                startHour = hourOfDay;
                startMinute = minute;
                DateTime dateTime = new DateTime(startYear, startMonth, startDay, startHour, startMinute);
                startDate = dateTime.getMillis() / 1000;
                startDateEditText.setText(Tools.getDateString(dateTime));
            }
        };
        startTimePicker = new TimePickerDialog(this, startTimeListener, c.get(Calendar.HOUR), c.get(Calendar.MINUTE), false);
        DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                startYear = year;
                startMonth = month + 1;
                startDay = day;
                startTimePicker.show();
            }
        };
        startDatePicker = new DatePickerDialog
                (this, startDateListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        startDatePicker.show();
    }

    @OnClick(R.id.stop_time_edittext)
    protected void selectStopTime() {
        Calendar c = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener stopTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                stopHour = hourOfDay;
                stopMinute = minute;
                DateTime dateTime = new DateTime(stopYear, stopMonth, stopDay, stopHour, stopMinute);
                stopDate = dateTime.getMillis() / 1000;
                stopDateEditText.setText(Tools.getDateString(dateTime));
            }
        };
        stopTimePicker = new TimePickerDialog(this, stopTimeListener, c.get(Calendar.HOUR), c.get(Calendar.MINUTE), false);
        DatePickerDialog.OnDateSetListener stopDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                stopYear = year;
                stopMonth = month + 1;
                stopDay = day;
                stopTimePicker.show();
            }
        };
        stopDatePicker = new DatePickerDialog
                (this, stopDateListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        stopDatePicker.show();
    }


    public boolean checkFields(){
        nameString = parkingNameEditText.getText().toString();
        descriptionString = descriptionEditText.getText().toString();
        boolean isValid = true;
        try {
            price = Double.parseDouble(priceEditText.getText().toString());
            if (price > 0) {
                priceTextInputLayout.setErrorEnabled(false);
            } else {
                isValid = false;
                priceTextInputLayout.setErrorEnabled(true);
                priceTextInputLayout.setError("Not a valid price");
            }
        } catch (NumberFormatException nfe) {
            isValid = false;
            priceTextInputLayout.setErrorEnabled(true);
            priceTextInputLayout.setError("Not a valid price");
        }

        if (nameString.isEmpty()) {
            isValid = false;
            parkingNameTextInputLayout.setErrorEnabled(true);
            parkingNameTextInputLayout.setError("Please enter a name.");
        } else {
            parkingNameTextInputLayout.setErrorEnabled(false);
        }
        if (descriptionString.isEmpty()) {
            isValid = false;
            descriptionTextInputLayout.setErrorEnabled(true);
            descriptionTextInputLayout.setError("Please enter a description.");
        } else {
            descriptionTextInputLayout.setErrorEnabled(false);
        }
        if (price <= 0){
            isValid = false;
            priceTextInputLayout.setErrorEnabled(true);
            priceTextInputLayout.setError("Please enter a price greater than $0");
        } else {
            priceTextInputLayout.setErrorEnabled(false);
        }
        // allow the user now to upload a parking space with the blank/default image
//        if (sourceImageUri == null) {
//            isValid = false;
//            Toast.makeText(PostListingActivity.this, "Please upload a picture of your parking spot.",
//                    Toast.LENGTH_SHORT).show();
//        }
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            isValid = false;
            Toast.makeText(PostListingActivity.this, "Please select a cancellation policy.",
                    Toast.LENGTH_SHORT).show();
        }
        if (startDate == 0) {
            isValid = false;
            startDateLayout.setError("Please select a start date");
            startDateLayout.setErrorEnabled(true);
        } else {
            startDateLayout.setErrorEnabled(false);
        }
        if (stopDate == 0) {
            isValid = false;
            stopDateLayout.setError("Please select a stop date");
            stopDateLayout.setErrorEnabled(true);
        } else {
            stopDateLayout.setErrorEnabled(false);
        }
        if (startDate != 0 && stopDate != 0) {
            if (startDate >= stopDate) {
                isValid = false;
                startDateLayout.setError("Please select a valid start date");
                startDateLayout.setErrorEnabled(true);
                stopDateLayout.setError("Please select a valid stop date");
                stopDateLayout.setErrorEnabled(true);
            }
        }
        if (latitude == -1 && longitude == -1) {
            isValid = false;
            Toast.makeText(PostListingActivity.this, "Please select a location through the search bar.",
                    Toast.LENGTH_SHORT).show();
        }
        saveSwitchValues();
        return isValid;
    }

    private void saveSwitchValues(){
        isCompact = compactSwitch.isChecked();
        isCovered = coveredSwitch.isChecked();
        isHandicap =  handicapSwitch.isChecked();
        isRefundable = radioGroup.getCheckedRadioButtonId() == R.id.refundable_rButton;
    }

    public void fillInFields(Listing listing) {
        parkingNameEditText.setText(listing.getName());
        descriptionEditText.setText(listing.getDescription());
        priceEditText.setText(Double.toString(listing.getPrice()));
        refundableRButton.toggle();
        if (listing.isRefundable()) {
            radioGroup.check(R.id.refundable_rButton);
        } else {
            radioGroup.check(R.id.nonrefundable_rButton);
        }
        compactSwitch.setChecked(listing.isCompact());
        coveredSwitch.setChecked(listing.isCovered());
        handicapSwitch.setChecked(listing.isHandicap());
        latitude = listing.getLatitude();
        longitude = listing.getLongitude();
        startDate = listing.getStartTime();
        stopDate = listing.getStopTime();
    }
}