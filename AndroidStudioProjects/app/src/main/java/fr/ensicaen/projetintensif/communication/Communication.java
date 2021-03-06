package fr.ensicaen.projetintensif.communication;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.sql.Timestamp;

import javax.net.ssl.HttpsURLConnection;

public class Communication {

    public enum RequestType{
        LOGIN,
        REGISTER,
        SEARCH_USER,
        OBSTACLE,
        GET_EVENT,
        GET_ALL_EVENTS,
        GET_ALL_OBSTACLES,
        ASK_ASSIST,
        REFRESH_ASSIST
    }

    private final String _serverURL = "https://roule-ma-poule.herokuapp.com/";
    //private final String _serverURL = "https://secure-lake-76948.herokuapp.com/";
    private final String _urlLogin = "authentication/login";
    private final String _urlRegister = "authentication/register";
    private final  String _urlGetProfile = "authentication/profile/";
    private final String _urlCreateObstacle = "obstacle/create";
    private final String _urlCreateEvent = "event/create";
    private final String _urlGetEvents = "event/list/";
    private final String _urlGetObstacles = "obstacle/list/";
    private final String _urlAskAssist = "assist/create/";
    private final String _urlRefreshHelp = "assist/list/";

    private RequestType _currentRequestType;
    private static String _token;
    private static JSONObject _getRes;
    private static JSONArray _JSONEvents;
    private static JSONArray _JSONObstacles;
    private static JSONArray _JSONHelpNeeded;
    private boolean registerSucceded = false;

    private String[] infoLogin;
    private String[] infoObstacle;

    private Object[] infoRegister;

    private Object[] infoGetEvent;

    private double[] infoEvents;



    // Constructeur pour register
    public Communication(String login, String pw, String pwConfirm, String name, String surname, String phoneNumber, long birthDate){
        infoRegister = new Object[]{login, pw, pwConfirm, name, surname,phoneNumber,birthDate};
        _currentRequestType = RequestType.REGISTER;
    }

    private String infoSearchUser;

    // Constructeur pour login
    public Communication(String login, String pw)
    {
        infoLogin = new String[]{login, pw};
        _currentRequestType = RequestType.LOGIN;
    }

    public Communication(String description, String type, String longitude, String latitude){
        infoObstacle = new String[]{description, type, longitude, latitude};
        _currentRequestType = RequestType.OBSTACLE;
    }

    //Constructeur pour create event
    public Communication(String name, String longitude, String latitude, long timeStamp, String description){
        infoGetEvent = new Object[]{name,longitude,latitude,timeStamp,description};
        _currentRequestType = RequestType.GET_EVENT;
    }

    public Communication(double latitude, double longitude, RequestType type) {
        infoEvents = new double[]{latitude ,longitude};
        _currentRequestType = type;
    }

    public boolean getRegisterSucceded() {
        return registerSucceded;
    }


    public Communication(String userSearch)
    {
        infoSearchUser = userSearch;
        _currentRequestType = RequestType.SEARCH_USER;
    }

    public void communicate(){
        try {
            switch (_currentRequestType)
            {
                case LOGIN:
                    communicate(infoLogin[0], infoLogin[1]);
                    break;
                case REGISTER:
                    communicate((String)infoRegister[0],(String)infoRegister[1],(String)infoRegister[2],(String)infoRegister[3],(String)infoRegister[4],(String)infoRegister[5],(long)infoRegister[6]);
                    break;
                case SEARCH_USER:
                    communicate(infoSearchUser);
                    break;
                case OBSTACLE:
                    communicate((String)infoObstacle[0], (String)infoObstacle[1], (String)infoObstacle[2], (String)infoObstacle[3]);
                    break;
                case GET_EVENT:
                    communicate((String)infoGetEvent[0], (String)infoGetEvent[1], (String)infoGetEvent[2], (long)infoGetEvent[3], (String)infoGetEvent[4]);
                    break;
                case GET_ALL_EVENTS:
                    communicate(infoEvents[0], infoEvents[1], _urlGetEvents);
                    break;
                case GET_ALL_OBSTACLES:
                    communicate(infoEvents[0], infoEvents[1], _urlGetObstacles);
                    break;
                case ASK_ASSIST:
                    communicate(infoEvents[0], infoEvents[1], _urlAskAssist);
                    break;
                case REFRESH_ASSIST:
                    communicate(infoEvents[0], infoEvents[1], _urlRefreshHelp);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void communicate(String login, String pwd)
    {
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("login", login);
            jsonObj.put("password",pwd);

            _token = sendPost(jsonObj, _urlLogin).split("\"")[3];

            Log.d("Login : ", _token);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void communicate(String profileName){
        try {
            _getRes = sendGet(_urlGetProfile+_token+"/"+profileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void communicate(double latitude, double longitude, String url) {
        try {
            if (url.equals(_urlGetEvents)){
                _JSONEvents = sendGetArray( url+_token+"/"+latitude+"/"+longitude);
            }
            else if (url.equals(_urlGetObstacles)){
                _JSONObstacles = sendGetArray( url+_token+"/"+latitude+"/"+longitude);
            }
            else if (url.equals(_urlAskAssist)){
                JSONObject jsonObj = new JSONObject();

                try {
                    jsonObj.put("token", _token);
                    jsonObj.put("utilisateur_id_2","4");
                    jsonObj.put("assistance_longitude",longitude);
                    jsonObj.put("assistance_latitude",latitude);
                    jsonObj.put("utilisateur_id", "4");

                    String res = sendPost(jsonObj, _urlAskAssist).toString();

                    Log.d("res",res);

                    if (res.equals("OK")){
                        registerSucceded = true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (url.equals(_urlRefreshHelp)){
                _JSONHelpNeeded = sendGetArray( url+_token+"/"+latitude+"/"+longitude);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Login : ", _token);

    }


    private void communicate(String login, String pw, String pwConfirm, String name, String surname, String phoneNumber, long birthDate){
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("login", login);
            jsonObj.put("password",pw);
            jsonObj.put("password_confirmation",pwConfirm);
            jsonObj.put("user_name",name);
            jsonObj.put("user_surname",surname);
            jsonObj.put("user_phone",phoneNumber);
            jsonObj.put("user_birthdate",birthDate);

            String res = sendPost(jsonObj, _urlRegister).toString();

            Log.d("res",res);

            if (res.equals("OK")){
                registerSucceded = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void communicate(String description, String type, String longitude, String latitude) {
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("token", _token);
            jsonObj.put("description",description);
            jsonObj.put("type",type);
            jsonObj.put("longitude",longitude);
            jsonObj.put("latitude",latitude);
            jsonObj.put("utilisateur_id", "4");

            String res = sendPost(jsonObj, _urlCreateObstacle).toString();

            Log.d("res",res);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void communicate(String name, String longitude, String latitude, long timeStamp, String description){
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("token", _token);
            jsonObj.put("event_name",name);
            jsonObj.put("event_longitude",longitude);
            jsonObj.put("event_latitude",latitude);
            jsonObj.put("event_timestamp",timeStamp);
            jsonObj.put("event_description",description);


            String res = sendPost(jsonObj, _urlCreateEvent);

            Log.d("res",res);

            if (res.equals("OK")){
                registerSucceded = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getToken()
    {
        return _token;
    }

    public JSONObject getGetResult(){
        return _getRes;
    }

    private String sendPost(JSONObject jsonObject, String urlPost)throws Exception{
        URL obj = new URL(_serverURL+urlPost);

        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        // Request header
        con.setRequestMethod("POST");
        con.setRequestProperty( "Content-type", "application/json");
        con.setRequestProperty( "Accept", "*/*" );
        con.setDoOutput(true);

        // Send post request
        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
        wr.write(jsonObject.toString());
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();


        Log.d("POST : url : ",  _serverURL + urlPost);
        Log.d("POST : jsonObject : ", jsonObject.toString());
        Log.d("POST : Response Code : ", responseCode + "");


        if (responseCode != 200)
        {
            return null;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Log.d("POST : Response ", response.toString());

        return response.toString();
    }

    private JSONObject sendGet(String urlGet) throws Exception {
        URL obj = new URL(_serverURL+urlGet);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty( "Accept", "*/*" );

        int responseCode = con.getResponseCode();


        Log.d("GET : url ",  _serverURL + urlGet);
        Log.d("GET : Response Code ", responseCode + "");


        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Log.d("GET : Response ", response.toString());

        JSONObject res = new JSONObject(response.toString());

        return res;
    }

    private JSONArray sendGetArray(String urlGet) throws Exception {
        URL obj = new URL(_serverURL+urlGet);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty( "Accept", "*/*" );

        int responseCode = con.getResponseCode();


        Log.d("GET : url ",  _serverURL + urlGet);
        Log.d("GET : Response Code ", responseCode + "");


        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Log.d("GET : Response ", response.toString());

        JSONArray res = new JSONArray(response.toString());

        return res;
    }


    public boolean doesSomeoneNeedHelp(){
        if (_JSONHelpNeeded != null){
            return _JSONHelpNeeded.length() != 0;
        }
        return false;
    }

    public static JSONArray get_JSONEvents() {
        return _JSONEvents;
    }

    public static JSONArray get_JSONObstacles() {
        return _JSONObstacles;
    }


}