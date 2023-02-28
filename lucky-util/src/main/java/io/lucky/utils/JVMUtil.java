package io.lucky.utils;

import java.lang.management.ManagementFactory;

public class JVMUtil {

    public static String getJVMPid(){
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        int index = pid.indexOf("@");
        if(index > 0){
            pid = pid.substring(0,index);
        }
        return pid;
    }
}
