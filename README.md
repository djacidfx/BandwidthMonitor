# BandwidthMonitor
 Java desktop application that allows to monitor the download/upload speed and amount of data (number of packets, size) on one of the network devices available, by capturing and analysing the packets.
How to Use:
Open Eclipse IDE, and import the project in your workspace:
- File -> Import
- General -> Existing Project into Workspace and click “next”.
- Select: “Select archive file” and browse the “BWMonitor_SourceCode.zip” archive. - In the “Project” view, Select the “BandwidthMonitor” project.
- Click “Finish”.


In order to connect to the network devices and be able to capture packets, the application need root privileges. So, to execute it, please follow these directions:
- Right click on the BandwidthMonitor Project, then “Export” -> “Java” -> “Runnable JAR File”, and follow the wizard.
- Execute the JAR file in a terminal using “sudo” (for macos/linux users):
sudo java -jar BM_runnable.jar.

For more information, please refer to the user guide pdf. 
