package com.bgs.cdc.traccar.service;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DeviceService {

    private final HashOperations<String, String, String> deviceNameOps;
    private final HashOperations<String, String, Float> deviceSpeedOps;

    public DeviceService(RedisTemplate<String, Object> redisTemplate) {
        this.deviceNameOps = redisTemplate.opsForHash();
        this.deviceSpeedOps = redisTemplate.opsForHash();
    }

    // Store device name
    public void saveDeviceName(String deviceId, String name) {
        deviceNameOps.put("deviceName", deviceId, name);
    }

    // Retrieve device name
    public String getDeviceName(String deviceId) {
        return deviceNameOps.get("deviceName", deviceId);
    }

    // Store device speed
    public void saveDeviceSpeed(String deviceId, Float speed) {
        deviceSpeedOps.put("deviceSpeed", deviceId, speed);
    }

    // Retrieve device speed
    public Float getDeviceSpeed(String deviceId) {
        return deviceSpeedOps.get("deviceSpeed", deviceId);
    }

    // Retrieve all device names
    public Map<String, String> getAllDeviceNames() {
        return deviceNameOps.entries("deviceName");
    }

    // Retrieve all device speeds
    public Map<String, Float> getAllDeviceSpeeds() {
        return deviceSpeedOps.entries("deviceSpeed");
    }
}
