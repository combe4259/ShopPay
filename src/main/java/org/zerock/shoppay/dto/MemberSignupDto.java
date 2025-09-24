package org.zerock.shoppay.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSignupDto {
    
    private String email;
    private String password;
    private String passwordConfirm;
    private String name;
    private String phone;
    private String address;
    private String detailAddress;
    private String zipCode;
    
    // 유효성 검증
    public boolean isPasswordMatching() {
        return password != null && password.equals(passwordConfirm);
    }
}