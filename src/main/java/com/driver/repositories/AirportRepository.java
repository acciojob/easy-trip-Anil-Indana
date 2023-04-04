package com.driver.repositories;

import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import io.swagger.models.auth.In;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AirportRepository {
    private HashMap<Integer, Passenger> passengerMap;
    private HashMap<Integer, Flight> flightMap;
    private HashMap<String, Airport> airportMap;
    private HashMap<Flight, List<Integer>> ticketsBookingMap;
    private HashMap<Integer,List<Flight>> passengerBookings;
    private HashMap<Integer,Integer> ticketPrices;
    private HashMap<Integer,Integer> flightRevenue;

    public AirportRepository() {
        passengerMap = new HashMap<>();
        flightMap = new HashMap<>();
        airportMap = new HashMap<>();
        ticketsBookingMap = new HashMap<>();
        ticketPrices = new HashMap<>();
        flightRevenue = new HashMap<>();
    }
    public void addAirport(Airport airport){
        String key = airport.getAirportName();
        airportMap.put(key,airport);
    }
    public String getLargestAirportName(){
        String ans = "";
        int maxTerminals = Integer.MIN_VALUE;
        for(Airport airport : airportMap.values()){
            if(airport.getNoOfTerminals() > maxTerminals){
                maxTerminals = airport.getNoOfTerminals();
                ans = airport.getAirportName();
            }
            else if(airport.getNoOfTerminals() == maxTerminals){
                if(airport.getAirportName().compareTo(ans) < 0) ans = airport.getAirportName();
            }
        }
        return ans;
    }
    public double getShortestDurationOfPossibleBetweenTwoCities(City fromCity,City toCity){
        double durationTime = Double.MAX_VALUE;
        for(Flight flight : flightMap.values()){
            if((flight.getFromCity() == fromCity && flight.getToCity().equals(toCity))){
                durationTime = Math.min(durationTime,flight.getDuration());
            }
        }
        return durationTime==Double.MAX_VALUE?-1:durationTime;
    }
    public String bookATicket(Integer flightId,Integer passengerId){
        if(flightMap.containsKey(flightId) && passengerMap.containsKey(passengerId)){
            if(flightMap.containsKey(flightId)) {
                Flight flight = flightMap.get(flightId);
                if(!ticketsBookingMap.containsKey(flight)) return null;
                int numberOfPassengers = ticketsBookingMap.get(flight).size();
                if (numberOfPassengers == flight.getMaxCapacity()) return "FAILURE";
                List<Integer> list;
                if(ticketsBookingMap.containsKey(flight)){
                    list = ticketsBookingMap.get(flight);
                }
                else list = new ArrayList<>();
                if(list.contains(passengerId)) return "FAILURE";
                list.add(passengerId);
                ticketsBookingMap.put(flight,list);
                flightPrice(flightId); // flight with its price
                revenue(flightId); // calculating total revenue
                List<Flight> flights;
                if(passengerBookings.containsKey(passengerId)) flights = passengerBookings.get(passengerId);
                else flights = new ArrayList<>();
                flights.add(flight);
                passengerBookings.put(passengerId,flights);
            }
            return "SUCCESS";
        }
        return null;
    }

    private void revenue(Integer flightId) {
        int price = 0;
        if(flightMap.containsKey(flightId) && ticketPrices.containsKey(flightId)){
            ticketPrices.get(flightId);
        }
        flightRevenue.put(flightId,flightRevenue.getOrDefault(price,0)+price);
    }

    public String cancelATicket(Integer flightId,Integer passengerId){
        // checking whether the passenger booked the ticket.
//        for(List<Integer> list : ticketsBookingMap.values()){
//            if(!list.contains(passengerId)) return "FAILURE";
//        }
        Flight f = flightMap.get(flightId);
        if(flightMap.containsKey(flightId) && ticketsBookingMap.containsKey(f)){
            for(Flight flight : ticketsBookingMap.keySet()){
                if(flight.getFlightId() == flightId){
                    List<Integer> list = ticketsBookingMap.get(flight);
                    if(list == null) return "FAILURE";
                    if(!list.contains(passengerId)) return "FAILURE";
                    else {
                        list.remove(passengerId);
                        return "SUCCESS";
                    }
                }
            }
        }
        return "FAILURE";
    }
    public int countOfBookingsDoneByPassengerAllCombined(Integer passengerId){
        if(passengerMap.containsKey(passengerId) && passengerBookings.containsKey(passengerId)) return passengerBookings.get(passengerId).size();
        return 0;
    }
    public String addFlight(Flight flight){
        int key = flight.getFlightId();
        flightMap.put(key,flight);
        flightPrice(flight.getFlightId());
        return "SUCCESS";
    }
    public String addPassenger(Passenger passenger){
        int key = passenger.getPassengerId();
        passengerMap.put(key,passenger);
        return "SUCCESS";
    }
    public int getNumberOfPeopleOn(Date date,String airportName){
        List<Integer> list = new ArrayList<>();
        if(flightMap.containsKey(airportName)){
            for(Flight flight : ticketsBookingMap.keySet()){
                if(flight.getFlightDate() == date){
                    list = ticketsBookingMap.get(flight);
                }
            }
        }
        int cnt = 0;
        for(Integer id : list){
            if(passengerMap.containsKey(id) && passengerBookings.containsKey(id)){
                List<Flight> flights = passengerBookings.get(id);
                    for(Flight flight : flights){
                        if(flight.getFromCity().equals(airportName)) cnt++;
                        if(flight.getToCity().equals(airportName) && flight.getDuration() <= 24) cnt++;
                }
            }
        }
        return cnt;
    }
    public int calculateFlightFare(Integer flightId){
        if(flightMap.containsKey(flightId) && ticketPrices.containsKey(flightId)) return  ticketPrices.get(flightId);
        return 0;
    }
    public String getAirportNameFromFlightId(Integer flightId){
        if(flightMap.containsKey(flightId)){
            City city = flightMap.get(flightId).getFromCity();
            for(Airport airport : airportMap.values()){
                if(airport.getCity().equals(city)) return airport.getAirportName();
            }
        }
        return null;
    }
    public int calculateRevenueOfAFlight(Integer flightId){
        if(flightMap.containsKey(flightId) && flightRevenue.containsKey(flightId)) return flightRevenue.get(flightId);
        return 0;
    }
    public void flightPrice(Integer flightId){
        int bookings = 0;
        if(flightMap.containsKey(flightId)) {
            Flight flight = flightMap.get(flightId);
            if (ticketsBookingMap.containsKey(flight)) {
                bookings = ticketsBookingMap.get(flight).size();
            }
            int price = 3000 + bookings * 50;
            ticketPrices.put(flightId, price);
        }
    }
}
