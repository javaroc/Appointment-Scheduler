import model.AppointmentInfo;
import model.AppointmentRequest;
import model.Doctor;
import proxy.ServerProxy;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

public class ScheduleManager {
    Map<Doctor, Map<LocalDateTime, AppointmentInfo>> doctorSchedules = new HashMap<>();
    /** Associates each patient via id with a list of their scheduled appointments in chronological order.
     * <p>
     * An array was chosen in place of a set to allow for binary searches to find the closest scheduled appointments
     * to a given timeslot. A set would allow for more efficient insertion, but timeslot validation happens at least
     * once, and sometimes more, for each appointment scheduled.*/
    Map<Integer, ArrayList<AppointmentInfo>> patientSchedules = new HashMap<>();

    /** New patients may only schedule appointments for 3pm or 4pm */
    private static final Set<Integer> ALLOWED_FIRST_APPOINTMENT_TIMES = Set.of(12 + 3, 12 + 4);

    private final ServerProxy serverProxy;


    public ScheduleManager() {
        // Create empty schedule for each doctor
        for (Doctor doctor: Doctor.values()) {
            doctorSchedules.put(doctor, new HashMap<>());
        }
        // Load schedules from proxy
        serverProxy = ServerProxy.getInstance();
        AppointmentInfo[] scheduledAppointments = serverProxy.getInitialSchedule();
        for (AppointmentInfo appointmentInfo : scheduledAppointments) {
            Doctor doctorId = appointmentInfo.getDoctorId();
            int patientId = appointmentInfo.getPersonId();
            LocalDateTime timeslot = appointmentInfo.getAppointmentTime();

            doctorSchedules.get(doctorId).put(timeslot, appointmentInfo);
            insertIntoSortedArraylist(patientSchedules.get(patientId), appointmentInfo);
        }
        // Initialize test environment
        serverProxy.startTest();
    }

    public void processAppointmentQueue() {
        AppointmentRequest request = serverProxy.getNextAppointmentRequest();
        while (request != null) {
            boolean success = scheduleAppointment(request);
            if (!success) {
                System.err.println("Failed to schedule appointment:");
                System.err.println(request);
            }
            request = serverProxy.getNextAppointmentRequest();
        }
    }

    /**
     * Attempts to schedule an appointment based on the given AppointmentRequest
     * @param request AppointmentRequest object containing the info for the appointment.
     * @return true if successful, false otherwise
     */
    private boolean scheduleAppointment(AppointmentRequest request) {
        for (LocalDateTime timeslot : request.preferredDays) {
            if (!isValidTimeSlot(timeslot, request.personId, request.isNew)) {
                continue;
            }
            for (Doctor doctorId : request.preferredDocs) {
                if (!doctorSchedules.get(doctorId).containsKey(timeslot)) {
                    AppointmentInfo appointmentInfo = new AppointmentInfo(doctorId, request.personId, timeslot, request.isNew);
                    boolean success = serverProxy.scheduleAppointment(appointmentInfo);
                    if (success) {
                        doctorSchedules.get(doctorId).put(timeslot, appointmentInfo);
                        insertIntoSortedArraylist(patientSchedules.get(request.personId), appointmentInfo);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static <T extends Comparable<T>> void insertIntoSortedArraylist(ArrayList<T> arr, T val) {
        int i = Collections.binarySearch(arr, val);
        if (i > 0) {
            System.err.println("Patient already has appointment at that time");
        } else {
            int insertionPoint = -(i + 1);
            arr.add(insertionPoint, val);
        }
    }

    private boolean isValidTimeSlot(LocalDateTime appointmentTime, int patientId, boolean isNewPatientAppointment) {
        // If patient is new, check that timeslot is 3pm or 4pm
        if (isNewPatientAppointment && !ALLOWED_FIRST_APPOINTMENT_TIMES.contains(appointmentTime.getHour())) {
            return false;
        }

        // Check that timeslot is not too close to patient's previous or next appointment
        ArrayList<AppointmentInfo> patientAppointments = patientSchedules.get(patientId);
        int i = Collections.binarySearch(patientAppointments, new AppointmentInfo(appointmentTime));
        int indexOfNextAppointment = -(i + 1);
        // Check previous appointment if it exists
        if (indexOfNextAppointment > 0) {
            LocalDateTime previousAppointmentTime = patientAppointments.get(indexOfNextAppointment - 1).getAppointmentTime();
            // This appointment must be at least a week after the last appointment.
            // To calculate the earliest allowable day, midnight of the day of the last appointment is calculated, then 7 days are added.
            LocalDateTime earliestAllowedDay = previousAppointmentTime.minusHours(previousAppointmentTime.getHour()).plusDays(7);
            if (appointmentTime.isBefore(earliestAllowedDay)) {
                return false;
            }
        }

        // Check next appointment if it exists
        if (indexOfNextAppointment < patientAppointments.size()) {
            LocalDateTime nextAppointmentTime = patientAppointments.get(indexOfNextAppointment).getAppointmentTime();
            // This appointment must be at least a week before the next appointment.
            // To calculate the earliest disallowed day, midnight of the day of the next appointment is calculated, then 6 days are subtracted.
            LocalDateTime earliestDisallowedDay = nextAppointmentTime.minusHours(nextAppointmentTime.getHour()).minusDays(6);
            if (appointmentTime.isAfter(earliestDisallowedDay)) {
                return false;
            }
        }

        // If timeslot is in Nov or Dec 2021, check that appointment is a weekday
        if (appointmentTime.getYear() == 2021
                && (appointmentTime.getMonth() == Month.NOVEMBER || appointmentTime.getMonth() == Month.DECEMBER)) {
            if (appointmentTime.getDayOfWeek() == DayOfWeek.SATURDAY || appointmentTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
                return false;
            }
        }

        // Check that timeslot is between 8am and 4pm inclusive
        if (appointmentTime.getHour() < 8 || appointmentTime.getHour() > 12 + 4) {
            return false;
        }
        // Timeslot has passed all checks
        return true;
    }

    public static void main(String[] args) {
        ScheduleManager scheduleManager = new ScheduleManager();
        scheduleManager.processAppointmentQueue();
    }
}
