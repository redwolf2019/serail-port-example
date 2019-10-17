package com.rw.example.serial.com;

import com.rw.example.serial.com.util.SerialComUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Slf4j
public class Application {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        SerialComUtil.scanPort();
        SerialComUtil.open("COM1");
        byte[] resp = SerialComUtil.sendDataWaitResp((LocalDateTime.now().toString() + "\n").getBytes());
        log.info("Serial port resp:{}", new String(resp));
        SerialComUtil.close();

//        System.in.read();
    }
}
