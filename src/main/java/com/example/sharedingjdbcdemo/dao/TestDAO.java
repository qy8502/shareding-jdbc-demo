package com.example.sharedingjdbcdemo.dao;

import com.example.sharedingjdbcdemo.dto.UserDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TestDAO {

    @Select("SELECT id,name,type FROM user")
    List<UserDTO> listUser();

    @Insert("insert into user(id,name,type) values(#{id},#{name},#{type})")
    void addUser(UserDTO type);
}
