package site.matzip.matzip.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import site.matzip.matzip.domain.Matzip;
import site.matzip.matzip.domain.MatzipRecommendation;
import site.matzip.matzip.dto.MatzipCreationDTO;
import site.matzip.matzip.dto.MatzipListDTO;
import site.matzip.matzip.service.MatzipService;


import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/matzip")
@RequiredArgsConstructor
public class MatzipController {
    private final MatzipService matzipService;

    @PostMapping("/create")
    public String create(@RequestBody MatzipCreationDTO matzipCreationDTO, BindingResult result) {
        if (result.hasErrors()) {
            return "/matzip/create";
        }
        matzipService.create(matzipCreationDTO);
        return "redirect:/matzip/list";
    }

    @GetMapping("/list")
    public String list(Model model) {
        List<Matzip> matzipList = matzipService.findAll();
        List<MatzipListDTO> matzipDtoList = matzipList.stream()
                .map(matzip -> MatzipListDTO.builder()
                        .matzipName(matzip.getMatzipName())
                        .address(matzip.getAddress())
                        .phoneNumber(matzip.getPhoneNumber())
                        .matzipType(matzip.getMatzipType())
                        .build())
                .collect(Collectors.toList());

        model.addAttribute("matzipList", matzipDtoList);
        return "/matzip/list";
    }

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<MatzipListDTO>> search() {
        List<Matzip> matzipList = matzipService.findAllWithRecommendations();
        List<MatzipListDTO> matzipDtoList = matzipList.stream()
                .map(matzip -> {
                    List<MatzipRecommendation> recommendations = matzip.getRecommendations();
                    double rating = 0;
                    String description = "";
                    if (!recommendations.isEmpty()) {
                        MatzipRecommendation recommendation = recommendations.get(0); // 첫 번째 평가 정보 사용
                        rating = recommendation.getRating();
                        description = recommendation.getDescription();
                    }
                    return MatzipListDTO.builder()
                            .matzipName(matzip.getMatzipName())
                            .address(matzip.getAddress())
                            .phoneNumber(matzip.getPhoneNumber())
                            .matzipUrl(matzip.getMatzipUrl())
                            .matzipType(matzip.getMatzipType())
                            .x(matzip.getX())
                            .y(matzip.getY())
                            .rating(rating)
                            .description(description)
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(matzipDtoList);
    }
}
