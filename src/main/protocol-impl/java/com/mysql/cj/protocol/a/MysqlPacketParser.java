package com.mysql.cj.protocol.a;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MysqlPacketParser {

    public static void parseTCPPayload(byte[] tcpPayload) {
        if (tcpPayload.length < 4) {
            System.out.println("too short");
            return;
        }
        int payloadLen = (tcpPayload[0] & 0xFF) | ((tcpPayload[1] & 0xFF) << 8) | ((tcpPayload[2] & 0xFF) << 16);
        int seqId = tcpPayload[3] & 0xFF;

        System.out.println("【MysqlPacketParser】 >>>>>> payloadLen = " + payloadLen + ", seqId = " + seqId);

        if (tcpPayload.length < 4 + payloadLen) {
            System.out.println("【MysqlPacketParser】 >>>>>> incomplete packet (need " + (4 + payloadLen) + " bytes, have " + tcpPayload.length + ")");
            return;
        }

        int command = tcpPayload[4] & 0xFF;
        byte[] body = Arrays.copyOfRange(tcpPayload, 5, 4 + payloadLen);
        String sql = new String(body, StandardCharsets.UTF_8);
        System.out.println("【MysqlPacketParser】 >>>>>> command = 0x" + Integer.toHexString(command) + " (" + command + ")");
        System.out.println("【MysqlPacketParser】 >>>>>> sql = " + sql);
    }
}

