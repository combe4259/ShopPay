package org.zerock.shoppay.Controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.zerock.shoppay.service.OrderService;
import org.zerock.shoppay.service.CartService;
import org.zerock.shoppay.service.MemberService;
import org.zerock.shoppay.Entity.Member;
import org.zerock.shoppay.Entity.Order;
import org.zerock.shoppay.Entity.OrderItem;
import org.zerock.shoppay.Entity.Cart;
import org.zerock.shoppay.Entity.CartItem;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;
import java.util.ArrayList;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.time.LocalDateTime;

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
    @ResponseBody
    public ResponseEntity<JSONObject> confirmPayment(
            HttpServletRequest request,
            @RequestBody String jsonBody,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        JSONObject requestData = parseRequestData(jsonBody);
        String orderId = (String) requestData.get("orderId");
        String paymentKey = (String) requestData.get("paymentKey");
        Long amount = Long.parseLong(requestData.get("amount").toString());

        // 토스페이먼츠 결제 승인 API 호출
        JSONObject response = sendRequest(requestData, API_SECRET_KEY, "https://api.tosspayments.com/v1/payments/confirm");
        
        System.out.println("토스 응답: " + response.toJSONString());
        
        if (!response.containsKey("error")) {
            System.out.println("결제 승인 성공!");
            
            // 결제 성공 - 주문 데이터 저장
            if (userDetails != null) {
                Member member = memberService.findByEmail(userDetails.getUsername());
                Cart cart = cartService.getCartWithItems(member);
                
                List<Integer> selectedItemIds = null;
                if (requestData.containsKey("selectedCartItems") && requestData.get("selectedCartItems") != null) {
                    String selectedItemsJsonString = (String) requestData.get("selectedCartItems");
                    if (selectedItemsJsonString != null && !selectedItemsJsonString.isEmpty()) {
                        JSONParser parser = new JSONParser();
                        try {
                            org.json.simple.JSONArray selectedItemsArray = (org.json.simple.JSONArray) parser.parse(selectedItemsJsonString);
                            selectedItemIds = new ArrayList<>();
                            for (Object id : selectedItemsArray) {
                                selectedItemIds.add(((Long) id).intValue());
                            }
                        } catch (ParseException e) {
                            System.err.println("Error parsing selectedCartItems: " + e.getMessage());
                            selectedItemIds = new ArrayList<>(); // 오류 발생 시 안전하게 빈 리스트로 처리
                        }
                    }
                }
                
                // 주문 생성
                Order order = Order.builder()
                    .orderId(orderId)
                    .member(member)
                    .totalAmount(amount.intValue())
                    .status("PAID")
                    .paymentKey(paymentKey)
                    .paidAt(LocalDateTime.now())
                    .build();
                
                List<OrderItem> orderItems = new ArrayList<>();
                List<CartItem> itemsToRemove = new ArrayList<>();
                
                if (selectedItemIds != null && !selectedItemIds.isEmpty()) {
                    for (CartItem cartItem : cart.getCartItems()) {
                        if (selectedItemIds.contains(cartItem.getId().intValue())) {
                            OrderItem orderItem = OrderItem.builder()
                                .order(order)
                                .product(cartItem.getProduct())
                                .quantity(cartItem.getQuantity())
                                .price(cartItem.getProduct().getPrice())
                                .build();
                            orderItems.add(orderItem);
                            itemsToRemove.add(cartItem); // 제거할 아이템 목록에 추가
                        }
                    }
                } else {
                    System.out.println("Warning: Payment confirmed but no selectedCartItems found. Cart will not be cleared.");
                }
                order.setOrderItems(orderItems);
                
                // 주문 저장
                System.out.println("주문 저장 시작...");
                Order savedOrder = orderService.saveOrder(order);
                System.out.println("주문 저장 완료! orderId: " + savedOrder.getOrderId());

                // 선택된 아이템만 장바구니에서 제거
                for (CartItem item : itemsToRemove) {
                    cartService.removeFromCart(item.getId());
                }
                
                response.put("message", "주문이 성공적으로 처리되었습니다.");
            } else {
                response.put("warning", "비로그인 상태에서 결제되었습니다. 주문 정보가 저장되지 않았습니다.");
            }
        } else {
            System.out.println("ERROR: 결제 승인 실패!");
        }
        
        int statusCode = response.containsKey("error") ? 400 : 200;
        return ResponseEntity.status(statusCode).body(response);
    }



    @GetMapping("/payment/checkout")
    public String checkout(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) Integer amount,
            @RequestParam(required = false) String orderName,
            Model model) {
        model.addAttribute("clientKey", CLIENT_KEY);
        model.addAttribute("orderId", orderId != null ? orderId : "ORDER_" + System.currentTimeMillis());
        model.addAttribute("amount", amount != null ? amount : 50000);
        model.addAttribute("orderName", orderName != null ? orderName : "IKEA 상품");
        return "payment/checkout";
    }
    








    private JSONObject parseRequestData(String jsonBody) {
        try {
            return (JSONObject) new JSONParser().parse(jsonBody);
        } catch (ParseException e) {
            //logger.error("JSON Parsing Error", e);
            return new JSONObject();
        }
    }


    private JSONObject sendRequest(JSONObject requestData, String secretKey, String urlString) throws IOException {
        HttpURLConnection connection = createConnection(secretKey, urlString);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream responseStream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
            return (JSONObject) new JSONParser().parse(reader);
        } catch (Exception e) {
            //logger.error("Error reading response", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Error reading response");
            return errorResponse;
        }
    }

    private HttpURLConnection createConnection(String secretKey, String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        return connection;
    }
    


}
