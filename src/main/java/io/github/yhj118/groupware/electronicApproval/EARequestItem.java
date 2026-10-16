package io.github.yhj118.groupware.electronicApproval;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 통합 품의서 구매 물품 등록, 수정, 삭제
@Setter
@Getter
@NoArgsConstructor
public class EARequestItem {

    private int approvalRequestItemId;          // 통합 품의서 구매 물품 식별번호
    private int approvalRequestId;              // 통합 품의서 식별번호
    private String itemName;                    // 통합 품의서 구매 물품명
    private int itemAmount;                     // 물품 금액
    private int approvalRequestTotalMoney;      // 구매 물품 총비용

}
