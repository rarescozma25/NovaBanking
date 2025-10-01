package service;

import model.Moneda;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class MonedaService {
    private static final String BASE_URL = "https://open.er-api.com/v6/latest/"; //url-ul api-ului

    public static double curs(Moneda from, Moneda to) {
        try {
            URL url=new URL(BASE_URL + from.name()); //construiesc url-ul
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET"); //cerere de get

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            //deschide un flux de date la conexiunea  HTTP
            //transforma sirul de biti in caractere
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                response.append(line); //adaug in response continutul din reader
            reader.close();

            JSONObject json = new JSONObject(response.toString()); //creeaza json
            if (!json.getString("result").equals("success")) {
                throw new RuntimeException("Eroare API: " + json.getString("error-type"));
            }

            JSONObject rates = json.getJSONObject("rates"); //din rares ia valoarea to.name
            return rates.getDouble(to.name());
        } catch (Exception e) {
            System.err.println("Eroare curs valutar: " + e.getMessage());
            return -1;
        }
    }
}
