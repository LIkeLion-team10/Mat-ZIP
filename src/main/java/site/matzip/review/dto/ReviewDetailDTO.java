package site.matzip.review.dto;

import lombok.Data;
import site.matzip.matzip.domain.Matzip;
import site.matzip.matzip.domain.MatzipType;
import site.matzip.review.domain.Review;

import java.time.LocalDateTime;

@Data
public class ReviewDetailDTO {
    private String profileImageUrl;
    private String authorNickname;
    private LocalDateTime createDate;
    private String content;
    //private Long views;
    private String matzipName;
    private MatzipType matzipType;
    private String address;
    private String phoneNumber;
    //private 리뷰이미지

    public ReviewDetailDTO(Review review, Matzip matzip) {
        this.profileImageUrl = review.getAuthor().getProfileImage().getImageUrl();
        this.authorNickname = review.getAuthor().getNickname();
        //this.views =
        this.matzipName = matzip.getMatzipName();
        this.address = matzip.getAddress();
        this.phoneNumber = matzip.getPhoneNumber();
        //this.리뷰이미지
        this.content = review.getContent();
    }
}