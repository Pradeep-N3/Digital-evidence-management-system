package com.decms;

import com.decms.service.StorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class StorageServiceTest {

    @Test
    void testResolveRelocatedStoredFilePath() {
        StorageService storageService = new StorageService("./vault");

        // Simulate an old stored path from when the project was located at C:\Users\prade\.gemini\antigravity\scratch\decms
        String oldStoredPath = "C:\\Users\\prade\\.gemini\\antigravity\\scratch\\decms\\backend\\vault\\cases\\CASE-2026-TEST\\EVD-2026-0001_1794501223035.log";

        Path resolved = storageService.resolveStoredFilePath(oldStoredPath);
        Assertions.assertNotNull(resolved);
        Assertions.assertTrue(Files.exists(resolved), "Resolved path should physically exist on disk");
        Assertions.assertTrue(resolved.toString().endsWith("EVD-2026-0001_1794501223035.log"));

        // Compute hash using the old path (which should automatically resolve)
        String hash = storageService.computeFileSha256(oldStoredPath);
        Assertions.assertNotNull(hash);
        Assertions.assertEquals(64, hash.length(), "SHA-256 hash must be 64 hexadecimal characters");
    }
}

