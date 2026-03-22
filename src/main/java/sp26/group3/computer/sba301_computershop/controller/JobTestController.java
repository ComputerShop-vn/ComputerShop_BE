package sp26.group3.computer.sba301_computershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sp26.group3.computer.sba301_computershop.service.PaymentScheduleJobService;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobTestController {

    private final PaymentScheduleJobService jobService;

    @PostMapping("/trigger-overdue-job")
    public ResponseEntity<String> triggerJobManually() {
        // This method triggers Task 2 (Mark as Overdue) and Task 3 (Calculate Penalty)
        // For convenience, we'll run the full daily job sequence
        jobService.runDailyPaymentJobs();
        return ResponseEntity.ok("Đã kích hoạt Job quét quá hạn và tính phí phạt thành công bằng tay!");
    }
}
