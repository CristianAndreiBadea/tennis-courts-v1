package com.tenniscourts.guests;

import com.tenniscourts.reservations.Reservation;
import com.tenniscourts.reservations.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    Guest findByName(String name);
}
