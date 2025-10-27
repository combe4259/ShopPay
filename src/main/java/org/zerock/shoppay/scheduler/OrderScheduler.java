package org.zerock.shoppay.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zerock.shoppay.Entity.Order;
import org.zerock.shoppay.repository.OrderRepository;
import org.zerock.shoppay.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 만료된 PENDING 주문을 자동으로 취소하고 재고를 복구하는 스케줄러
 * - 프론트엔드 결제창 이탈 감지가 실패한 경우를 대비한 백업 메커니즘
 * - 15분 동안 결제되지 않은 주문을 자동으로 취소
 */
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    /**
     * 1분마다 만료된 PENDING 주문 확인 및 취소
     * - reservedUntil이 현재 시간보다 이전인 PENDING 주문을 찾음
     * - 각 주문에 대해 cancelOrderAndRestoreStock() 호출하여 재고 복구
     */
    @Scheduled(fixedRate = 60000) // 1분 = 60000ms
    public void cancelExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> expiredOrders = orderRepository.findExpiredOrders("PENDING", now);

        if (!expiredOrders.isEmpty()) {
            System.out.println("========== 만료된 주문 자동 취소 시작 ==========");
            System.out.println("현재 시간: " + now);
            System.out.println("만료된 주문 수: " + expiredOrders.size());

            for (Order order : expiredOrders) {
                try {
                    System.out.println("주문 취소 시도: " + order.getOrderId()
                        + " (만료 시간: " + order.getReservedUntil() + ")");
                    orderService.cancelOrderAndRestoreStock(order.getOrderId());
                } catch (Exception e) {
                    System.err.println("주문 취소 실패: " + order.getOrderId() + ", 에러: " + e.getMessage());
                }
            }

            System.out.println("========== 만료된 주문 자동 취소 완료 ==========");
        }
    }
}
