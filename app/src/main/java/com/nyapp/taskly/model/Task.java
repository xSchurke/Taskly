package com.nyapp.taskly.model;

import com.google.firebase.firestore.Exclude;


public class Task {

    private String Id;
    private String Name;
    private String Description;
    private String Date;
    private String Assignment;
    private String Status;



    public Task(String Id, String Name, String Description, String Date, String Assignment, String Status) {
        this.Id = Id;
        this.Name = Name;
        this.Description = Description;
        this.Date = Date;
        this.Assignment = Assignment;
        this.Status = Status;
;
    }
    public Task(){}


    @Exclude
    public String getId() {return Id;}

    public String getName() {return Name;}

    public String getDescription() {
        return Description;
    }

    public String getDate() {
        return Date;
    }

    public String getAssignment() {
        return Assignment;
    }

    public String getStatus(){
        return Status;
    }

    public void setId(String id) {this.Id = id;}

}
