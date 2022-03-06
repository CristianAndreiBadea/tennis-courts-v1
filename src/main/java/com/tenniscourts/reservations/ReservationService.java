package com.tenniscourts.reservations;

import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.guests.Guest;
import com.tenniscourts.guests.GuestRepository;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleDTO;
import com.tenniscourts.schedules.ScheduleMapper;
import com.tenniscourts.schedules.ScheduleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class ReservationService {

    @Autowired
    private final ReservationRepository reservationRepository;
    @Autowired
    private final ScheduleRepository scheduleRepository;
    @Autowired
    private final GuestRepository guestRepository;

    private final ReservationMapper reservationMapper;
    private final ScheduleMapper scheduleMapper;


    public ReservationDTO bookReservation(CreateReservationRequestDTO createReservationRequestDTO) {

        Guest guest = guestRepository.findById(createReservationRequestDTO.getGuestId()).<EntityNotFoundException>orElseThrow(() -> {
            throw new EntityNotFoundException("Guest not found.");
        });

        Schedule schedule = scheduleRepository.findById(createReservationRequestDTO.getScheduleId()).<EntityNotFoundException>orElseThrow(() -> {
            throw new EntityNotFoundException("Schedule not found.");
        });

        Reservation reservation = Reservation.builder()
                .guest(guest)
                .schedule(schedule)
                .refundValue(BigDecimal.valueOf(10))
                .value(BigDecimal.valueOf(10))
                .reservationStatus(ReservationStatus.READY_TO_PLAY)
                .build();

        reservationRepository.save(reservation);
        return reservationMapper.map(reservation);
    }

    public ReservationDTO findReservation(Long reservationId) throws EntityNotFoundException {
        return reservationRepository.findById(reservationId).map(reservationMapper::map).<EntityNotFoundException>orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation not found.");
        });
    }

    public ReservationDTO cancelReservation(Long reservationId) {
        return reservationMapper.map(this.cancel(reservationId));
    }

    private Reservation cancel(Long reservationId) {
        return reservationRepository.findById(reservationId).map(reservation -> {

            this.validateCancellation(reservation);

            BigDecimal refundValue = getRefundValue(reservation);
            return this.updateReservation(reservation, refundValue, ReservationStatus.CANCELLED);

        }).<EntityNotFoundException>orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation not found.");
        });
    }

    private Reservation updateReservation(Reservation reservation, BigDecimal refundValue, ReservationStatus status) {
        reservation.setReservationStatus(status);
        reservation.setValue(reservation.getValue().subtract(refundValue));
        reservation.setRefundValue(refundValue);

        return reservationRepository.save(reservation);
    }

    private void validateCancellation(Reservation reservation) {
        if (!ReservationStatus.READY_TO_PLAY.equals(reservation.getReservationStatus())) {
            throw new IllegalArgumentException("Cannot cancel/reschedule because it's not in ready to play status.");
        }

        if (reservation.getSchedule().getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Can cancel/reschedule only future dates.");
        }
    }

    public BigDecimal getRefundValue(Reservation reservation) {

        LocalDateTime now = LocalDateTime.now();

        if(reservation.getSchedule().getStartDateTime().plusHours(12).isBefore(now)
                && reservation.getSchedule().getStartDateTime().plusHours(12).plusMinutes(59).isAfter(now)) {
            return reservation.getValue().multiply(new BigDecimal(0.75));
        } else if(reservation.getSchedule().getStartDateTime().plusHours(2).isBefore(now)
                && reservation.getSchedule().getStartDateTime().plusHours(11).plusMinutes(59).isAfter(now)) {
            return reservation.getValue().multiply(new BigDecimal(0.50));
        }else if(reservation.getSchedule().getStartDateTime().plusMinutes(1).isBefore(now)
                && reservation.getSchedule().getStartDateTime().plusHours(2).isAfter(now)) {
            return reservation.getValue().multiply(new BigDecimal(0.25));
        }

        return BigDecimal.ZERO;
    }

    public ReservationDTO rescheduleReservation(Long previousReservationId, Long scheduleId) {
        Reservation previousReservation = cancel(previousReservationId);

        if (scheduleId.equals(previousReservation.getSchedule().getId())
                && ReservationStatus.READY_TO_PLAY.name().equalsIgnoreCase(previousReservation.getReservationStatus().name())) {
            throw new IllegalArgumentException("Cannot reschedule to the same slot.");
        }

        previousReservation.setReservationStatus(ReservationStatus.RESCHEDULED);
        reservationRepository.save(previousReservation);

        ReservationDTO newReservation = bookReservation(CreateReservationRequestDTO.builder()
                .guestId(previousReservation.getGuest().getId())
                .scheduleId(scheduleId)
                .build());
        newReservation.setPreviousReservation(reservationMapper.map(previousReservation));
        return newReservation;
    }
}
