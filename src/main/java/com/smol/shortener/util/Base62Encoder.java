package com.smol.shortener.util;

import org.springframework.stereotype.Component;

/**
 * Converts a numeric database ID into a short alphanumeric string.
 *
 * HOW BASE62 WORKS:
 * Just like binary is base-2 (digits: 0,1) and decimal is base-10 (digits: 0-9),
 * Base62 uses 62 characters: 0-9, a-z, A-Z
 *
 * EXAMPLE: encode(125) → "cb"
 *   125 / 62 = 2 remainder 1  → characters[1] = 'b'
 *   2   / 62 = 0 remainder 2  → characters[2] = 'c'
 *   Read remainders in reverse: "cb"
 *
 * WHY THIS GUARANTEES UNIQUENESS:
 * Each DB row has a unique auto-increment ID.
 * Same input always produces same output.
 * Different input always produces different output.
 * Zero collision risk — no retry loops needed.
 *
 * CAPACITY:
 * 6-character code = 62^6 = ~56 billion unique URLs
 */
@Component
public class Base62Encoder {

    private static final String CHARACTERS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = CHARACTERS.length(); // 62

    public String encode(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be a positive number, got: " + id);
        }

        StringBuilder sb = new StringBuilder();
        long num = id;

        while (num > 0) {
            int remainder = (int) (num % BASE);
            sb.append(CHARACTERS.charAt(remainder));
            num /= BASE;
        }

        // Built in reverse order, so flip it
        return sb.reverse().toString();
    }

    public long decode(String code) {
        long result = 0;
        for (char c : code.toCharArray()) {
            result = result * BASE + CHARACTERS.indexOf(c);
        }
        return result;
    }
}