package org.zerock.shoppay.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.zerock.shoppay.Entity.Member;
import org.zerock.shoppay.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmailAndIsActive(email, true)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        return User.builder()
            .username(member.getEmail())
            .password(member.getPassword())
            .authorities(member.getRole().getValue())
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!member.getIsActive())
            .build();
    }
}