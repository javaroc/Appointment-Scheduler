package proxy;

import com.google.gson.*;
import model.AppointmentInfo;
import model.AppointmentRequest;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class ServerProxy {
    private static final String apiToken = "6a1831dd-97df-4349-837c-b1443d4b914d";
    private static final String apiPath = "http://scheduling-interview-2021-265534043.us-west-2.elb.amazonaws.com/api/Scheduling";

    private static ServerProxy instance;

    private final HttpClient httpClient;

    private final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()).toLocalDateTime();
        }
    }).create();
    private ServerProxy() {
        httpClient = HttpClient.newHttpClient();
    }

    public static ServerProxy getInstance() {
        if (instance == null) {
            instance = new ServerProxy();
        }
        return instance;
    }

    public AppointmentRequest getNextAppointmentRequest() {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiPath + "/AppointmentRequest?token=" + apiToken))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        try {
            System.out.println("Requesting next appointmentRequest...");
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("Requested next appointmentRequest. Status code: " + httpResponse.statusCode());

            if (httpResponse.statusCode() == 200) {
                //TODO: Gson can't deserialize AppointmentRequest because it contains a LocalDateTime.
                // Maybe need a custom deserializer?
                AppointmentRequest appointmentRequest = gson.fromJson(httpResponse.body(), AppointmentRequest.class);
                System.out.println("Received request from server:");
                System.out.println(appointmentRequest);
                return appointmentRequest;
            } else if (httpResponse.statusCode() == 204) {
                System.out.println("No more appointments to schedule.");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public AppointmentInfo[] getInitialSchedule() {
        //TODO: Implement getInitialSchedule
        return null;
    }

    public boolean scheduleAppointment(AppointmentInfo appointmentInfo) {
        return false;
    }

    public void startTest() {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiPath + "/Start?token=" + apiToken))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            System.out.println("Sending /start request...");
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("Started test. Status code: " + httpResponse.statusCode());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopTest() {
        //TODO: Implement stop test
    }

    public static void main(String[] args) {
        ServerProxy serverProxy = ServerProxy.getInstance();
//        serverProxy.startTest();
        serverProxy.getNextAppointmentRequest();
    }
}
