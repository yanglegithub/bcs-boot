package com.phy.bcs.service.ifs.netty.codec.pdxp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PdxpMessage {
    private int ver;
    private byte[] mid;
    private byte[] sid;
    private byte[] did;
    private byte[] bid;
    private int number;
    private byte flag;
    private byte[] reserve;
    private int date;
    private int time;
    private int l;
    private byte[] data;
}
