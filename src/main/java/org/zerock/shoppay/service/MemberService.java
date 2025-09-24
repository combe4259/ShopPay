package org.zerock.shoppay.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.shoppay.Entity.Member;
import org.zerock.shoppay.dto.MemberSignupDto;
import org.zerock.shoppay.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    // 회원가입
    @Transactional
    public Member signup(MemberSignupDto signupDto) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(signupDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        
        // 비밀번호 확인
        if (!signupDto.isPasswordMatching()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        
        // 회원 엔티티 생성
        Member member = Member.builder()
            .email(signupDto.getEmail())
            .password(passwordEncoder.encode(signupDto.getPassword()))
            .name(signupDto.getName())
            .phone(signupDto.getPhone())
            .address(signupDto.getAddress())
            .detailAddress(signupDto.getDetailAddress())
            .zipCode(signupDto.getZipCode())
            .role(Member.MemberRole.USER)
            .isActive(true)
            .build();
        
        return memberRepository.save(member);
    }
    
    // 이메일로 회원 조회
    public Member findByEmail(String email) {
        return memberRepository.findByEmailAndIsActive(email, true)
            .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }
    
    // 이메일 중복 체크
    public boolean checkEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }
    
    // 회원 정보 수정
    @Transactional
    public Member updateMember(Long memberId, String name, String phone, 
                              String address, String detailAddress, String zipCode) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        member.setName(name);
        member.setPhone(phone);
        member.setAddress(address);
        member.setDetailAddress(detailAddress);
        member.setZipCode(zipCode);
        
        return member;
    }
    
    // 비밀번호 변경
    @Transactional
    public void changePassword(Long memberId, String currentPassword, String newPassword) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        member.setPassword(passwordEncoder.encode(newPassword));
    }
    
    // 회원 탈퇴 (소프트 삭제)
    @Transactional
    public void deactivateMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        member.setIsActive(false);
    }
}