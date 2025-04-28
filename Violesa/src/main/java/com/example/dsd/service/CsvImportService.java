package com.example.dsd.service;

import com.example.dsd.model.User;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets; // 确保文件编码正确

/**
 * 处理 CSV 文件导入的服务
 */
@Service
public class CsvImportService {

    @Autowired
    private UserService userService; // 注入 UserService 以保存用户

    /**
     * 从指定的 CSV 文件路径导入用户数据
     * 假设 CSV 文件格式为: username,password (无表头)
     * @param csvFilePath CSV 文件路径
     */
    @Transactional // 整个导入过程作为一个事务
    public void importUsersFromCsv(String csvFilePath) {
        int count = 0;
        // 使用 try-with-resources 确保 CSVReader 被正确关闭
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath, StandardCharsets.UTF_8))) { // 指定 UTF-8 编码
            String[] nextLine;
            System.out.println("开始从 " + csvFilePath + " 导入用户...");

            // 逐行读取 CSV
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length >= 2) { // 确保行至少有两列
                    String username = nextLine[0].trim();
                    String password = nextLine[1].trim();

                    // 创建 User 对象
                    User newUser = new User(username, password);

                    // 调用 UserService 保存用户
                    // 注意：这里没有做重复用户检查，如果需要，可以在保存前查询用户是否存在
                    userService.saveUser(newUser);
                    count++;
                    System.out.println("已导入用户: " + username);
                } else {
                    System.err.println("警告：跳过格式不正确的行: " + String.join(",", nextLine));
                }
            }
            System.out.println("CSV 用户导入完成，共导入 " + count + " 个用户。");

        } catch (IOException e) {
            System.err.println("读取 CSV 文件时出错: " + csvFilePath);
            e.printStackTrace(); // 打印详细错误信息
            // 可以在这里抛出自定义异常或进行其他错误处理
        } catch (CsvValidationException e) {
            System.err.println("CSV 文件验证失败: " + csvFilePath);
            e.printStackTrace();
        } catch (Exception e) { // 捕获其他潜在异常
            System.err.println("导入过程中发生未知错误: ");
            e.printStackTrace();
        }
    }
}