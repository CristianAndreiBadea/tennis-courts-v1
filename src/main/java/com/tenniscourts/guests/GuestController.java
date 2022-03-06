package com.tenniscourts.guests;

import com.tenniscourts.config.BaseRestController;
import com.tenniscourts.reservations.CreateReservationRequestDTO;
import com.tenniscourts.reservations.ReservationDTO;
import com.tenniscourts.reservations.ReservationService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@Api(value = "GuestController")
public class GuestController extends BaseRestController {

    @Autowired
    private final GuestService guestService;


    @PostMapping("/createGuest")
    public ResponseEntity<GuestDTO> createGuest(CreateGuestRequestDTO createGuestRequestDTO) {
        return ResponseEntity.created(locationByEntity(guestService.createGuest(createGuestRequestDTO).getId())).build();
    }

    @PutMapping("/updateGuest")
    public ResponseEntity<GuestDTO> updateGuest(Long guestId, String name) {
        return ResponseEntity.ok(guestService.updateGuest(guestId, name));
    }

    @DeleteMapping("/deleteGuest")
    public ResponseEntity<GuestDTO> deleteGuest(Long guestId) {
        return ResponseEntity.ok(guestService.deleteGuest(guestId));
    }

    @GetMapping("/findGuestById")
    public ResponseEntity<GuestDTO> findById(Long guestId) {
        return ResponseEntity.ok(guestService.findById(guestId));
    }

    @GetMapping("/findGuestByName")
    public ResponseEntity<GuestDTO> findGuestByName(String name) {
        return ResponseEntity.ok(guestService.findByName(name));
    }

    @GetMapping("/listAllGuests")
    public ResponseEntity<List<GuestDTO>> listAllGuests() {
        return ResponseEntity.ok(guestService.listAllGuests());
    }
}
