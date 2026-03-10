package com.campusroomreservation.project1;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.campusroomreservation.project1.data.AmenityRepository;
import com.campusroomreservation.project1.data.BuildingRepository;
import com.campusroomreservation.project1.data.RoomRepository;
import com.campusroomreservation.project1.data.UserRepository;
import com.campusroomreservation.project1.model.Amenity;
import com.campusroomreservation.project1.model.Building;
import com.campusroomreservation.project1.model.Room;
import com.campusroomreservation.project1.model.User;


@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadCsvData(BuildingRepository buildingRepo, RoomRepository roomRepo, AmenityRepository amenityRepo, UserRepository userRepo) {
        return args -> {
            
            // Load default user for testing, matches credentials in SecurityConfig
            if (userRepo.count() == 0) {
                User user = new User();
                user.setUsername("user");
                user.setPassword("password"); 
                userRepo.save(user);
            }
            
            // Check if data already exists to avoid duplicate loading
            if (buildingRepo.count() > 0 || roomRepo.count() > 0 || amenityRepo.count() > 0) {
                System.out.println("Data already exists, skipping CSV loading.");
                return;
            }

            Map<String, Building> buildingNameMap = new HashMap<>();
            Map<String, Amenity> amenityNameMap = new HashMap<>();

            // Load buildings from buildings.csv
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new ClassPathResource("data/buildings.csv").getInputStream(), StandardCharsets.UTF_8))) {
                String header = br.readLine(); // Skip header
                String line;
                while ((line = br.readLine()) != null) {
                    String name = line.trim();
                    if (name.isBlank()) continue;

                    Building building = new Building();
                    building.setName(name);

                    buildingRepo.save(building);
                    buildingNameMap.put(name, building);
                }
            }


            // Load amenities from amenities.csv
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new ClassPathResource("data/amenities.csv").getInputStream(), StandardCharsets.UTF_8))) {
                String header = br.readLine(); // Skip header
                String line;
                while ((line = br.readLine()) != null) {
                    String name = line.trim();
                    if (name.isBlank()) continue;

                    Amenity amenity = new Amenity();
                    amenity.setName(name.toUpperCase()); // Store amenities in uppercase for consistency

                    amenityRepo.save(amenity);
                    amenityNameMap.put(name, amenity);
                }
            }

            // Load rooms from rooms.csv
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new ClassPathResource("data/rooms.csv").getInputStream(), StandardCharsets.UTF_8))) {
                String header = br.readLine(); // Skip header
                String line;
                
                while((line = br.readLine()) != null) {
                    if (line.isBlank()) continue;

                    String[] parts = line.split(",", -1);
                    if (parts.length < 3) continue; // Skip malformed lines

                    String buildingName = parts[0].trim();
                    String roomNumber = parts[1].trim();
                    int capacity = Integer.parseInt(parts[2].trim());
                

                    Building building = buildingNameMap.get(buildingName);
                    if (building == null) {
                        System.err.println("Building not found for room: " + buildingName);
                        continue;
                    }

                    Set<Amenity> amenities = new HashSet<>();
                    if (parts.length >= 4 && !parts[3].isBlank()){
                        String[] amenityNames = parts[3].split("\\|");
                        for (String amenityNameRaw : amenityNames) {
                            String amenityName = amenityNameRaw.trim().toUpperCase();
                            Amenity amenity = amenityNameMap.get(amenityName);
                            if (amenity != null) {
                                amenities.add(amenity);
                            }
                        }
                    }

                    Room room = new Room();
                    room.setBuilding(building);
                    room.setRoomNumber(roomNumber);
                    room.setCapacity(capacity);
                    room.setAmenities(amenities);

                    roomRepo.save(room);
                }

                
                
            }

        };


    }
    
}
