package com.example.dsd;

import com.example.dsd.service.CsvImportService;
import com.example.dsd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext; // 引入 ApplicationContext
import org.springframework.core.io.ClassPathResource; // 用于获取资源文件路径
import org.springframework.util.FileCopyUtils; // 用于文件操作

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@SpringBootApplication // 标记为 Spring Boot 应用
public class DsdApplication implements CommandLineRunner { // 实现 CommandLineRunner 以在启动后执行代码

    @Autowired // 注入 UserService
    private UserService userService;

    @Autowired // 注入 CsvImportService
    private CsvImportService csvImportService;

    @Autowired
    private ApplicationContext applicationContext; // 注入 ApplicationContext

    public static void main(String[] args) {
        SpringApplication.run(DsdApplication.class, args);
    }

    @Override // CommandLineRunner 的 run 方法会在 Spring Boot 应用启动完成后执行
    public void run(String... args) throws Exception {
        System.out.println("Spring Boot Application Started. Running demonstrations...");
        System.out.println("=========================================================");

        // --- 功能 1: 修改某用户的密码 ---
        System.out.println("功能 1: 尝试修改用户 'DOC0000' 的密码为 'newPassword123'");
        boolean passwordUpdated = userService.updateUserPassword("DOC0000", "newPassword123");
        if (passwordUpdated) {
            System.out.println("密码修改演示成功。");
        } else {
            System.err.println("密码修改演示失败。");
        }
        System.out.println("---------------------------------------------------------");


        // --- 功能 2: 导入某 .csv 文件 ---
        System.out.println("功能 2: 尝试从 'users_to_import.csv' 导入用户");

        // 获取 CSV 文件路径。 ClassPathResource 用于从 classpath 读取。
        // 为了让 CsvImportService 能直接使用文件路径，我们将资源文件复制到临时目录
        String csvFileName = "demo.csv";
        File tempCsvFile = null;
        try {
             ClassPathResource resource = new ClassPathResource(csvFileName);
             if (!resource.exists()) {
                 System.err.println("错误: 无法在 classpath 中找到 " + csvFileName);
                 return; // 如果文件不存在，则不继续执行导入
             }
             // 创建临时文件
             tempCsvFile = File.createTempFile("imported_users_", ".csv");
             tempCsvFile.deleteOnExit(); // 确保程序退出时删除临时文件

             // 将 classpath 资源复制到临时文件
             try (InputStream inputStream = resource.getInputStream();
                  FileOutputStream outputStream = new FileOutputStream(tempCsvFile)) {
                 FileCopyUtils.copy(inputStream, outputStream);
             }

             // 调用服务执行导入
             csvImportService.importUsersFromCsv(tempCsvFile.getAbsolutePath());
             System.out.println("CSV 导入演示完成。");

        } catch (Exception e) {
            System.err.println("处理或导入 CSV 文件时出错: " + e.getMessage());
            e.printStackTrace();
        } finally {
             // 如果需要，可以在这里删除临时文件，但 deleteOnExit() 通常足够
             // if (tempCsvFile != null && tempCsvFile.exists()) {
             //     tempCsvFile.delete();
             // }
        }


        System.out.println("=========================================================");
        System.out.println("Demonstrations finished.");

        // 可以在这里添加代码让应用保持运行，或者直接结束
        // System.exit(0); // 如果只是想运行完演示就退出
    }
}
