package com.oneact.bandwidthmonitor;



import java.util.Arrays;
import jpcap.PacketReceiver;
import jpcap.packet.DatalinkPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;

public class PacketCatcher implements PacketReceiver
{

    public static float downloadedDataSize = 0;
    public static float uploadedDataSize = 0;
    public static int downloadedPacketNb = 0;
    public static int uploadedPacketNb = 0;
    public static float tmpSize = 0;
    public static int tmpNb = 0;
    public static byte[] macAddress = {0, 0, 0, 0, 0, 0};
    
	// This callback method is called every time Jpcap lib captures a packet
	public void receivePacket(Packet packet)
	{   
	    DatalinkPacket dp = packet.datalink;
	    EthernetPacket ept=(EthernetPacket)dp;
	    
	    byte[] destAddress = ept.dst_mac;
	    System.out.println("dest addr: " + destAddress);
	    
	    // Get the packet direction looking at the destination address
	    if(Arrays.equals(destAddress, macAddress) == true)
	    {
	    	downloadedDataSize += packet.len;
	    	downloadedPacketNb ++;
	    	System.out.println("downloadedSize: " + downloadedDataSize);
	    }
	    else
	    {
	    	uploadedDataSize += packet.len;
	    	uploadedPacketNb ++;
	    	System.out.println("uploadedSize: " + uploadedDataSize);
	    }
	}

    public static float getDownloadedDataSize()
    {
        tmpSize = downloadedDataSize;
        downloadedDataSize = 0;
        return tmpSize;
    }

    public static float getUploadedDataSize()
    {
        tmpSize = uploadedDataSize;
        uploadedDataSize = 0;
        return tmpSize;
    }

    public static int getDownloadedPacketNb()
    {
        tmpNb = downloadedPacketNb;
        downloadedPacketNb = 0;
        return tmpNb;
    }

    public static int getUploadedPacketNb()
    {
        tmpNb = uploadedPacketNb;
        uploadedPacketNb = 0;
        return tmpNb;
    }

    public static void setMacAddress(byte[] addr)
    {
    	System.arraycopy(addr, 0, macAddress, 0, 6);
    }

}
