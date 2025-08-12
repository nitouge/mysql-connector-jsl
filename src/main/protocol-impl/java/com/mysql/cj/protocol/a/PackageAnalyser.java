package com.mysql.cj.protocol.a;

/**
 * 数据包分析工具
 */
public final class PackageAnalyser {

    private PackageAnalyser() {
    }

    /**
     * 将指定byte数组以16进制的形式打印到控制台
     */
    public static void printHexString(byte[] message) {
        // printHexString(message, 16);
        printByteArrayVertical(message);
        // printByteArrayHorizontal(message, 1);
    }

    /**
     * 将指定byte数组以16进制的形式打印到控制台
     */
    public static void printHexString(byte[] message, int colsNum) {
        StringBuilder pack = new StringBuilder();
        String hex;
        for (int i = 0; i < message.length; i++) {
            if (i % colsNum == 0) {
                pack.append("\n");
            }
            hex = Integer.toHexString(message[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            pack.append(hex.toUpperCase()).append(" ");
        }
        System.out.println(pack);
    }

    /**
     * 纵向展示（推荐清晰阅读）
     *
     * @param message
     */
    public static void printByteArrayVertical(byte[] message) {
        if (message == null || message.length == 0) {
            System.out.println("Byte array is empty.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-5s %-5s %-5s %-10s\n", "Idx", "Hex", "Dec", "Binary"));
        for (int i = 0; i < message.length; i++) {
            int b = message[i] & 0xFF;
            sb.append(String.format("%-5d %-5s %-5d %-10s\n",
                    i,
                    String.format("%02X", b),
                    b,
                    String.format("%8s", Integer.toBinaryString(b)).replace(' ', '0')));
        }
        System.out.println(sb);
    }

    /**
     * 横向分组展示（每行展示多个），打印 byte[] 数组的十六进制、十进制和二进制形式（支持对齐和按列输出）
     */
    public static void printByteArrayHorizontal(byte[] message, int colsNum) {
        if (message == null || message.length == 0) {
            System.out.println("Byte array is empty.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        String header = String.format("%-5s %-5s %-5s %-10s", "Idx", "Hex", "Dec", "Binary");
        sb.append(header).append("\n");

        for (int i = 0; i < message.length; i++) {
            int unsignedByte = message[i] & 0xFF;
            String line = String.format("%-5d %-5s %-5d %-10s",
                    i,
                    String.format("%02X", unsignedByte),
                    unsignedByte,
                    String.format("%8s", Integer.toBinaryString(unsignedByte)).replace(' ', '0')
            );
            sb.append(line).append((i + 1) % colsNum == 0 ? "\n" : "  ");
        }
        System.out.println(sb);
    }

}