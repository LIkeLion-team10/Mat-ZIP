package site.matzip.review;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReviewController {
    @GetMapping("/review/create")
    public String create() {
        return "review/create";
    }

}
