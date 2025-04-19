package com.example.factorial.src;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


public class DataBase extends Base{
    private int data;
    public DataBase(){
    }
    @Override
    public int GETDATE() {return data;}

    @Override
    public int GET(PermissionBase p) {
        return 0;
    }


}
