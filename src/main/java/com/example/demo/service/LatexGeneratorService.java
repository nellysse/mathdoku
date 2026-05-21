package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class LatexGeneratorService {

    private final Random random = new Random();

    public String toLatex(int value, String difficulty) {
        String result;
        int attempts = 0;
        // Жесткие лимиты: Medium макс 14 символов, Hard макс 22
        int maxLength = difficulty.equalsIgnoreCase("hard") ? 22 : 14;
        
        do {
            result = switch (difficulty.toLowerCase()) {
                case "medium" -> generateMediumLatex(value);
                case "hard"   -> generateHardLatex(value);
                default       -> generateEasyLatex(value);
            };
            attempts++;
        } while (result.length() > maxLength && attempts < 10);
        
        // Запасной план: если формула физически слишком длинная, возвращаем цифру
        if (result.length() > maxLength) {
            return String.valueOf(value);
        }
        return result;
    }

    private String generateEasyLatex(int n) {
        int variant = random.nextInt(3);
        int a = random.nextInt(3) + 2; 
        return switch (variant) {
            case 0 -> "\\sqrt{" + (n * n) + "}";
            case 1 -> (n + a) + "-" + a;
            case 2 -> {
                int[] divisors = getDivisors(n);
                int b = divisors[random.nextInt(divisors.length)];
                if (b == 1) yield String.valueOf(n);
                yield b + "\\cdot" + (n / b);
            }
            default -> String.valueOf(n);
        };
    }

    private String generateMediumLatex(int n) {
        int variant = random.nextInt(2);
        int a = n > 3 ? 2 : 3; 

        return switch (variant) {
            case 0 -> "\\log_{" + a + "}" + safePow(a, n);
            case 1 -> {
                if (n == 1) yield "\\lg 10";
                if (n == 2) yield "\\lg 100";
                if (n == 3) yield "\\lg 1000";
                yield "\\log_{" + a + "}" + safePow(a, n);
            }
            default -> String.valueOf(n);
        };
    }

    private String generateHardLatex(int n) {
        int variant = random.nextInt(2);

        return switch (variant) {
            case 0 -> "\\int_{0}^{" + n + "} dx";
            case 1 -> {
                if (n % 2 == 0) yield "(x^2)'|_{" + (n / 2) + "}";
                yield "(x^2)'|_{\\frac{" + n + "}{2}}";
            }
            default -> String.valueOf(n);
        };
    }

    private long safePow(int base, int exponent) {
        return Math.round(Math.pow(base, exponent));
    }

    private int[] getDivisors(int n) {
        return switch (n) {
            case 1 -> new int[]{1};
            case 2 -> new int[]{1, 2};
            case 3 -> new int[]{1, 3};
            case 4 -> new int[]{1, 2, 4};
            case 5 -> new int[]{1, 5};
            case 6 -> new int[]{1, 2, 3, 6};
            case 7 -> new int[]{1, 7};
            case 8 -> new int[]{1, 2, 4, 8};
            case 9 -> new int[]{1, 3, 9};
            default -> new int[]{1};
        };
    }
}
