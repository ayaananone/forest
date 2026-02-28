package com.ceshi.forest;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String storedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO";
        String inputPassword = "123456";

        boolean matches = encoder.matches(inputPassword, storedPassword);
        System.out.println("密码是否匹配: " + matches);

        // 生成新密码
        String newPassword = encoder.encode("123456");
        System.out.println("新密码: " + newPassword);
    }
}