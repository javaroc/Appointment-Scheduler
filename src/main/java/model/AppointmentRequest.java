package model;

import com.google.gson.Gson;

import java.time.LocalDateTime;

public class AppointmentRequest {
    public int requestId;
    public int personId;
    public LocalDateTime[] preferredDays;
    public Doctor[] preferredDocs;
    public boolean isNew;

    public AppointmentRequest(int requestId, int personId, LocalDateTime[] preferredDays, Doctor[] preferredDocs, boolean isNew) {
        this.requestId = requestId;
        this.personId = personId;
        this.preferredDays = preferredDays;
        this.preferredDocs = preferredDocs;
        this.isNew = isNew;
    }


    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
