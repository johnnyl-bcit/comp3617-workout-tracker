package ca.bcit.comp3617final;

/**
 * Created by Johnny on 7/19/2017.
 */

class ExerciseDetail {
    private String eventId;
    private String eventName;
    private int set;
    private int rep;
    private int weight;
    private String colorId;
    private Boolean isComplete;

    public ExerciseDetail(String eventId,
                          String eventName,
                          int set,
                          int rep,
                          int weight,
                          String colorId,
                          Boolean isComplete) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.set = set;
        this.rep = rep;
        this.weight = weight;
        this.colorId = colorId;
        this.isComplete = isComplete;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public int getSet() {
        return set;
    }

    public int getRep() {
        return rep;
    }

    public int getWeight() {
        return weight;
    }

    public String getColor() {
        return colorId;
    }

    public Boolean getComplete() {
        return isComplete;
    }

    public void setComplete(Boolean complete) {
        isComplete = complete;
    }
}
