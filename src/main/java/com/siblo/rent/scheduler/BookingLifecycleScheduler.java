package com.siblo.rent.scheduler;

import com.siblo.rent.service.BookingExpiryService;
import com.siblo.rent.service.BookingLifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingLifecycleScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingLifecycleScheduler.class);

    private final BookingExpiryService bookingExpiryService;
    private final BookingLifecycleService bookingLifecycleService;

    public BookingLifecycleScheduler(BookingExpiryService bookingExpiryService,
                                     BookingLifecycleService bookingLifecycleService) {
        this.bookingExpiryService = bookingExpiryService;
        this.bookingLifecycleService = bookingLifecycleService;
    }

    @Scheduled(fixedRateString = "${app.scheduling.payment-expiry-fixed-rate-ms:60000}")
    public void processExpiredPayments() {
        int count = bookingExpiryService.expirePendingPayments();
        if (count > 0) {
            log.info("Expired {} pending payment(s)", count);
        }
    }

    @Scheduled(fixedRateString = "${app.scheduling.lifecycle-fixed-rate-ms:300000}")
    public void updateBookingLifecycle() {
        int count = bookingLifecycleService.advanceLifecycle();
        if (count > 0) {
            log.info("Advanced lifecycle for {} booking(s)", count);
        }
    }
}
