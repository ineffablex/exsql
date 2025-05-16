package com.example.exsql.legacy.newland;


import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author zxy
 * @version 2017/12/20
 *          Description:
 *          Modified By:
 */
public class HostUtil 
{

    public static InetAddress getInetAddress(){

        try
        {
            return InetAddress.getLocalHost();
        }
        catch(UnknownHostException e)
        {
        	e.printStackTrace();
        }
        return null;

    }

    public static String getHostIp(InetAddress netAddress){
        if(null == netAddress){
            return null;
        }
        String ip = netAddress.getHostAddress(); //get the ip address
        return ip;
    }

    public static String getHostName(InetAddress netAddress){
        if(null == netAddress){
            return null;
        }
        String name = netAddress.getHostName(); //get the host address
        return name;
    }
}
