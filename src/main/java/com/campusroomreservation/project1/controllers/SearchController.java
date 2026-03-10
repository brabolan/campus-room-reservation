package com.campusroomreservation.project1.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.campusroomreservation.project1.data.BuildingRepository;
import com.campusroomreservation.project1.model.Room;
import com.campusroomreservation.project1.services.RoomSearchService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import java.time.format.DateTimeParseException;


@Controller
@RequestMapping("/search")
public class SearchController {
    
    private final BuildingRepository buildingRepository;
    private final RoomSearchService roomSearchService;

    public SearchController(BuildingRepository buildingRepository, RoomSearchService roomSearchService) {
        this.buildingRepository = buildingRepository;
        this.roomSearchService = roomSearchService;
    }

    // Show the search page
    @GetMapping
    public String showSearch(Model model){
        model.addAttribute("buildings", buildingRepository.findAll());
        model.addAttribute("searchCriteria", new RoomSearchCriteria());
        return "search";
    }

    // Handle search form submission
    @PostMapping
    public String handleSearch(@Valid @ModelAttribute("searchCriteria") RoomSearchCriteria searchCriteria, BindingResult bindingResult, Model model) {

        model.addAttribute("buildings", toList(buildingRepository.findAll()));
        
        if (bindingResult.hasErrors()) {
            return "search";

        }

        LocalDateTime startTime;
        LocalDateTime endTime;
        try {
            startTime = LocalDateTime.parse(searchCriteria.getStartTime());
            endTime = LocalDateTime.parse(searchCriteria.getEndTime());
        } catch (DateTimeParseException e) {
            model.addAttribute("timeError", "Invalid date/time format.");
            return "search";
        }


        List<Room> results;
        try {
            results = roomSearchService.searchAvailableRooms(
                    searchCriteria.getBuildingId(),
                    searchCriteria.getMinCapacity(),
                    searchCriteria.isHasProjector(),
                    searchCriteria.isHasWhiteboard(),
                    startTime,
                    endTime);
        } catch (IllegalArgumentException e) {
            model.addAttribute("searchError", e.getMessage());
            return "search";
        }

        model.addAttribute("results", results);
        model.addAttribute("start", startTime);
        model.addAttribute("end", endTime);
        return "results";
    }

    // Helper method to convert Iterable to List
    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }
    // RoomSearchCriteria class to hold search parameters
    public static class RoomSearchCriteria {

        @NotNull
        private Long buildingId;

        @Min(1)
        private int minCapacity;

        private boolean hasProjector;
        private boolean hasWhiteboard;

        @NotNull
        private String startTime; 

        @NotNull
        private String endTime;

        // Getters and Setters
        public Long getBuildingId() {
            return buildingId;
        }
        public void setBuildingId(Long buildingId) {
            this.buildingId = buildingId;
        }

        public int getMinCapacity() {
            return minCapacity;
        }
        public void setMinCapacity(int minCapacity) {
            this.minCapacity = minCapacity;
        }

        public boolean isHasProjector() {
            return hasProjector;
        }
        public void setHasProjector(boolean hasProjector) {
            this.hasProjector = hasProjector;
        }

        public boolean isHasWhiteboard() {
            return hasWhiteboard;
        }
        public void setHasWhiteboard(boolean hasWhiteboard) {
            this.hasWhiteboard = hasWhiteboard;
        }

        public String getStartTime() {
            return startTime;
        }
        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }
        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }

}
