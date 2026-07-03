# Member 7 - Postman Final Verification

Collection: `07_Member_Promotion_Payment_Chatbot`

Mục đích: lưu evidence retest cuối của Member 7 và đồng bộ trạng thái các Jira ticket đã hoàn tất.

## Promotion Template Verification

Completed test cases:
- PT01: Tạo promotion template và lưu promotionTemplateId.
- PT03: Cập nhật promotion template.
- PT04: Xóa promotion template.
- PT05: Lấy danh sách promotion template.
- PT06: Xem chi tiết promotion template.

Result: Passed and finalized.

## Promotion Verification

Completed test cases:
- PRO03: Tính discount với tổng đơn hàng hợp lệ.
- PRO04: Kiểm tra không đạt min value.
- PRO05: Admin tạo promotion và lưu promotionId.
- PRO08: Cập nhật promotion.
- PRO09: Disable promotion.
- PRO10: Xem chi tiết promotion.

Result: Passed and finalized.

Excluded from this close-out:
- PRO07: pending backend/EC2 validation verification. 

## Payment Verification

Completed test cases:
- PAY04: User lấy lịch sử thanh toán.
- PAY05: VNPay callback thiếu hoặc sai chữ ký/parameter.
- PAY06: VNPay return redirect flow.

Result: Passed and finalized.

Excluded from this close-out:
- PAY01: pending VNPAY PENDING-order verification. 

## Chatbot Admin Verification

Completed test cases:
- BOTADM01: Xem trạng thái chatbot.
- BOTADM02: Bật chatbot.
- BOTADM03: Tắt chatbot.
- BOTADM04: Toggle trạng thái chatbot.
- BOTADM05: Cập nhật chatbot configuration.

Result: Passed and finalized.

Excluded from this close-out:
- BOT01 and BOT03: pending chatbot response-time verification.