package com.backend.allreva.member.ui;

import com.backend.allreva.auth.application.AuthMember;
import com.backend.allreva.common.dto.Response;
import com.backend.allreva.member.command.application.request.MemberInfoRequest;
import com.backend.allreva.member.command.application.request.RefundAccountRequest;
import com.backend.allreva.member.command.domain.Member;
import com.backend.allreva.member.query.application.response.MemberDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "회원 API", description = "회원 정보를 관리하는 API")
public interface MemberControllerSwagger {

    @Operation(summary = "회원 정보 조회", description = "회원 정보를 조회합니다.")
    Response<MemberDetailResponse> getMemberDetail(
            @AuthMember Member member
    );

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 중복을 확인합니다.")
    Response<Boolean> isDuplicatedNickname(
            final String nickname
    );

    @Operation(summary = "oauth2 회원가입", description = "oauth2 회원가입 시 USER 권한으로 승격됩니다.")
    @RequestBody(
            content = @Content(
                encoding = @Encoding(
                        name = "memberInfoRequest", contentType = MediaType.APPLICATION_JSON_VALUE)))
    Response<Void> registerMember(
            @AuthMember Member member,
            @RequestPart MemberInfoRequest memberInfoRequest,
            @RequestPart(value = "image", required = false) MultipartFile image
    );

    @Operation(summary = "회원 프로필 수정", description = "회원 프로필 수정 API, USER 권한일 때만 사용 가능합니다.")
    @RequestBody(
            content = @Content(
                    encoding = @Encoding(
                            name = "memberInfoRequest", contentType = MediaType.APPLICATION_JSON_VALUE)))
    Response<Void> updateMemberInfo(
            @AuthMember Member member,
            @RequestPart MemberInfoRequest memberInfoRequest,
            @RequestPart(value = "image", required = false) MultipartFile image
    );

    @Operation(summary = "회원 환불 계좌 등록", description = "회원 환불 계좌 등록 API")
    Response<Void> registerRefundAccount(
            @AuthMember Member member,
            @RequestBody RefundAccountRequest refundAccountRequest
    );

    @Operation(summary = "회원 환불 계좌 삭제", description = "회원 환불 계좌 삭제 API")
    Response<Void> deleteRefundAccount(
            @AuthMember Member member
    );
}
