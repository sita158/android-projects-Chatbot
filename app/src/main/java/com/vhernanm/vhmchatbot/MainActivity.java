package com.vhernanm.vhmchatbot;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.internal.LinkedTreeMap;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.Entity;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ConversationService service = null;
    private MessageResponse previousResponse = null;
    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Instanciar el servicio de Watson Conversation
        service = new ConversationService("2017-07-13");
        service.setUsernameAndPassword("cc3c959c-598c-4ed5-8f62-0290e1c63502", "zOE4pu5q5WIs");
    }

    public void sendMessage (View view){
        final TextView output = (TextView) findViewById(R.id.output);
        final EditText input = (EditText)findViewById(R.id.input);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    final String message = input.getText().toString();
                    final String outputString = output.getText().toString();

                    MessageRequest newMessage;

                    //Para construir el MessageRequest hay que tener en cuenta si es la primera interacción o si ya ha habido
                    //diálogo para enviarle el conexto.
                    if(previousResponse==null){
                        newMessage = new MessageRequest.Builder()
                                .inputText(message)
                                .build();
                    } else {
                        newMessage = new MessageRequest.Builder()
                                .inputText(message)
                                // Replace with the context obtained from the initial request
                                .context(previousResponse.getContext())
                                .build();

                        //Log.d("App", "Visited: " + (((ArrayList)((LinkedTreeMap)previousResponse.getOutput()).get("nodes_visited")).get(0)));
                    }

                    String workspaceId = "84621005-e9c1-4611-80cb-920ac2beb2ea";

                    //Obteniendo la respuesta de Watson, dependiendo del MessageRequest.
                    final MessageResponse response = service
                            .message(workspaceId, newMessage)
                            .execute();

                    String stringResponse = "";


                    //Aquí vamos a revisar si hay una interacción previa y si esta corresponde al nodo "Obtener Información",
                    //ya que entonces lo que sigue es determinar la ciudad por medio de las entidades.
                    if(previousResponse != null && (((ArrayList)((LinkedTreeMap)previousResponse.getOutput()).get("nodes_visited")).get(0)).toString().equalsIgnoreCase("obtener información")){
                        if(((ArrayList)response.getEntities()).size()>0){

                            //Se revisa cuál entidad corresponde al mensaje actual, para enviar las coordenadas geográficas
                            //de la ciudad solicitada.
                            String ciudad = ((Entity)((ArrayList)response.getEntities()).get(0)).getValue();
                            Log.d("App", "Ciudad: "+ciudad);

                            Location location = new Location("");
                            switch (ciudad)
                            {
                                case "Campeche":
                                    location.setLatitude(19.830125);
                                    location.setLongitude(-90.534909);
                                    //Obtener el string del clima (se tiene que actualizar allí directamente por la asincronía)
                                    getWeather(location, outputString, message, input, output);
                                    flag = true;
                                    break;
                                case "Cancún":
                                    location.setLatitude(21.161908);
                                    location.setLongitude(-86.851528);
                                    //Obtener el string del clima (se tiene que actualizar allí directamente por la asincronía)
                                    getWeather(location, outputString, message, input, output);
                                    flag = true;
                                    break;
                                case "Ciudad de México":
                                    location.setLatitude(19.4326);
                                    location.setLongitude(-99.1332);
                                    //Obtener el string del clima (se tiene que actualizar allí directamente por la asincronía)
                                    getWeather(location, outputString, message, input, output);
                                    flag = true;
                                    break;
                                case "Mérida":
                                    location.setLatitude(20.967370);
                                    location.setLongitude(-89.592586);
                                    //Obtener el string del clima (se tiene que actualizar allí directamente por la asincronía)
                                    getWeather(location, outputString, message, input, output);
                                    flag = true;
                                    break;
                                case "Monterrey":
                                    location.setLatitude(25.686614);
                                    location.setLongitude(-100.316113);
                                    //Obtener el string del clima (se tiene que actualizar allí directamente por la asincronía)
                                    getWeather(location, outputString, message, input, output);
                                    flag = true;
                                    break;
                                case "Querétaro":
                                    location.setLatitude(20.588793);
                                    location.setLongitude(-100.389888);
                                    //Obtener el string del clima (se tiene que actualizar allí directamente por la asincronía)
                                    getWeather(location, outputString, message, input, output);
                                    flag = true;
                                    break;
                                case "Toluca":
                                    location.setLatitude(19.282610);
                                    location.setLongitude(-99.655665);
                                    //Obtener el string del clima (se tiene que actualizar allí directamente por la asincronía)
                                    getWeather(location, outputString, message, input, output);
                                    flag = true;
                                    break;
                                default:
                                    stringResponse = "[ No tengo información de esa ciudad ]";
                                    break;
                            }
                        }
                    } else{
                        stringResponse = response.getText().toString();
                    }


                    final String stringResponseFinal = stringResponse;

                    Log.d("App", "Response: " + response.toString());
                    Log.d("App", "Entities: " + response.getEntities().toString());

                    //En caso de que aún no se haya actualizado la interfaz, porque la interacción no correspondía a los nodos de
                    //entidades, hacerlo en este punto
                    if(!flag){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                output.setText(outputString + "\n" + message +"\n"+ stringResponseFinal);
                                input.setText("");
                            }
                        });
                    }

                    //Conservar la respuesta previa y resetear el flag que señala si se está trabajando con entidades.
                    previousResponse = response;
                    flag=false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void getWeather (Location location, final String outputString, final String message, final EditText input, final TextView output){
        //URL de mi instancia de Weather Company Data Service en Bluemix
        String url = "https://eb2a8f8b-bbfd-4155-8051-7e9b312dd994:MFbBpJ4FAH@twcservice.mybluemix.net/api/weather/v1/geocode/" + location.getLatitude() + "/" + location.getLongitude() + "/observations.json?units=m&language=es-MX";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("App", "Response: " + response);

                try{
                    //En caso de obtener una respuesta exitosa, obtener el valor de "observation" y dentro de éste "wx_phrase" y "temp"
                    JSONObject weatherObject = new JSONObject(response);
                    JSONObject observation = weatherObject.getJSONObject("observation");
                    final String stringResponseFinal= "[" + observation.getString("wx_phrase") + ", " + observation.getString("temp") + "°C ]";

                    //Actualizar interfaz
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            output.setText(outputString + "\n" + message +"\n"+ stringResponseFinal);
                            input.setText("");
                        }
                    });

                } catch (JSONException e){
                    Log.d("App", e.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //En caso de no poder obtener el clima, colocar mensaje de error en la interfaz
                Log.d("App", "Error: " + error.getMessage());
                final String stringResponseFinal= "[ Disculpa, no pude obtener el clima para esa ciudad. ]";

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        output.setText(outputString + "\n" + message +"\n"+ stringResponseFinal);
                        input.setText("");
                    }
                });
            }
        })
        {
            //Credenciales
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                String creds = String.format("%s:%s","eb2a8f8b-bbfd-4155-8051-7e9b312dd994","MFbBpJ4FAH");
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;

            }
        };

        //Enviar request
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }
}
