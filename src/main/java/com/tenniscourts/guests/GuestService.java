package com.tenniscourts.guests;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GuestService {

    @Autowired
    private GuestRepository guestRepository;

    private GuestMapper guestMapper;

    public GuestDTO createGuest(CreateGuestRequestDTO createGuestRequestDTO) {
        Guest guest = Guest.builder().name(createGuestRequestDTO.getName()).build();
        guest = guestRepository.save(guest);
        return guestMapper.map(guest);
    }

    public GuestDTO updateGuest(Long guestId, String name) {
        Guest guest = guestRepository.findById(guestId)
                .<EntityNotFoundException>orElseThrow(() -> {throw new EntityNotFoundException("Guest not found");});
        guest.setName(name);
        guestRepository.save(guest);
        return guestMapper.map(guest);
    }

    public GuestDTO deleteGuest(Long guestId){
        Guest guest = guestRepository.findById(guestId)
                .<EntityNotFoundException>orElseThrow(() -> {throw new EntityNotFoundException("Guest not found");});
        guestRepository.delete(guest);
        return guestMapper.map(guest);
    }

    public GuestDTO findById(Long guestId){
        Guest guest = guestRepository.findById(guestId)
                .<EntityNotFoundException>orElseThrow(() -> {throw new EntityNotFoundException("Guest not found");});
        return guestMapper.map(guest);
    }

    public GuestDTO findByName(String guestName){
        Guest guest = guestRepository.findByName(guestName);
        return guestMapper.map(guest);
    }

    public List<GuestDTO> listAllGuests(){
        List<Guest> listOfGuests = guestRepository.findAll();
        return guestMapper.map(listOfGuests);
    }

}

