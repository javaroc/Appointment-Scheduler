package model;

import java.time.LocalDateTime;

public class AppointmentInfo implements Comparable<AppointmentInfo> {
    private Doctor doctorId;
    private int personId;
    private LocalDateTime appointmentTime;
    private boolean isNewPatientAppointment;

    public AppointmentInfo(Doctor doctorId, int personId, LocalDateTime appointmentTime, boolean isNewPatientAppointment) {
        this.doctorId = doctorId;
        this.personId = personId;
        this.appointmentTime = appointmentTime;
        this.isNewPatientAppointment = isNewPatientAppointment;
    }

    public AppointmentInfo(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    @Override
    public int compareTo(AppointmentInfo other) {
        return this.appointmentTime.compareTo(other.appointmentTime);
    }

    public Doctor getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Doctor doctorId) {
        this.doctorId = doctorId;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public boolean isNewPatientAppointment() {
        return isNewPatientAppointment;
    }

    public void setNewPatientAppointment(boolean newPatientAppointment) {
        isNewPatientAppointment = newPatientAppointment;
    }
}
