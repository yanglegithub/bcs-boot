package com.phy.bcs.common.util.exception;

public class DataInterfaceException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 4554412823825008739L;

    private String dIType;
    
    public DataInterfaceException(String dItype, String message) {
        super(message);
        this.dIType = dItype;
    }
    
    public DataInterfaceException(String dItype, String message, Throwable cause) {
        super(message, cause);
        this.dIType = dItype;
    }

    public String getDIType() {
        return dIType;
    }

    public void setDIType(String dIType) {
        this.dIType = dIType;
    }
}
