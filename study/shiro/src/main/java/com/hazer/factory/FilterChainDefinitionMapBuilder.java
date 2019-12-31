package com.hazer.factory;

import java.util.LinkedHashMap;

/**
 * @ClassName: FilterChainDefinitionMapBuilder
 * @Author: Hazer
 * @Description:
 * @Date: 2019/12/31 14:51
 * @Version: 1.0
 */
public class FilterChainDefinitionMapBuilder {
    public LinkedHashMap<String, String> buildFilterChainDefinitionMap(){
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        map.put("/login.jsp", "anon");
        map.put("/shiro/login", "anon");
        map.put("/shiro/logout", "logout");
        map.put("/user.jsp", "authc,roles[user]");
        map.put("/admin.jsp", "authc,roles[admin]");
        map.put("/list.jsp", "user");

        map.put("/**", "authc");

        return map;
    }

}
