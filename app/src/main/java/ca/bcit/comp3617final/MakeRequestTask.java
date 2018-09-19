package ca.bcit.comp3617final;

import android.app.Dialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import static ca.bcit.comp3617final.MainActivity.REQUEST_GOOGLE_PLAY_SERVICES;

/**
 * An asynchronous task that handles the Google Calendar API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */

class MakeRequestTask extends AsyncTask<Void, Void, Void>
{
    private static final String TAG = MakeRequestTask.class.getName();
    private MainActivity activity;

    private com.google.api.services.calendar.Calendar mService = null;
    private Exception mLastError = null;

    MakeRequestTask(MainActivity activity, GoogleAccountCredential credential) {
        this.activity = activity;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Google Calendar Workout Tracker")
                .build();
    }

    /**
     * Background task to call Google Calendar API.
     *
     * @param params no parameters needed for this task.
     */
    @Override
    protected Void doInBackground(Void... params) {
        try {
            getDataFromApi();
        } catch (Exception e) {
            Log.wtf(TAG, e.toString());
            mLastError = e;
            cancel(true);
        }
        return null;
    }

    /**
     * Fetch a list of the next 10 events from the primary calendar.
     *
     * @return List of Strings describing returned events.
     * @throws IOException
     */
    private void getDataFromApi() throws IOException {
        DateTime startOfDay = new DateTime(getStartOfDayInMillis());
        DateTime endOfDay = new DateTime(getEndOfDayInMillis());
        List<String> eventStrings = new ArrayList<String>();

        Events events = mService.events().list(getWorkoutCalendarId())
                .setMaxResults(20)
                .setTimeMin(startOfDay)
                .setTimeMax(endOfDay)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();

        Log.wtf(TAG, "Number of events retried: " + items.size());

        for (Event event : items) {
            MainActivity.exerciseNames.add(event.getSummary());
            MainActivity.exerciseIDs.add(event.getId());
            if (!MainActivity.exerciseData.containsKey(event.getId()))
            {
                MainActivity.exerciseData.put(event.getId(), parseEventDescription(event));
            }
        }

        Log.wtf(TAG, "Number of events in exerciseNames: " + MainActivity.exerciseNames.size());
    }

    private ExerciseDetail parseEventDescription(Event event)
    {
        final int set, rep, weight;

        Scanner scanner = new Scanner(event.getDescription());

        scanner.next();
        set = scanner.nextInt();
        scanner.nextLine();
        scanner.next();
        rep = scanner.nextInt();
        if (scanner.hasNextLine())
        {
            scanner.nextLine();
            scanner.next();
            weight = scanner.nextInt();
        }
        else
        {
            weight = 0;
        }

        Log.wtf(TAG, event.getSummary() + ": " + set + ", " + rep + ", " + weight);
        scanner.close();
        return new ExerciseDetail(event.getId(), event.getSummary(), set, rep, weight, event.getColorId(), false);
    }

    /**
     * Looks through the CalendarList and returns the calendarId if there is a
     * CalendarListEntry with the summary: "Workout"
     *
     * @return calendarId for the "Workout" calendar
     * @throws IOException
     */
    protected String getWorkoutCalendarId() throws IOException {
        String calendarId = null;
        String pageToken = null;
        do {
            CalendarList calendarList = mService.calendarList().list()
                    .setPageToken(pageToken)
                    .execute();
            List<CalendarListEntry> items = calendarList.getItems();

            for (CalendarListEntry calendarListEntry : items) {
                if (calendarListEntry.getSummary().equalsIgnoreCase("Workout"))
                {
                    calendarId = calendarListEntry.getId();
                }
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);

        return calendarId;
    }


    @Override
    protected void onPreExecute() {
        activity.mOutputText.setText("");
        activity.mProgress.show();
    }

    @Override
    protected void onCancelled() {
        activity.mProgress.hide();
        if (mLastError != null) {
            if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                showGooglePlayServicesAvailabilityErrorDialog(
                        ((GooglePlayServicesAvailabilityIOException) mLastError)
                                .getConnectionStatusCode());
            } else if (mLastError instanceof UserRecoverableAuthIOException) {
                activity.startActivityForResult(
                        ((UserRecoverableAuthIOException) mLastError).getIntent(),
                        MainActivity.REQUEST_AUTHORIZATION);
            } else {
                activity.mOutputText.setText("The following error occurred:\n"
                        + mLastError.getMessage());
            }
        } else {
            activity.mOutputText.setText("Request cancelled.");
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        activity.mProgress.dismiss();
        activity.adapter.notifyDataSetChanged();
    }

    private long getStartOfDayInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfDayInMillis() {
        // Add one day's time to the beginning of the day.
        // 24 hours * 60 minutes * 60 seconds * 1000 milliseconds = 1 day
        return getStartOfDayInMillis() + (24 * 60 * 60 * 1000);
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    private void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                activity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
}
