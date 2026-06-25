package com.siblo.rent.scheduler;

import com.siblo.rent.entity.Court.CourtStatus;
import com.siblo.rent.entity.Court;
import com.siblo.rent.repository.CourtRepository;
import com.siblo.rent.service.CourtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
public class SlotMaintenanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(SlotMaintenanceScheduler.class);

    private final CourtRepository courtRepository;
    private final CourtService courtService;

    @Value("${app.scheduling.slot-horizon-days:14}")
    private int slotHorizonDays;

    public SlotMaintenanceScheduler(CourtRepository courtRepository, CourtService courtService) {
        this.courtRepository = courtRepository;
        this.courtService = courtService;
    }

    @Scheduled(cron = "${app.scheduling.slot-extend-cron:0 0 1 * * *}")
    @Transactional
    public void extendSlotHorizon() {
        List<Court> activeCourts = courtRepository.findByStatus(CourtStatus.ACTIVE);
        for (Court court : activeCourts) {
            courtService.generateSlots(court.getId(), slotHorizonDays);
        }
        log.info("Extended slot horizon for {} active court(s)", activeCourts.size());
    }
}
