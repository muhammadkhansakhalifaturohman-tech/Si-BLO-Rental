package com.siblo.rent.dto;

public class AdminStatsDTO {
    private Long todaysRevenue; private String revenueChange; private Long activeBookings;
    private Integer capacityPercent; private Long openSlots; private String nextAvailableTime;

    public AdminStatsDTO() {}

    public AdminStatsDTO(Long todaysRevenue, String revenueChange, Long activeBookings,
                         Integer capacityPercent, Long openSlots, String nextAvailableTime) {
        this.todaysRevenue = todaysRevenue; this.revenueChange = revenueChange;
        this.activeBookings = activeBookings; this.capacityPercent = capacityPercent;
        this.openSlots = openSlots; this.nextAvailableTime = nextAvailableTime;
    }

    public Long getTodaysRevenue() { return todaysRevenue; }
    public void setTodaysRevenue(Long todaysRevenue) { this.todaysRevenue = todaysRevenue; }
    public String getRevenueChange() { return revenueChange; }
    public void setRevenueChange(String revenueChange) { this.revenueChange = revenueChange; }
    public Long getActiveBookings() { return activeBookings; }
    public void setActiveBookings(Long activeBookings) { this.activeBookings = activeBookings; }
    public Integer getCapacityPercent() { return capacityPercent; }
    public void setCapacityPercent(Integer capacityPercent) { this.capacityPercent = capacityPercent; }
    public Long getOpenSlots() { return openSlots; }
    public void setOpenSlots(Long openSlots) { this.openSlots = openSlots; }
    public String getNextAvailableTime() { return nextAvailableTime; }
    public void setNextAvailableTime(String nextAvailableTime) { this.nextAvailableTime = nextAvailableTime; }
}
