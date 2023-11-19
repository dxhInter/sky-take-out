package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {
    /**
     * 查询当前登录用户的所有地址信息
     * @param addressBook
     * @return
     */
    List<AddressBook> list(AddressBook addressBook);

    /**
     * 保存地址信息
     * @param addressBook
     */
    void save(AddressBook addressBook);

    /**
     * 根据id查询地址信息
     * @param id
     * @return
     */
    AddressBook getById(Long id);

    /**
     * 修改地址信息
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 设置默认地址
     * @param addressBook
     */
    void setDefault(AddressBook addressBook);

    /**
     * 删除地址信息
     * @param id
     */
    void delete(Long id);
}
