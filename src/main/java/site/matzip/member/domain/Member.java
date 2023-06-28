package site.matzip.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.matzip.comment.domain.Comment;
import site.matzip.matzip.domain.MatzipRecommendation;
import site.matzip.review.domain.Review;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String nickname;
    private String email;
    @Enumerated(EnumType.STRING)
    private MemberRole role;
    //TODO:이 부분도 oAuth만 이용시 필요없음. 삭제예정
    private String password;
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<MatzipRecommendation> matzipRecommendations = new ArrayList<>();
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Review> reviews = new ArrayList<>();
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Comment> comments = new ArrayList<>();


    //    @Builder
//    public Member(String username, String nickname, String email) {
//        this.username = username;
//        this.nickname = nickname;
//        this.email = email;
//        role = MemberRole.ROLE_MEMBER;
//    }
    @Builder
    public Member(String username, String nickname, String password, String email) {
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        role = MemberRole.ROLE_MEMBER;
    }

    public String getEmail() {
        if (email == null) {
            return "";
        }
        return email;
    }

    public void updateEmail(String email) {
        this.email = email;
    }
}
