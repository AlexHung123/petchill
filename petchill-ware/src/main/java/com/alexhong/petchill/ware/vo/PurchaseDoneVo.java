package com.alexhong.petchill.ware.vo;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class PurchaseDoneVo {

    @NonNull
    private Long id;

    private List<PurchaseItemDoneVo> items;
}
