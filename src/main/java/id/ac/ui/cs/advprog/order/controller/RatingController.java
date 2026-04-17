package id.ac.ui.cs.advprog.order.controller;

import id.ac.ui.cs.advprog.order.dto.RatingCreateRequest;
import id.ac.ui.cs.advprog.order.service.RatingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/order")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/{id}/rating")
    public String createRating(
            @PathVariable("id") Long orderId,
            @ModelAttribute RatingCreateRequest request,
            @RequestParam(value = "viewer", defaultValue = "titiper") String viewer
    ) {
        ratingService.createRating(orderId, request);
        return "redirect:/order/list?viewer=" + viewer + "&success=Rating+berhasil+disimpan";
    }
}

