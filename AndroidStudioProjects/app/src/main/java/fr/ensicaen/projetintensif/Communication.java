package fr.ensicaen.projetintensif;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Communication {

    public enum RequestType{
        LOGIN,
        REGISTER,
        SEARCH_USER
    }

    private final String _serverURL = "https://roule-ma-poule.herokuapp.com/";
    //private final String _serverURL = "https://secure-lake-76948.herokuapp.com/";
    private final String _urlLogin = "authentication/login";
    private final String _urlRegister = "authentication/register";
    private final  String _urlGetProfile = "authentication/profile/";

    private RequestType _currentRequestType;
    private static String _token;
    private JSONObject _getRes;

    private String[] infoLogin;
    private String infoSearchUser;

    public Communication(String login, String pw)
    {
        infoLogin = new String[]{login, pw};
        _currentRequestType = RequestType.LOGIN;
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
                    break;
                case SEARCH_USER:
                    communicate(infoSearchUser);
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

            _token = sendPost(jsonObj, _urlLogin).getString("token");
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

    public String getToken()
    {
        return _token;
    }

    public JSONObject getGetResult(){
        return _getRes;
    }

    private JSONObject sendPost(JSONObject jsonObject, String urlPost)throws Exception{
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

        Log.d("POST : url ",  _serverURL + _urlLogin);
        Log.d("POST : jsonObject ", jsonObject.toString());
        Log.d("POST : Response Code ", responseCode + "");

        if (responseCode == 401 || responseCode == 404)
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

        JSONObject res = new JSONObject(response.toString());

        return res;
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


}