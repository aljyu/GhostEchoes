package com.example.ghostechoes.ghostechoes;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GetEcho extends AppCompatActivity {
    String LOG_TAG = "GetEcho";

    int SET_MILES = 3;
    Double METER_LIMIT = SET_MILES * 1609.34;

    // Buttons, Views
    Button btn_map;

    // Request
    RequestQueue queue;

    // GPS
    LocationTracker gps;

    private class ListElement {
        ListElement() {};
        public String textLabel;
        public String subTextLabel;

        ListElement(String t1, String t2) {
            textLabel = t1;
            subTextLabel = t2;
        }
    }
    private ArrayList<ListElement> aList;

    private class MyAdapter extends ArrayAdapter<ListElement> {
        int resource;
        Context context;

        public MyAdapter(Context _context, int _resource, List<ListElement> items) {
            super(_context,_resource, items);
            resource = _resource;
            context = _context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout newView;
            ListElement w = getItem(position);

            // Inflate new view if needed
            if (convertView == null) {
                newView = new LinearLayout(getContext());
                LayoutInflater vi = (LayoutInflater)
                        getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                vi.inflate(resource,  newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            TextView tvCoordinates = (TextView) newView.findViewById(R.id.itemText);
            TextView tvMessage = (TextView) newView.findViewById(R.id.itemSubText);
            tvCoordinates.setText(w.textLabel);
            tvMessage.setText(w.subTextLabel);

            // Set listener for whole list item
            newView.setTag(w.subTextLabel);
            newView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent echoForm_intent = new Intent(context, AnonEchoFormActivity.class);
                    String echoMessage = v.getTag().toString();
                    echoForm_intent.putExtra("echoMessage", echoMessage);

                    String s = v.getTag().toString();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, "Opening Echo", duration);
                    toast.show();

                    startActivity(echoForm_intent);

                }
            });
            return newView;
        }
    }

    private MyAdapter aa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_echo_list);
        // Clickable Objects
        btn_map = (Button) findViewById(R.id.goToMap);
        // Request Queue
        queue = Volley.newRequestQueue(this);
        // Get echoes from database
        aList = new ArrayList<ListElement>();
        aa = new MyAdapter(this, R.layout.echo_list_element, aList);
        ListView myListView = (ListView) findViewById(R.id.listView);
        myListView.setAdapter(aa);
        aa.notifyDataSetChanged();
    }

    /**
     * Volley Callback
     */
    public interface VolleyCallback {
        void onSuccessResponse(JSONArray jsonResponse);
    }

    @Override
    protected void onResume() {
        super.onResume();
        clickRefresh(null);
    }

    /**
     * Sends GET request to server to retrieve data (i.e. location, text)
     * into database.
     */
    public void getEcho(final VolleyCallback callback) {
        final TextView msg = (TextView) findViewById(R.id.systemMessage);
        // Request queue
        queue = Volley.newRequestQueue(this);
        String url = "http://darkfeather2.pythonanywhere.com/get_data";
        // Request a string response from the provided URL.
        final JsonArrayRequest jsonArrayReq = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                ArrayList<JSONObject> jsonList = new ArrayList<>();
                for(int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        Double latitude;
                        Double longitude;
                        try {
                            latitude = jsonObject.getDouble("latitude");
                            longitude = jsonObject.getDouble("longitude");
                        } catch (JSONException e) {
                            continue;
                        }
                        // Display only echoes within set range
                        if (isEchoInRange(latitude, longitude)) {
                            jsonList.add(jsonObject);
                        }
                    } catch (Exception e) {
                        Log.d(LOG_TAG, e.toString());
                    }
                }
                JSONArray jsonResponse = new JSONArray(jsonList);
                callback.onSuccessResponse(jsonResponse);

                String jsonResponseData = jsonList.toString();
                Log.d(LOG_TAG, "Response is: "+ jsonResponse);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                msg.setText("Please try again!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(jsonArrayReq);
    }

    /**
     * Calls getEcho to send GET Request for echo data.
     * On successful response, update list with latest data.
     */
    public void clickRefresh (View v) {
        getEcho(new VolleyCallback() {
            @Override
            public void onSuccessResponse(JSONArray jsonResponse) {
                // aList is associated with array adapter
                aList.clear();
                for (int i = 0; i < jsonResponse.length(); i++) {
                    try {
                        JSONObject jObj = jsonResponse.getJSONObject(i);
                        Double latitude = jObj.getDouble("latitude");
                        Double longitude = jObj.getDouble("longitude");
                        String message = jObj.getString("message");

                        // Retrieve Location Address
                        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
                        String coordinates;
                        List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
                        if (addresses.size() > 0) {
                            coordinates = addresses.get(0).getLocality();
                        } else {
                            coordinates = latitude.toString() + ", " + longitude.toString();
                        }
                        aList.add(new ListElement(coordinates, message));
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "JSON List Error");
                    }
                }
                // We notify the ArrayList adapter that the underlying list has changed,
                // triggering a re-rendering of the list
                aa.notifyDataSetChanged();
            }
        });
        Log.i(LOG_TAG, "Requested a refresh of the list");
    }

    /**
     * Checks and returns boolean value of whether an echo that is within
     * range of user's set current location.
     */
    public boolean isEchoInRange(double latitude, double longitude) {
        gps = new LocationTracker(getApplicationContext());
        if (gps.canGetLocation()) {
            double currentLatitude = gps.getLatitude();
            double currentLongitude = gps.getLongitude();

            if (gps.radius(latitude, longitude) <= METER_LIMIT) {
                return true;
            }
        } else {
            Log.d(LOG_TAG, "Cannot get current location");
            return false;
        }
        return false;
    }

    /**
     * Return to map activity.
     */
    public void goToMap(View v) {
        // Should only go to echo when location can be retrieved
        Intent intent = new Intent(this, GoogleMapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
