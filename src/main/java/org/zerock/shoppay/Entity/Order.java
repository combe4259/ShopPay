package org.zerock.shoppay.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    @Id
    @Column(length = 100)
    private String orderId;  // 토스페이먼츠 orderId와 동일하게 사용
    
    @Column(length = 100)
    private String paymentKey;  // 토스페이먼츠 결제 키 (결제 완료 후 저장)
    
    @Column(nullable = false)
    private Integer totalAmount;
    
    @Column(nullable = false, length = 50)
    private String status;  // PENDING, PAID, CANCELLED, FAILED
    
    @Column(length = 100)
    private String customerName;
    
    @Column(length = 100)
    private String customerEmail;
    
    @Column(length = 20)
    private String customerPhone;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime paidAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
}