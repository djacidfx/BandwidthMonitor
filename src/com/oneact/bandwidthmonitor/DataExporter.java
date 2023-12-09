package com.oneact.bandwidthmonitor;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DataExporter {
	
	FileWriter m_writer;
	
	public int openFile()
	{
		int retVal = 0;
		
		try
		{
			m_writer = new FileWriter("test.csv");
	        StringBuilder sb = new StringBuilder();
	        sb.append("Time (s)");
	        sb.append(',');
	        sb.append("Download Speed (kb/s)");
	        sb.append(',');
	        sb.append("Upload Speed (kb/s)");
	        sb.append('\n');
	        m_writer.write(sb.toString());
			
		} 
		catch (IOException e)
		{
			retVal = -1;
			e.printStackTrace();
		}
		
		return retVal;
	}
	
	public int closeFile()
	{
		int retVal = 0;
		
		try
		{
			m_writer.close();
		}
		catch (IOException e)
		{
			retVal = -1;
			e.printStackTrace();
		}
		
		return retVal;
	}
	
	public int writeLine(float downloadSpeed, float uploadSpeed, int time)
	{
		int retVal = 0;

        StringBuilder sb = new StringBuilder();
        sb.append(time);
        sb.append(',');
        sb.append(Float.toString(downloadSpeed));
        sb.append(',');
        sb.append(Float.toString(uploadSpeed));
        sb.append('\n');

        try
        {
			m_writer.write(sb.toString());
		}
        catch (IOException e)
        {
			retVal = -1;
			e.printStackTrace();
		}
		
		return retVal;
	}
	
	public void copyFile(String path) throws IOException
	{
		Files.copy(Paths.get("test.csv"), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
		
	}
	
	

}
