package com.rw.example.serial.com.util;

import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.fazecast.jSerialComm.SerialPort.NO_PARITY;
import static com.fazecast.jSerialComm.SerialPort.ONE_STOP_BIT;

@Slf4j
public class SerialComUtil {

    private static volatile SerialPort ACTIVATED_COM = null;
    private static final Map<String, SerialPort> COM_MAP = new HashMap<>();

    private SerialComUtil() {

    }

    public static void scanPort() {
        SerialPort[] ports = SerialPort.getCommPorts();
        COM_MAP.clear();
        if (ports != null && ports.length > 0) {
            for (SerialPort port : ports) {
                COM_MAP.put(port.getSystemPortName(), port);
                log.info("SCAN AVAILABLE PORT:{}", port.getSystemPortName());
            }
        } else {
            log.warn("CAN'T FIND ANY SERIAL PORT...");
        }
    }

    public static void open(String port) {
        log.info("OPEN SERIAL PORT:{}", port);
        ACTIVATED_COM = COM_MAP.get(port);
        ACTIVATED_COM.openPort();
        ACTIVATED_COM.setComPortParameters(9600, 8, ONE_STOP_BIT, NO_PARITY);

    }

    public static byte[] sendDataWaitResp(byte[] data) throws InterruptedException, ExecutionException, TimeoutException {
        log.debug("SERIAL SEND:{}", new String(data));

        CompletableFuture<byte[]> future = new CompletableFuture<>();
        new Thread(() -> {
            byte[] bytes = new byte[512];
            int len;
            while ((len = ACTIVATED_COM.readBytes(bytes, bytes.length)) < 1) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            byte[] resp = new byte[len];
            System.arraycopy(bytes, 0, resp, 0, len);
            future.complete(resp);
        }).start();

        ACTIVATED_COM.writeBytes(data, data.length);
        return future.get(5, TimeUnit.MINUTES);
    }

    public static void close() {
        if (ACTIVATED_COM != null) {
            if (ACTIVATED_COM.isOpen()) {
                ACTIVATED_COM.closePort();
            }
        }
    }

}
