package ca.bcit.comp3617final;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Map;

public class Exercise
        extends AppCompatActivity
{
    private static final String TAG = Exercise.class.getName();

    private String id;
    private int setCount = 0;
    private Map<String, ExerciseDetail> data;
    private ExerciseDetail exercise;
    private TextView exerciseName;
    private TextView setVal;
    private TextView repVal;
    private TextView weight;
    private TextView weightVal;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Intent intent = getIntent();
        id = intent.getStringExtra("id");

        data = MainActivity.exerciseData;
        exercise = data.get(id);
        Log.wtf(TAG, exercise.getEventName());
        Log.wtf(TAG, "" + exercise.getSet());
        Log.wtf(TAG, "" + exercise.getRep());
        Log.wtf(TAG, "" + exercise.getWeight());

        exerciseName = (TextView) findViewById(R.id.exerciseName);
        exerciseName.setText(exercise.getEventName());

        setVal = (TextView) findViewById(R.id.exerciseSetsValue);
        setVal.setText("" + exercise.getSet());

        repVal = (TextView) findViewById(R.id.exercoseRepsValue);
        repVal.setText("" + exercise.getRep());

        if (exercise.getWeight() > 0)
        {
            weight = (TextView) findViewById(R.id.exerciseWeight);
            weight.setText("Weight(lb): ");
            weightVal = (TextView) findViewById(R.id.exerciseWeightValue);
            weightVal.setText("" + exercise.getWeight());
        }

        button = (Button) findViewById(R.id.exerciseButton);
        button.setText("Start Exercise!");
    }

    public void ButtonClicked(View view)
    {
        if (setCount == 0)
        {
            setCount++;
            button.setText("Finished set " + setCount);
        }
        else if (setCount < exercise.getSet())
        {
            setCount++;
            button.setText("Finished set " + setCount);
        }
        else if (setCount == exercise.getSet())
        {
            setCount++;
            button.setText("Complete exercise!");
            button.setTextColor(Color.BLACK);
            button.setBackgroundColor(getResources().getColor(R.color.exerciseComplete));
        }
        else
        {
            data.get(id).setComplete(true);
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.complete);
            mp.start();
            finish();
        }
    }
}
