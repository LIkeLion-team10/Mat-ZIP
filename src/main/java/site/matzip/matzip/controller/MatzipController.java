package site.matzip.matzip.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import site.matzip.base.rq.Rq;
import site.matzip.base.rsData.RsData;
import site.matzip.config.auth.PrincipalDetails;
import site.matzip.image.service.ReviewImageService;
import site.matzip.matzip.domain.Matzip;
import site.matzip.matzip.dto.MatzipCreationDTO;
import site.matzip.matzip.dto.MatzipListDTO;
import site.matzip.matzip.dto.MatzipReviewDTO;
import site.matzip.matzip.service.MatzipService;
import site.matzip.member.domain.Member;
import site.matzip.review.domain.Review;
import site.matzip.review.dto.ReviewCreationDTO;
import site.matzip.review.service.ReviewService;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/matzip")
@RequiredArgsConstructor
public class MatzipController {
    private final MatzipService matzipService;
    private final ReviewService reviewService;
    private final ReviewImageService reviewImageService;
    private final Rq rq;

    @GetMapping("create")
    public String create() {
        return "/matzip/create";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String create(@RequestBody @Valid MatzipCreationDTO matzipCreationDTO,
                         BindingResult result,
                         Authentication authentication) {
        if (result.hasErrors()) {
            return "/matzip/create";
        }
        Member author = rq.getMember(authentication);

        matzipService.create(matzipCreationDTO, author.getId());
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/createWithReview")
    public String createWithReview(@ModelAttribute @Valid MatzipReviewDTO matzipReviewDTO,
                                   BindingResult result,
                                   @AuthenticationPrincipal PrincipalDetails principalDetails) throws IOException {

        if (result.hasErrors()) {
            return rq.historyBack("리뷰 등록을 위한 올바른 값을 입력해주세요.");
        }

        MatzipCreationDTO matzipCreationDTO = matzipReviewDTO.getMatzipCreationDTO();
        ReviewCreationDTO reviewCreationDTO = matzipReviewDTO.getReviewCreationDTO();
        Long authorId = principalDetails.getMember().getId();
        Member author = principalDetails.getMember();

        Matzip createdMatzip = matzipService.create(matzipCreationDTO, authorId);
        Review createdReview = reviewService.create(reviewCreationDTO, authorId, createdMatzip);
        reviewImageService.create(reviewCreationDTO.getImageFiles(), createdReview);

        return rq.redirectWithMsg("/", "맛집과 리뷰가 등록되었습니다.");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<MatzipListDTO>> searchAll(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            List<MatzipListDTO> matzipDtoList = matzipService.findAndConvertAll(rq.getMember(authentication).getId());
            return ResponseEntity.ok(matzipDtoList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/mylist")
    @ResponseBody
    public ResponseEntity<List<MatzipListDTO>> searchMine(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            List<MatzipListDTO> matzipDtoList = matzipService.findAndConvertMine(rq.getMember(authentication).getId());
            return ResponseEntity.ok(matzipDtoList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/delete/{id}")
    @ResponseBody
    public RsData delete(@PathVariable Long id, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        RsData deleteRs = matzipService.delete(id, principalDetails.getMember().getId());
        return deleteRs;
    }
}