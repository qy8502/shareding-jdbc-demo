package com.example.sharedingjdbcdemo.dao;

import com.example.sharedingjdbcdemo.dto.UserDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TestDAO {

    @Select("SELECT id,name,type,infos,tags FROM user")
    List<UserDTO> listUser();

    @Select("<script>SELECT id,name,type,infos,tags FROM user " +
            "WHERE 1=1 AND <if test=\"name!=null\"> name=#{name}</if>" +
            "ORDER BY id</script>")
    List<UserDTO> searchUser(@Param("name") String name);

    @Insert("insert into user(id,name,type,infos,tags) values(#{id},#{name},#{type},#{infos},#{tags})")
    void addUser(UserDTO type);
}
