package com.pedometer.steptracker.runwalk.dailytrack.model;

public class StepData {
    private int steps;
    private int goal;
    private double calories;
    private double distance;
    private long time;
    private String date;

    public StepData() {
        this.steps = 0;
        this.goal = 6000;
        this.calories = 0;
        this.distance = 0;
        this.time = 0;
    }

    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }

    public int getGoal() { return goal; }
    public void setGoal(int goal) { this.goal = goal; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public long getTime() { return time; }
    public void setTime(long time) { this.time = time; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
