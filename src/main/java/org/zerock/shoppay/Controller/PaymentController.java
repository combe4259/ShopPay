package org.zerock.shoppay.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.zerock.shoppay.service.OrderService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
public class PaymentController {

    @Value("${toss.secret.key}")
    private String API_SECRET_KEY;
    
    @Value("${toss.client.key}")
    private String CLIENT_KEY;
    
    @Autowired
    private OrderService orderService;
    @RequestMapping(value = "/confirm/payment")
    @ResponseBody
    public ResponseEntity<JSONObject> confirmPayment(HttpServletRequest request, @RequestBody String jsonBody) throws Exception {
        String secretKey = request.getRequestURL().toString().contains("/confirm/payment") ? API_SECRET_KEY : null;
        JSONObject response = sendRequest(parseRequestData(jsonBody), secretKey, "https://api.tosspayments.com/v1/payments/confirm");
        int statusCode = response.containsKey("error") ? 400 : 200;
        return ResponseEntity.status(statusCode).body(response);
    }
    @GetMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("clientKey", CLIENT_KEY);
        return "payment/checkout";
    }
    
    // 상품 결제 확인 (주문 처리 포함)
    @RequestMapping(value = "/confirm/order")
    @ResponseBody
    public ResponseEntity<JSONObject> confirmOrderPayment(@RequestBody String jsonBody) throws Exception {
        JSONObject requestData = parseRequestData(jsonBody);
        String orderId = (String) requestData.get("orderId");
        String paymentKey = (String) requestData.get("paymentKey");
        
        // 토스페이먼츠 결제 승인 요청
        JSONObject response = sendRequest(requestData, API_SECRET_KEY, "https://api.tosspayments.com/v1/payments/confirm");
        
        // 결제 성공 시 주문 상태 업데이트
        if (!response.containsKey("error")) {
            try {
                orderService.confirmPayment(orderId, paymentKey, response);
                response.put("orderProcessed", true);
            } catch (Exception e) {
                response.put("orderProcessed", false);
                response.put("orderError", e.getMessage());
            }
        }
        
        int statusCode = response.containsKey("error") ? 400 : 200;
        return ResponseEntity.status(statusCode).body(response);
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
