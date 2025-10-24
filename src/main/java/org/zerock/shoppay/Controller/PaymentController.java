package org.zerock.shoppay.Controller;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.zerock.shoppay.Entity.Member;
import org.zerock.shoppay.Entity.Order;
import org.zerock.shoppay.dto.PaymentConfirmRequestDto;
import org.zerock.shoppay.service.CartService;
import org.zerock.shoppay.service.MemberService;
import org.zerock.shoppay.service.OrderService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    @Value("${toss.secret.key}")
    private String API_SECRET_KEY;

    @Value("${toss.client.key}")
    private String CLIENT_KEY;

    private final OrderService orderService;
    private final CartService cartService;
    private final MemberService memberService;

    @PostMapping("/confirm/payment")
    public ResponseEntity<JSONObject> confirmPayment(
            @RequestBody PaymentConfirmRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        // 1. DB에서 주문 정보를 미리 조회합니다.
        Order order = orderService.findById(requestDto.getOrderId())
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다: " + requestDto.getOrderId()));

        // 2. DB에 저장된 금액과 요청된 결제 금액이 일치하는지 확인합니다. (금액 위변조 방지)
        if (!order.getTotalAmount().equals(requestDto.getAmount().intValue())) {
            throw new RuntimeException("주문 금액이 일치하지 않습니다.");
        }

        // 3. 토스페이먼츠 결제 승인 API를 호출합니다.
        JSONObject requestData = new JSONObject();
        requestData.put("orderId", requestDto.getOrderId());
        requestData.put("amount", requestDto.getAmount());
        requestData.put("paymentKey", requestDto.getPaymentKey());

        JSONObject response = sendRequest(requestData, API_SECRET_KEY, "https://api.tosspayments.com/v1/payments/confirm");

        // 4. 토스페이먼츠의 응답에 따라 후속 처리를 합니다.
        if (response.get("status").equals("DONE")) {
            // 5. 결제 성공: OrderService를 통해 주문 상태를 'PAID'로 변경하고 재고를 차감합니다.
            Order confirmedOrder = orderService.confirmPayment(requestDto.getOrderId(), requestDto.getPaymentKey(), requestDto.getAmount());

            // 6. 결제된 상품들을 장바구니에서 제거합니다.
            if (userDetails != null) {
                Member member = memberService.findByEmail(userDetails.getUsername());
                List<Long> purchasedProductIds = confirmedOrder.getOrderItems().stream()
                        .map(orderItem -> orderItem.getProduct().getId())
                        .collect(Collectors.toList());
                cartService.removeCartItemsByProductIds(member, purchasedProductIds);
            }

            return ResponseEntity.ok(response);
        } else {
            // 7. 결제 실패: 토스페이먼츠가 돌려준 에러 메시지를 그대로 클라이언트에게 전달합니다.
            return ResponseEntity.status(400).body(response);
        }
    }

    @GetMapping("/payment/checkout")
    public String checkoutPage(Model model) {
        model.addAttribute("clientKey", CLIENT_KEY);
        return "payment/checkout";
    }

    private JSONObject sendRequest(JSONObject requestData, String secretKey, String urlString) throws IOException, ParseException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        try (InputStream responseStream = (responseCode == 200) ? connection.getInputStream() : connection.getErrorStream();
             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
            return (JSONObject) new JSONParser().parse(reader);
        }
    }
}
