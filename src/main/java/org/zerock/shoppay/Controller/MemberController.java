package org.zerock.shoppay.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.shoppay.Entity.Member;
import org.zerock.shoppay.Entity.Order;
import org.zerock.shoppay.dto.MemberSignupDto;
import org.zerock.shoppay.service.MemberService;
import org.zerock.shoppay.service.OrderService;
import org.zerock.shoppay.repository.OrderRepository;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService memberService;
    private final OrderRepository orderRepository;
    
    // 회원가입 페이지
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupDto", new MemberSignupDto());
        return "member/signup";
    }
    
    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(@ModelAttribute MemberSignupDto signupDto, 
                        RedirectAttributes redirectAttributes) {
        try {
            Member member = memberService.signup(signupDto);
            redirectAttributes.addFlashAttribute("message", 
                "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("signupDto", signupDto);
            return "redirect:/signup";
        }
    }
    
    // 로그인 페이지
    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           Model model) {
        if (error != null) {
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (logout != null) {
            model.addAttribute("message", "로그아웃되었습니다.");
        }
        return "member/login";
    }
    
    // 이메일 중복 체크 (AJAX)
    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return !memberService.checkEmailDuplicate(email);
    }



    
    // 마이페이지
    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            Member member = memberService.findByEmail(userDetails.getUsername());
            model.addAttribute("member", member);
            
            // 주문 내역 조회
            List<Order> orders = orderRepository.findByMemberOrderByCreatedAtDesc(member);
            model.addAttribute("orders", orders);
            
            // 대시보드 데이터
            model.addAttribute("orderCount", orders.size());
            model.addAttribute("wishlistCount", 0);
            //model.addAttribute("couponCount", 1);
            //model.addAttribute("points", 0);
            model.addAttribute("recentOrders", orders.size() > 3 ? orders.subList(0, 3) : orders);
        }
        return "member/mypage";
    }
    
    // 주문 내역 상세 페이지
    @GetMapping("/mypage/orders")
    public String orderList(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            Member member = memberService.findByEmail(userDetails.getUsername());
            List<Order> orders = orderRepository.findByMemberOrderByCreatedAtDesc(member);
            model.addAttribute("member", member);
            model.addAttribute("orders", orders);
        }
        return "member/orders";
    }
    
    // 회원정보 수정 페이지
    @GetMapping("/mypage/edit")
    public String editForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            Member member = memberService.findByEmail(userDetails.getUsername());
            model.addAttribute("member", member);
        }
        return "member/edit";
    }
    
    // 회원정보 수정 처리
    @PostMapping("/mypage/edit")
    public String editMember(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam String name,
                            @RequestParam(required = false) String phone,
                            @RequestParam(required = false) String address,
                            @RequestParam(required = false) String detailAddress,
                            @RequestParam(required = false) String zipCode,
                            RedirectAttributes redirectAttributes) {
        try {
            if (userDetails != null) {
                Member member = memberService.findByEmail(userDetails.getUsername());
                memberService.updateMember(member.getId(), name, phone, address, detailAddress, zipCode);
                redirectAttributes.addFlashAttribute("message", "회원정보가 수정되었습니다.");
            }
            return "redirect:/mypage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "회원정보 수정에 실패했습니다.");
            return "redirect:/mypage/edit";
        }
    }
    
    // 비밀번호 변경 페이지
    @GetMapping("/mypage/password")
    public String passwordForm() {
        return "member/password";
    }
    
    // 비밀번호 변경 처리
    @PostMapping("/mypage/password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            if (userDetails != null) {
                Member member = memberService.findByEmail(userDetails.getUsername());
                memberService.changePassword(member.getId(), currentPassword, newPassword);
                redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
            }
            return "redirect:/mypage";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/mypage/password";
        }
    }
}