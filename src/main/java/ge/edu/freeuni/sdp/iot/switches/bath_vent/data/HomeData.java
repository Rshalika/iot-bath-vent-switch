package ge.edu.freeuni.sdp.iot.switches.bath_vent.data;

import ge.edu.freeuni.sdp.iot.switches.bath_vent.model.Home;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by khrak on 6/25/16.
 */
public class HomeData {


    private ConcurrentHashMap<String, Home> homeData;

    private static HomeData instance = new HomeData();

    private static HomeData testInstance;
    private static boolean testMode = false;

    public static HomeData getInstance()
    {
        if(testMode) {
            return testInstance;
        }

        if (instance == null) {
            synchronized (HomeData.class) {
                if (instance == null)
                    instance = new HomeData();
            }
        }
        return instance;
    }

    public static void setTestInstance(HomeData instance) {
        testInstance = instance;
    }

    public static void setTestMode(boolean value) {
        testMode = value;
    }

    private HomeData() {
        homeData = new ConcurrentHashMap<String, Home>();
        registerHomes();
    }

    public void addHome(Home home) {
        homeData.put(home.getHomeid(), home);
    }

    public Home getHome(String houseid) {
        return homeData.get(houseid);
    }

    private void registerHomes() {
        String respJson = getResponseString("https://iot-house-registry.herokuapp.com/houses/");

        if(respJson == null) return;

        keepHomes(respJson);
    }

    private String getResponseString(String url) {
        Client client = ClientBuilder.newClient();
        Response response =
                client.target(url)
                        .request(MediaType.APPLICATION_JSON)
                        .get();

        int responseStatus = response.getStatus();
        if (responseStatus != Response.Status.OK.getStatusCode())
            return null;

        return response.readEntity(String.class);
    }

    private void keepHomes(String respJson) {

        JSONArray jsonArray = new JSONArray(respJson);
        for (int i=0; i<jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            String homeid = jsonObject.getJSONObject("RowKey").getString("_");
            //String venturl = jsonObject.getJSONObject("vent_ip").getString("_");
            String venturl = "https://iot-sim-bath.herokuapp.com/";

            Home home = new Home(homeid, venturl);
            addHome(home);
        }
    }
}
