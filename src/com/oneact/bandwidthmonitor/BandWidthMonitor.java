package com.oneact.bandwidthmonitor;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.awt.event.ActionEvent;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.RangeType;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;


public class BandWidthMonitor  implements ActionListener{

	private JFrame frmBandwidthMonitor;
	private JLabel lblPacketsNb;
	private JLabel lblPacketsNbUp;
	private JLabel lblSessionstatus;
    private JLabel lblSpeed;
    private JLabel lblSpeedup;
    
    private JLabel lblInterface;
    private JComboBox<String> comboBox;
	private ArrayList<Integer> interfacesList;

	private JButton btnStart;
	private JButton btnStop;
	private JButton btnReset;
	private JButton showFileDialogButton;
	
	private Timer refreshTimer;
	private int nb;

	private int downloadedPacketNb;
	private int uploadedPacketNb;
	private long downloadedDataSize;
	private long uploadedDataSize;

    public XYSeries downSpeedSerie = null;
    public XYSeries upSpeedSerie = null;
    XYDataset speedDataset = null;
    JFreeChart speedChart = null;
    ChartPanel chartPanel = null;

    Queue<Integer> downSpeedFifo = new LinkedList<Integer>();
    Queue<Integer> upSpeedFifo = new LinkedList<Integer>();

    PacketLookupThread thread = null;
    DataExporter dataExporter = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) { 
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BandWidthMonitor window = new BandWidthMonitor();
					window.frmBandwidthMonitor.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public BandWidthMonitor()
	{
		initialize();
		
		dataExporter = new DataExporter();
		
		nb = 0;
		downloadedPacketNb = 0;
		uploadedPacketNb = 0;
		downloadedDataSize = 0;
		uploadedDataSize = 0;
		
		downSpeedSerie = new XYSeries("Download speed in kbps");
		upSpeedSerie = new XYSeries("Upload speed in kbps");
		
		for(int i = 0 ; i<40; i++)
		{
			downSpeedFifo.add(0);
			upSpeedFifo.add(0);
		}
		
		speedDataset = createDatasets();
		speedChart = createChart(speedDataset);
        chartPanel = new ChartPanel(speedChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setBounds(130, 6, 500, 270);
        frmBandwidthMonitor.getContentPane().add(chartPanel);
        
        // Get the available network interfaces
        discoverInterfaces();
        
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmBandwidthMonitor = new JFrame();
		frmBandwidthMonitor.setTitle("Bandwidth Monitor");
		frmBandwidthMonitor.setForeground(Color.DARK_GRAY);
		frmBandwidthMonitor.setBackground(Color.DARK_GRAY);
		frmBandwidthMonitor.getContentPane().setBackground(Color.DARK_GRAY);
		frmBandwidthMonitor.setBounds(100, 100, 700, 400);
		frmBandwidthMonitor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmBandwidthMonitor.getContentPane().setLayout(null);
		
		lblSessionstatus = new JLabel("Recording time --s");
		lblSessionstatus.setForeground(Color.LIGHT_GRAY);
		lblSessionstatus.setBounds(16, 308, 171, 16);
		frmBandwidthMonitor.getContentPane().add(lblSessionstatus);
		
		btnStart = new JButton("Start");
		btnStart.addActionListener(this);
		btnStart.setBounds(6, 105, 117, 29);
		frmBandwidthMonitor.getContentPane().add(btnStart);
		
		btnStop = new JButton("Stop");
		btnStop.setEnabled(false);
		btnStop.addActionListener(this);
		btnStop.setBounds(6, 146, 117, 29);
		frmBandwidthMonitor.getContentPane().add(btnStop);
		
		btnReset = new JButton("Reset");
		btnReset.setEnabled(false);
		btnReset.addActionListener(this);
		btnReset.setBounds(6, 187, 117, 29);
		frmBandwidthMonitor.getContentPane().add(btnReset);
		
		lblSpeed = new JLabel("Download speed: -- kb/s");
		lblSpeed.setForeground(Color.cyan);
		lblSpeed.setBounds(188, 280, 200, 16);
		frmBandwidthMonitor.getContentPane().add(lblSpeed);
		
		lblSpeedup = new JLabel("Upload speed: -- kb/s");
		lblSpeedup.setForeground(Color.yellow);
		lblSpeedup.setBounds(188, 308, 200, 16);
		frmBandwidthMonitor.getContentPane().add(lblSpeedup);

		lblPacketsNb = new JLabel("Downloaded Packets:");
		lblPacketsNb.setForeground(Color.cyan);
		lblPacketsNb.setBounds(386, 280, 294, 16);
		frmBandwidthMonitor.getContentPane().add(lblPacketsNb);

		lblPacketsNbUp = new JLabel("Uploaded Packets:");
		lblPacketsNbUp.setForeground(Color.yellow);
		lblPacketsNbUp.setBounds(386, 308, 294, 16);
		frmBandwidthMonitor.getContentPane().add(lblPacketsNbUp);
        
        comboBox = new JComboBox<String>();
        
        comboBox.setBackground(Color.WHITE);
        comboBox.setBounds(6, 46, 117, 27);
        frmBandwidthMonitor.getContentPane().add(comboBox);
        
        lblInterface = new JLabel("Interface");
        lblInterface.setForeground(Color.LIGHT_GRAY);
        lblInterface.setBounds(16, 30, 61, 16);
        frmBandwidthMonitor.getContentPane().add(lblInterface);
        
        showFileDialogButton = new JButton("Export...");
		showFileDialogButton.addActionListener(this);
		showFileDialogButton.setEnabled(false);
		showFileDialogButton.setBounds(6, 343, 117, 29);
		frmBandwidthMonitor.getContentPane().add(showFileDialogButton);
        
		// Refresh Timer
        refreshTimer = new Timer(1000, this);
        
	}
	

    private void discoverInterfaces()
    {
    	//Obtain the list of network interfaces
		NetworkInterface[] interfaces = JpcapCaptor.getDeviceList();
		interfacesList = new ArrayList<Integer>();
		
		// For each network interface
		for (int i = 0; i < interfaces.length; i++)
		{
			// Print out its name and description
			System.out.println(i+": "+interfaces[i].name + "(" + interfaces[i].description+")");

			// Print out its datalink name and description
			System.out.println(" datalink: "+interfaces[i].datalink_name + "(" + interfaces[i].datalink_description+")");
			if(interfaces[i].datalink_description.equalsIgnoreCase("Ethernet"))
			{
				comboBox.addItem(interfaces[i].name);
				interfacesList.add(i);
			}

			// Print out its MAC address
			System.out.print(" MAC address:");
			for (byte b : interfaces[i].mac_address)
				System.out.print(Integer.toHexString(b&0xff) + ":");
			System.out.println();

			// Print out its IP address, subnet mask and broadcast address
			for (NetworkInterfaceAddress a : interfaces[i].addresses)
				System.out.println(" address:"+a.address + " " + a.subnet + " "+ a.broadcast);
		}	
    
    }

    private XYDataset createDatasets()
    {
        downSpeedSerie.clear();
        upSpeedSerie.clear();

        Iterator<Integer> itDown = downSpeedFifo.iterator();
        Iterator<Integer> itUp = upSpeedFifo.iterator();

        double a = 1.0;
        while (itDown.hasNext())
        {
            Integer downValue = (Integer) itDown.next();
            Integer upValue = (Integer) itUp.next();
            downSpeedSerie.add(a, ((double) downValue));
            upSpeedSerie.add(a++, ((double) upValue));
        }

        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(downSpeedSerie);
        dataset.addSeries(upSpeedSerie);

        return dataset;
    }

    private JFreeChart createChart(final XYDataset dataset) {

        // create the chart...
        final JFreeChart chart = ChartFactory.createXYAreaChart(
                "",
                "Seconds",
                "Speed (kb/s)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
                );

        // chart color customisation
        chart.setBackgroundPaint(Color.darkGray);
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.darkGray);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(0, Color.cyan); 
        plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(1, Color.yellow);

        final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRangeMinimumSize(500);
        yAxis.setRangeType(RangeType.POSITIVE);
        yAxis.setLabelPaint(Color.gray);
        yAxis.setTickLabelPaint(Color.gray);
        final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setRange(0, 40);
        
        return chart;
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		
		Object source=e.getSource();
		
		if(source == btnStart)
		{
			// Open log file
			dataExporter.openFile();
			
			lblSessionstatus.setText("Recording time " + nb + "s");
			thread = new PacketLookupThread();
			thread.setIndex(interfacesList.get(comboBox.getSelectedIndex()));
			thread.start();
			refreshTimer.start();
			
			btnStart.setEnabled(false);
			btnReset.setEnabled(false);
			comboBox.setEnabled(false);
			btnStop.setEnabled(true);
			showFileDialogButton.setEnabled(false);
		}
		else if(source == btnStop)
		{
			// Open log file
			dataExporter.closeFile();
			
			lblSessionstatus.setText("Session stopped");
			thread.stopsession();
			refreshTimer.stop();

			btnStart.setEnabled(true);
			btnReset.setEnabled(true);
			comboBox.setEnabled(true);
			btnStop.setEnabled(false);
			showFileDialogButton.setEnabled(true);
		}
		else if(source == btnReset)
		{
			lblSessionstatus.setText("Recording time --s");
			nb = 0;
			downloadedPacketNb = 0;
			uploadedPacketNb = 0;
			downloadedDataSize = 0;
			uploadedDataSize = 0;
			lblPacketsNb.setText("Downloaded Packets: " + downloadedPacketNb);
			lblPacketsNbUp.setText("Uploaded Packets: " + uploadedPacketNb);
			lblSpeed.setText("Download speed: -- kb/s");
			lblSpeedup.setText("Upload speed: -- kb/s");

		}
		else if(source == refreshTimer)
		{
			nb++;
			lblSessionstatus.setText("Recording time " + nb + "s");

			// Get download speed
			float f_downSpeed = PacketCatcher.getDownloadedDataSize();
			int downSpeed = new Integer((int) (f_downSpeed / 1024));
			downloadedDataSize += downSpeed;
			String speed = "";
			if(f_downSpeed >= 1000)
			{
				speed = String.format("%d", downSpeed);
			}
			else
				speed = String.format("%.3f", (f_downSpeed / 1024));
			
			lblSpeed.setText("Download speed: " + speed + " kb/s");

			// Get Upload speed
			float f_upSpeed = PacketCatcher.getUploadedDataSize();
			int upSpeed = new Integer((int) (f_upSpeed / 1024));
			uploadedDataSize += upSpeed;
			if(f_upSpeed >= 1000)
			{
				speed = String.format("%d", upSpeed);
			}
			else
				speed = String.format("%.3f", (f_upSpeed / 1024));
			
			lblSpeedup.setText("Upload speed: " + speed + " kb/s");
			
			// Get new downloaded packet nb
			downloadedPacketNb += PacketCatcher.getDownloadedPacketNb();
			uploadedPacketNb += PacketCatcher.getUploadedPacketNb();
			
			String dataStr="";
			if(downloadedDataSize >= 1000)
				dataStr = String.format(" (%.1fMb)", ((float)downloadedDataSize / 1000));
			else
				dataStr = String.format(" (%dkb)", downloadedDataSize);
			lblPacketsNb.setText("Downloaded Packets: " + downloadedPacketNb + dataStr);

			if(uploadedDataSize >= 1000)
				dataStr = String.format(" (%.1fMb)", (float)(uploadedDataSize / 1000));
			else
				dataStr = String.format(" (%dkb)", uploadedDataSize);
			lblPacketsNbUp.setText("Uploaded Packets: " + uploadedPacketNb + dataStr);
			
            downSpeedFifo.add(downSpeed >= 1 ? downSpeed : 1);
            if (downSpeedFifo.size() > 40)
            	downSpeedFifo.poll();
            upSpeedFifo.add(upSpeed >= 1 ? upSpeed : 1);
            if (upSpeedFifo.size() > 40)
            	upSpeedFifo.poll();

			// Write in log file
			dataExporter.writeLine(f_downSpeed, f_upSpeed, nb);

    		speedDataset = createDatasets();
    		speedChart = createChart(speedDataset);
            chartPanel = new ChartPanel(speedChart);
		}
		else if(source == showFileDialogButton)
		{
			final JFileChooser fileDialog = new JFileChooser();
			fileDialog.setSelectedFile(new File("log.csv"));
			fileDialog.setApproveButtonText("Save");
			
			int returnVal = fileDialog.showDialog(frmBandwidthMonitor, "Save");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				java.io.File file = fileDialog.getSelectedFile();
				try
				{
					dataExporter.copyFile(file.getPath());		
					JOptionPane.showMessageDialog(frmBandwidthMonitor, "Log exported!", "BandWidth Monitor", JOptionPane.INFORMATION_MESSAGE);
				} 
				catch (IOException e1) 
				{
					// Display an error popup
					JOptionPane.showMessageDialog(frmBandwidthMonitor, "Unable to create the log file!", "BandWidth Monitor",
							JOptionPane.WARNING_MESSAGE);
					e1.printStackTrace();
				}
			}
		}
	}
}
