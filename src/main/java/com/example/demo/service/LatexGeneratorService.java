package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class LatexGeneratorService {

    private final Random random = new Random();

    public String toLatex(int value, String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "medium" -> generateMediumLatex(value);
            case "hard"   -> generateHardLatex(value);
            default       -> generateEasyLatex(value);
        };
    }

    private String generateEasyLatex(int n) {
        int variant = random.nextInt(5);
        int a = random.nextInt(5) + 2; // 2..6
        return switch (variant) {
            case 0 -> "\\sqrt{" + (n * n) + "}";
            case 1 -> "\\frac{" + (n * a) + "}{" + a + "}";
            case 2 -> (n + a) + " - " + a;
            case 3 -> {
                int[] divisors = getDivisors(n);
                int b = divisors[random.nextInt(divisors.length)];
                if (b == 1) yield String.valueOf(n);
                yield b + " \\cdot " + (n / b);
            }
            case 4 -> "|-" + n + "|";
            default -> String.valueOf(n);
        };
    }

    private String generateMediumLatex(int n) {
        int variant = random.nextInt(4);
        int a = n > 5 ? 2 : (random.nextInt(3) + 2); // 2..4

        return switch (variant) {
            case 0 -> "\\log_{" + a + "} " + (long) Math.pow(a, n);

            case 1 -> {
                if (n == 1) yield "\\lg 10";
                if (n == 2) yield "\\lg 100";
                if (n == 3) yield "\\lg 1000";
                if (n == 4) yield "\\lg 10000";
                yield "\\log_{" + a + "} " + (long) Math.pow(a, n);
            }

            case 2 -> {
                int b = random.nextInt(2) + 1; // 1..2
                long val = (long) Math.pow(a, n + b);
                yield "\\log_{" + a + "}(" + val + ") - " + b;
            }

            case 3 -> {
                int[] divisors = getDivisors(n);
                int b = divisors[random.nextInt(divisors.length)];
                if (b == 1) {
                    yield "\\log_{" + a + "} " + (long) Math.pow(a, n);
                } else {
                    yield b + " \\cdot \\log_{" + a + "} " + (long) Math.pow(a, n / b);
                }
            }

            default -> String.valueOf(n);
        };
    }

    private String generateHardLatex(int n) {
        int variant = random.nextInt(4);
        int a = random.nextInt(4) + 2; // 2..5

        return switch (variant) {
            case 0 -> "\\int_{0}^{" + n + "} 1 \\, dx";

            case 1 -> {
                if (n % 2 == 0) {
                    yield "(x^2)'|_{x=" + (n / 2) + "}";
                } else {
                    yield "(x^2)'|_{x=\\frac{" + n + "}{2}}";
                }
            }

            case 2 -> "\\int_{0}^{1} " + (n * a) + "x^{" + (a - 1) + "} \\, dx";

            case 3 -> {
                int k = random.nextInt(4) + 1; // 1..4
                if (n % k == 0) {
                    int c = n / k;
                    if (c == 1) yield "\\int_{0}^{" + k + "} 1 \\, dx";
                    yield "\\int_{0}^{" + k + "} " + c + " \\, dx";
                } else {
                    yield "\\int_{0}^{" + k + "} \\frac{" + n + "}{" + k + "} \\, dx";
                }
            }

            default -> String.valueOf(n);
        };
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