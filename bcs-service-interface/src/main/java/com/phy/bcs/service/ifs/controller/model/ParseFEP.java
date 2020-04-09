package com.phy.bcs.service.ifs.controller.model;

import com.phy.bcs.service.ifs.controller.util.ParseUtil;
import lombok.Data;

import java.io.UnsupportedEncodingException;

/**
 * 创建者：huangj
 * 类描述: FEP协议包解析
 */
@Data
public class ParseFEP {
    //标志
    private String flag;
    private SendFEPMode sendFEPMode;
    private FinishFEPMode finishFEPMode;
    private AnswerFEPMode answerFEPMode;
    private DataFEPMode dataFEPMode;

    public ParseFEP(byte[] bytes) throws UnsupportedEncodingException {
        String flag = new String(ParseUtil.strChange(bytes, 0, 1),"UTF-8");
        //协议包内容
        byte[] packageData  = ParseUtil.strChange(bytes,1,bytes.length);
        switch (Integer.parseInt(flag)) {
            case 1:
                sendFEPMode = new SendFEPMode(packageData);
                break;
            case 2:
                answerFEPMode = new AnswerFEPMode(packageData);
                break;
            case 3:
                finishFEPMode = new FinishFEPMode(packageData);
                break;
            case 4:
                dataFEPMode = new DataFEPMode(packageData);
                break;
            default:
                break;
        }
    }
    public ParseFEP(){

    }




}
