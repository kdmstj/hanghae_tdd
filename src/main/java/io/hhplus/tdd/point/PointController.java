package io.hhplus.tdd.point;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;

    @GetMapping("/{userId}")
    public UserPoint point(
            @PathVariable long userId
    ) {
        return pointService.get(userId);
    }

    @GetMapping("/{userId}/histories")
    public List<PointHistory> history(
            @PathVariable long userId
    ) {
        return pointService.getHistory(userId);
    }

    @PatchMapping("{userId}/charge")
    public UserPoint charge(
            @PathVariable long userId,
            @RequestBody @Valid PointRequest request
    ) {
        return pointService.charge(userId, request.amount());
    }


    @PatchMapping("/{userId}/use")
    public UserPoint use(
            @PathVariable long userId,
            @RequestBody @Valid PointRequest request
    ) {
        return pointService.use(userId, request.amount());
    }
}
