package com.siblo.rent.service;

import com.siblo.rent.entity.Court;
import com.siblo.rent.entity.Court.CourtStatus;
import com.siblo.rent.entity.TimeSlot;
import com.siblo.rent.entity.TimeSlot.SlotStatus;
import com.siblo.rent.repository.CourtRepository;
import com.siblo.rent.repository.TimeSlotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotGenerationTest {

    @Mock CourtRepository courtRepository;
    @Mock TimeSlotRepository timeSlotRepository;
    @Mock BookingExpiryService bookingExpiryService;
    @InjectMocks CourtService courtService;

    private Court courtWithHours(LocalTime open, LocalTime close) {
        Court c = new Court();
        c.setId(1L);
        c.setOpenTime(open);
        c.setCloseTime(close);
        c.setStatus(CourtStatus.ACTIVE);
        return c;
    }

    @Test
    void generateSlots_standardHours_createsCorrectSlots() {
        Court court = courtWithHours(LocalTime.of(6, 0), LocalTime.of(22, 0));
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(timeSlotRepository.existsByCourtIdAndDateAndStartTime(anyLong(), any(), any()))
            .thenReturn(false);

        courtService.generateSlots(1L, 1);

        ArgumentCaptor<TimeSlot> captor = ArgumentCaptor.forClass(TimeSlot.class);
        verify(timeSlotRepository, atLeastOnce()).save(captor.capture());
        List<TimeSlot> saved = captor.getAllValues();
        assertEquals(16, saved.size());
        assertEquals(LocalTime.of(6, 0), saved.get(0).getStartTime());
        assertEquals(LocalTime.of(7, 0), saved.get(0).getEndTime());
        assertEquals(LocalTime.of(21, 0), saved.get(15).getStartTime());
        assertEquals(LocalTime.of(22, 0), saved.get(15).getEndTime());
        saved.forEach(s -> assertEquals(SlotStatus.AVAILABLE, s.getStatus()));
    }

    @Test
    void generateSlots_nonWholeHourOpen_roundsUp() {
        Court court = courtWithHours(LocalTime.of(6, 30), LocalTime.of(22, 0));
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(timeSlotRepository.existsByCourtIdAndDateAndStartTime(anyLong(), any(), any()))
            .thenReturn(false);

        courtService.generateSlots(1L, 1);

        ArgumentCaptor<TimeSlot> captor = ArgumentCaptor.forClass(TimeSlot.class);
        verify(timeSlotRepository, atLeastOnce()).save(captor.capture());
        List<TimeSlot> saved = captor.getAllValues();
        assertEquals(15, saved.size());
        assertEquals(LocalTime.of(7, 0), saved.get(0).getStartTime());
        assertEquals(LocalTime.of(21, 0), saved.get(14).getStartTime());
    }

    @Test
    void generateSlots_nonWholeHourClose_endsBeforeClose() {
        Court court = courtWithHours(LocalTime.of(6, 0), LocalTime.of(21, 30));
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(timeSlotRepository.existsByCourtIdAndDateAndStartTime(anyLong(), any(), any()))
            .thenReturn(false);

        courtService.generateSlots(1L, 1);

        ArgumentCaptor<TimeSlot> captor = ArgumentCaptor.forClass(TimeSlot.class);
        verify(timeSlotRepository, atLeastOnce()).save(captor.capture());
        List<TimeSlot> saved = captor.getAllValues();
        assertEquals(15, saved.size());
        assertEquals(LocalTime.of(6, 0), saved.get(0).getStartTime());
        assertEquals(LocalTime.of(20, 0), saved.get(14).getStartTime());
    }

    @Test
    void generateSlots_existingSlots_skipsDuplicates() {
        Court court = courtWithHours(LocalTime.of(6, 0), LocalTime.of(22, 0));
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(timeSlotRepository.existsByCourtIdAndDateAndStartTime(anyLong(), any(), any()))
            .thenReturn(true);

        courtService.generateSlots(1L, 1);

        verify(timeSlotRepository, never()).save(any());
    }
}
