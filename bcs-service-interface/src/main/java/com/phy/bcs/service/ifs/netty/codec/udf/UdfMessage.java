package com.phy.bcs.service.ifs.netty.codec.udf;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@ToString
@Data
public class UdfMessage {
    private byte[] sid;
    private byte[] did;
    private byte[] mid;
    private byte[] bid;
    private byte[] res;
    private byte[] js;
    private byte[] len;
    private byte[] data;
}
