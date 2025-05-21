package com.smartcashpro.model;

import java.math.BigDecimal;
import java.time.LocalDateTime; 
import java.sql.Timestamp; 


public class Shift {
    private int shiftId;
    private LocalDateTime startTime;
    private LocalDateTime endTime; 
    private String status; 
    private int startUserId;
    private Integer endUserId; 
    private BigDecimal startingFloat;
    

    
    public Shift(int shiftId, LocalDateTime startTime, LocalDateTime endTime, String status, int startUserId, Integer endUserId, BigDecimal startingFloat) {
        this.shiftId = shiftId;
        this.startTime = startTime;
        this.endTime = endTime; 
        this.status = status;
        this.startUserId = startUserId;
        this.endUserId = endUserId; 
        this.startingFloat = startingFloat;
    }

     
     public Shift(int startUserId, BigDecimal startingFloat) {
         
         this(0, LocalDateTime.now(), null, "Open", startUserId, null, startingFloat);
     }

    
    public int getShiftId() { return shiftId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public int getStartUserId() { return startUserId; }
    public Integer getEndUserId() { return endUserId; }
    public BigDecimal getStartingFloat() { return startingFloat; }

     
     public void setShiftId(int shiftId) { this.shiftId = shiftId; } 
     public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
     public void setStatus(String status) { this.status = status; }
     public void setEndUserId(Integer endUserId) { this.endUserId = endUserId; }
     
}