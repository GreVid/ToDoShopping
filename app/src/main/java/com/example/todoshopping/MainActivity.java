package com.example.todoshopping;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private List<ThingToBuy> thingsToBuy = new ArrayList<>();
    private List<ThingToBuy> selectedThingsToBuy = new ArrayList<>();
    private final RecyclerView adapter = new RecyclerView(thingsToBuy, MainActivity.this);
    private Animation animationEinblenden;
    private Animation animationWackeln;
    private FloatingActionButton floatingButtonAdd;
    private Handler handler = new Handler();
    private Menu optionsMenu;
    private RequestQueue requestQueue;
    private LinearLayout linearLayout;
    private int numDeleteRequests;

    @Override
    protected void onResume() {
        View v = findViewById(R.id.constraintLayout);
        v.startAnimation(animationEinblenden);
        super.onResume();
        handler.postDelayed(new WackleButton(), 3000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.sendToServerButton) {
            numDeleteRequests = selectedThingsToBuy.size();
            Iterator<ThingToBuy> selectedThingsIterator = selectedThingsToBuy.iterator();
            makeDeleteRequest(selectedThingsIterator);
            onPrepareOptionsMenu(optionsMenu);
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem sendToServerButton = menu.findItem(R.id.sendToServerButton);
        sendToServerButton.setVisible(!selectedThingsToBuy.isEmpty());
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        animationEinblenden = AnimationUtils.loadAnimation(this, R.anim.einblenden);
        animationWackeln = AnimationUtils.loadAnimation(this, R.anim.wackeln);
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(MainActivity.this);
        setContentView(R.layout.activity_main);

        setupRecyclerView();

        final EditText productName = new EditText(this);
        final Button sendAdd = new Button(this);
        final Button cancelAdd = new Button(this);
        final LinearLayout buttonsContainer = new LinearLayout(this);
        setupAddProductField(productName, sendAdd, cancelAdd, buttonsContainer);

        linearLayout = this.findViewById(R.id.linearLayout);
        final ConstraintLayout constraintLayout = this.findViewById(R.id.constraintLayout);
        floatingButtonAdd = this.findViewById(R.id.floatingActionButton);
        floatingButtonAdd.setOnClickListener(
                new FloatingActionButton.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Slide slide = new Slide();
                        slide.setDuration(500);
                        slide.setSlideEdge(Gravity.RIGHT);
                        TransitionManager.beginDelayedTransition(linearLayout, slide);
                        constraintLayout.removeView(floatingButtonAdd);
                        linearLayout.addView(productName, 0);
                        linearLayout.addView(buttonsContainer, 1);
                    }
                }
        );

        sendAdd.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String nameOfThingToBuy = productName.getText().toString();
                        makePostRequest(nameOfThingToBuy);
                        productName.setText("");
                    }
                }
        );

        cancelAdd.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        productName.setText("");
                        Slide slide = new Slide();
                        slide.setDuration(500);
                        slide.setSlideEdge(Gravity.RIGHT);
                        TransitionManager.beginDelayedTransition(linearLayout, slide);
                        constraintLayout.addView(floatingButtonAdd);
                        linearLayout.removeView(productName);
                        linearLayout.removeView(buttonsContainer);
                    }
                }
        );

        addSwipeToRefresh();
        refreshProducts();
    }

    private void addSwipeToRefresh() {
        final SwipeRefreshLayout pullToRefresh;
        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshProducts();
                pullToRefresh.setRefreshing(false);
            }
        });
    }

    private void setupAddProductField(EditText productName, Button sendAdd, Button cancelAdd,
                                      LinearLayout buttonsContainer) {
        productName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES |
                InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        productName.setHint("Was muss gekauft werden?");
        productName.setWidth(200);
        sendAdd.setText("Hinzufügen");
        sendAdd.setWidth(400);
        sendAdd.setBackgroundColor(Color.parseColor("#174066"));
        sendAdd.setTextColor(Color.parseColor("#FFFFFF"));
        cancelAdd.setText("Abbrechen");
        cancelAdd.setWidth(400);
        buttonsContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonsContainer.addView(cancelAdd, 0);
        buttonsContainer.addView(sendAdd, 1);
    }

    private void setupRecyclerView() {
        android.support.v7.widget.RecyclerView recyclerView = findViewById(R.id.List);
        android.support.v7.widget.RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        ((LinearLayoutManager) layoutManager).setReverseLayout(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(false);
    }

    public void refreshProducts() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                makeGetRequest();
            }
        }, 0);
    }

    public void addToSelected(ThingToBuy thingToBuy) {
        optionsMenu.findItem(R.id.sendToServerButton).setVisible(true);
        selectedThingsToBuy.add(thingToBuy);
    }

    public void removeFromSelected(ThingToBuy thingToBuy) {
        selectedThingsToBuy.remove(thingToBuy);
        if (selectedThingsToBuy.isEmpty()) {
            optionsMenu.findItem(R.id.sendToServerButton).setVisible(false);
        }
    }

    public void makeGetRequest() {
        thingsToBuy.removeAll(thingsToBuy);
        selectedThingsToBuy.removeAll(thingsToBuy);
        String url = "";
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Iterator<String> keys = response.keys();
                        while(keys.hasNext()) {
                            String key = keys.next();
                            try {
                                String val = response.getString (key);
                                JSONObject valObj = new JSONObject(val);
                                String title = valObj.getString ("title");
                                ThingToBuy thingToBuy = new ThingToBuy();
                                thingToBuy.id = key;
                                thingToBuy.name = title;
                                thingsToBuy.add(0, thingToBuy);
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) { }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof ParseError) {
                            adapter.notifyDataSetChanged();
                            Toast.makeText(MainActivity.this, "Liste ist leer.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Produkte können " +
                                    "im Moment nicht aktualisiert werden.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        requestQueue.add(objectRequest);
    }

    public void makePostRequest(String title) {
        Map<String, String> params = new HashMap();
        params.put("amount", "1");
        params.put("title", title);
        JSONObject parameters = new JSONObject(params);
        String url = "";
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, url, parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        makeGetRequest();
                    }
                },
        new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Produkte können " +
                        "im Moment nicht aktualisiert werden.", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(objectRequest);
    }

    public void countSuccessfulDeleteRequests() {
        numDeleteRequests -= 1;
        Toast.makeText(MainActivity.this, "Gelöscht", Toast.LENGTH_SHORT).show();
        if (numDeleteRequests == 0) {
            selectedThingsToBuy.clear();
            optionsMenu.findItem(R.id.sendToServerButton).setVisible(false);
            makeGetRequest();
        }
    }

    public void makeDeleteRequest(Iterator<ThingToBuy> selectedThingsIterator) {
        while (selectedThingsIterator.hasNext()) {
            ThingToBuy thingToBuy = selectedThingsIterator.next();
            String id = thingToBuy.id;
            String url = "";
            JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.DELETE, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            countSuccessfulDeleteRequests();
                            Toast.makeText(MainActivity.this, "DELETING", Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                       @Override
                        public void onErrorResponse(VolleyError error) {
                           if (error instanceof ParseError) {
                               countSuccessfulDeleteRequests();
                           }
                       }
                    });
            objectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    4000,
                    1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(objectRequest);
        }
    }

    @Override
    public void onInit(int status) { }

    private class WackleButton implements Runnable {
        @Override
        public void run() {
            floatingButtonAdd.startAnimation(animationWackeln);
        }
    }
}