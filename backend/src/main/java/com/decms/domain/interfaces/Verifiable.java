package com.decms.domain.interfaces;

public interface Verifiable {
    String calculateHash();
    boolean verifyIntegrity(String currentHash);
    String getHashAlgorithm();
}
