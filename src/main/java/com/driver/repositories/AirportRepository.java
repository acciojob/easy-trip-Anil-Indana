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
            if((flight.getFromCity() == fromCity && flight.getToCity() == toCity) && durationTime < flight.getDuration()){
                durationTime = flight.getDuration();
            }
        }
        return durationTime==Double.MAX_VALUE?-1:durationTime;
    }
    public String bookATicket(Integer flightId,Integer passengerId){
        if(flightMap.containsKey(flightId)) {
            Flight flight = flightMap.get(flightId);
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

    private void revenue(Integer flightId) {
        int price = ticketPrices.get(flightId);
        flightRevenue.put(flightId,flightRevenue.getOrDefault(price,0)+price);
    }

    public String cancelATicket(Integer flightId,Integer passengerId){
        // checking whether the passenger booked the ticket.
//        for(List<Integer> list : ticketsBookingMap.values()){
//            if(!list.contains(passengerId)) return "FAILURE";
//        }
        for(Flight flight : ticketsBookingMap.keySet()){
            if(flight.getFlightId() == flightId){
                List<Integer> list = ticketsBookingMap.get(flight);
                for(Integer passenger : list){
                    if(passenger == passengerId) {
                        list.remove(passengerId);
                        return "SUCCESS";
                    }
                }
            }
        }
        return "FAILURE";
    }
    public int countOfBookingsDoneByPassengerAllCombined(Integer passengerId){
        if(passengerBookings.containsKey(passengerId)) return passengerBookings.get(passengerId).size();
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
        for(Flight flight : ticketsBookingMap.keySet()){
            if(flight.getFlightDate() == date){
                list = ticketsBookingMap.get(flight);
            }
        }
        int cnt = 0;
        for(Integer id : list){
            if(passengerBookings.containsKey(id)){
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
        return  ticketPrices.get(flightId);
    }
    public String getAirportNameFromFlightId(Integer flightId){
        for(Flight flight : flightMap.values()){
            if(flight.getFlightId() == flightId){
                return flight.getFromCity().name();
            }
        }
        return null;
    }
    public int calculateRevenueOfAFlight(Integer flightId){
        return flightRevenue.get(flightId);
    }
    public void flightPrice(Integer flightId){
        int bookings = 0;
        Flight flight = flightMap.get(flightId);
        if(ticketsBookingMap.containsKey(flight)){
            bookings = ticketsBookingMap.get(flight).size();
        }
        int price = 3000 + bookings*50;
        ticketPrices.put(flightId,price);
    }
}
