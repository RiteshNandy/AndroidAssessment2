package com.ritesh.assessment2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button timeButton, timeButton2;
    int hour, minute;

    EditText title, location, description;
    Button addEvent;
    SwitchCompat allDay,accessSwitch;

    //Initializing for date picker spinner.
    private DatePickerDialog datePickerDialog;
    private Button dateButton;

    //Initializing for Photo Capture
    Button captureBtn;
    ImageView imageView;
    static final int PERMISSION_CODE = 1000;
    static final int IMAGE_CAPTURE_CODE = 1001;
    Uri image_uri;

    //Initializing for Call
    EditText phoneNo;
    Button callBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing for events
        title = findViewById(R.id.title);
        location = findViewById(R.id.location);
        description = findViewById(R.id.description);
        addEvent = findViewById(R.id.event);
        allDay = findViewById(R.id.allDaySwitch);
        accessSwitch = findViewById(R.id.accessSwitch);

        //Initializing for start and end time pickers
        timeButton = findViewById(R.id.startTimePicker);
        timeButton2 = findViewById(R.id.endTimePicker);

        //Initializing for photo intents
        imageView = findViewById(R.id.imageView);
        captureBtn = findViewById(R.id.capture);

        //Initializing for date pickers
        initDatePicker();
        dateButton = findViewById(R.id.datePickerButton);
        dateButton.setText(getTodaysDate());

        //Initializing for Call intent
        phoneNo = findViewById(R.id.phoneNo);
        callBtn = findViewById(R.id.callBtn);

        //Action listener for event intent
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!title.getText().toString().isEmpty() && !location.getText().toString().isEmpty()
                        && !description.getText().toString().isEmpty()){

                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setData(CalendarContract.Events.CONTENT_URI);
                    intent.putExtra(CalendarContract.Events.TITLE, title.getText().toString());
                    intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location.getText().toString());
                    intent.putExtra(CalendarContract.Events.DESCRIPTION, description.getText().toString());
                    intent.putExtra(CalendarContract.Events.ACCESS_LEVEL, accessSwitch.getText().toString());
                    if(allDay.isChecked()) {
                        intent.putExtra(CalendarContract.Events.ALL_DAY, true);
                    }else{
                        //start time and end time displayed in calender app
                        intent.putExtra(CalendarContract.Events.DTSTART, timeButton.getText().toString());
                        intent.putExtra(CalendarContract.Events.DTEND, timeButton2.getText().toString());
                    }
                    if(intent.resolveActivity(getPackageManager())!= null) {
                        startActivity(intent);
                    }else {
                        Toast.makeText(MainActivity.this, "No such App to handle Intent",
                                Toast.LENGTH_SHORT).show();
                    }
                }else
                {
                    Toast.makeText(MainActivity.this, "Please fill all the details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Action listener for photo intent
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_DENIED ||
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED){

                    //permissions not provided so requesting it
                    String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

                    //popups to request permissions
                    requestPermissions(permission, PERMISSION_CODE);
                }
                else {
                    //if permission granted
                    openCamera();
                }
            }
        });

        //Action listener for call intent
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+phoneNo.getText().toString()));
                startActivity(intent);
            }
        });
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //when user is asked about permissions
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permissions was granted
                    openCamera();
                } else {
                    //permissions from popup wasn't granted
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //called when image is captured
        if(resultCode == RESULT_OK){
            //set image captured to imageviewer
            imageView.setImageURI(image_uri);
        }
    }

    private String getTodaysDate()
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private void initDatePicker()
    {

        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener(){

            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = makeDateString(day, month, year);
                dateButton.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.BUTTON_NEUTRAL;

        datePickerDialog = new DatePickerDialog(this, style, dateListener, year, month, day);

    }

    private String makeDateString(int day, int month, int year)
    {
        return getMonthFormat(month) + " " + day + " " + year;
    }

    private String getMonthFormat(int month)
    {
        if(month == 1)
            return "JAN";
        if(month == 2)
            return "FEB";
        if(month == 3)
            return "MAR";
        if(month == 4)
            return "APR";
        if(month == 5)
            return "MAY";
        if(month == 6)
            return "JUN";
        if(month == 7)
            return "JUL";
        if(month == 8)
            return "AUG";
        if(month == 9)
            return "SEP";
        if(month == 10)
            return "OCT";
        if(month == 11)
            return "NOV";
        if(month == 12)
            return "DEC";

        //default should never happen
        return "JAN";
    }

    public void datePicker(View view)
    {
        datePickerDialog.show();
    }

    public void startTimePicker(View view) {

        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
            {
                hour = selectedHour;
                minute = selectedMinute;
                timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d",hour, minute));
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                onTimeSetListener, hour, minute, true);

        timePickerDialog.show();
    }

    public void endTimePicker(View view) {

        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
            {
                hour = selectedHour;
                minute = selectedMinute;
                timeButton2.setText(String.format(Locale.getDefault(), "%02d:%02d",hour, minute));
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                onTimeSetListener, hour, minute, true);

        timePickerDialog.show();
    }
}