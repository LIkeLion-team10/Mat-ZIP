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
import site.matzip.base.exception.UnauthorizedException;
import site.matzip.base.rq.Rq;
import site.matzip.base.rsData.RsData;
import site.matzip.config.auth.PrincipalDetails;
import site.matzip.friend.service.FriendService;
import site.matzip.image.service.ReviewImageService;
import site.matzip.matzip.domain.Matzip;
import site.matzip.matzip.dto.MatzipCreationDTO;
import site.matzip.matzip.dto.MatzipListDTO;
import site.matzip.matzip.dto.MatzipReviewDTO;
import site.matzip.matzip.dto.MatzipUpdateDTO;
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
    private final FriendService friendService;
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
        return "redirect:/main";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/createWithReview")
    public String createWithReview(@ModelAttribute MatzipReviewDTO matzipReviewDTO,
                                   BindingResult result,
                                   @AuthenticationPrincipal PrincipalDetails principalDetails) throws IOException {
        MatzipCreationDTO matzipCreationDTO = matzipReviewDTO.getMatzipCreationDTO();
        ReviewCreationDTO reviewCreationDTO = matzipReviewDTO.getReviewCreationDTO();
        Long authorId = principalDetails.getMember().getId();
        Member author = principalDetails.getMember();

        Matzip createdMatzip = matzipService.create(matzipCreationDTO, authorId);
        Review createdReview = reviewService.create(reviewCreationDTO, authorId, createdMatzip);
        reviewImageService.create(reviewCreationDTO.getImageFiles(), createdReview);

        return rq.redirectWithMsg("/main", "맛집과 리뷰가 등록되었습니다.");
    }

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<MatzipListDTO>> searchAll(Authentication authentication) throws UnauthorizedException {
        if (authentication == null) {
            throw new UnauthorizedException("Please login");
        }
        List<MatzipListDTO> matzipDtoList = matzipService.findAndConvertAll(rq.getMember(authentication).getId());
        return ResponseEntity.ok(matzipDtoList);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/mylist")
    @ResponseBody
    public ResponseEntity<List<MatzipListDTO>> searchMine(Authentication authentication) throws UnauthorizedException {

        if (authentication == null) {
            throw new UnauthorizedException("Please login");
        }
        List<MatzipListDTO> matzipDtoList = matzipService.findAndConvertById(rq.getMember(authentication).getId());
        return ResponseEntity.ok(matzipDtoList);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/list/{id}")
    @ResponseBody
    public ResponseEntity<List<MatzipListDTO>> searchFriendsMap(@PathVariable Long id,
                                                                @AuthenticationPrincipal PrincipalDetails principalDetails) throws UnauthorizedException {
        // 인증되지 않은 사용자는 UnauthorizedException 발생
        if (principalDetails == null) {
            throw new UnauthorizedException("Please Login");
        }
        // 둘이 친구가 아닌 경우, BadRequest(잘못된 요청) 응답 반환
        if (!friendService.isFriend(id, principalDetails.getUserId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        List<MatzipListDTO> matzipDtoList = matzipService.findAndConvertById(id);
        return ResponseEntity.ok(matzipDtoList);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<RsData> delete(@PathVariable Long id, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        RsData deleteRs = matzipService.delete(id, principalDetails.getMember().getId());
        if (deleteRs.isFail()) {
            return new ResponseEntity<>(deleteRs, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(deleteRs, HttpStatus.OK);
    }

    @PostMapping("/api/update/{id}")
    public ResponseEntity<RsData> update(@PathVariable Long id, @AuthenticationPrincipal PrincipalDetails principalDetails,
                                         @RequestBody MatzipUpdateDTO matzipUpdateDTO) {
        RsData updateRs = matzipService.modify(id, principalDetails.getMember().getId(), matzipUpdateDTO);
        if (updateRs.isFail()) {
            return new ResponseEntity<>(updateRs, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updateRs, HttpStatus.OK);
    }
}