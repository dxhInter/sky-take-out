package com.sky.dto;

import com.sky.entity.AddressBook;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author dxh
 * @version 1.0
 * @project sky-take-out
 * @date 2024/2/20 19:57:53
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateDTO implements Serializable {
    private Orders orders;
    private AddressBook addressBook;
    private Long userId;
    private ShoppingCart shoppingCart;
}
