# prepfiles
ReplayTV video file processing
## Background
* The ReplayTV is a discontinued DVR (digital video recorder) that is based on a Linux 
system and supports sharing files with other ReplayTV units.
* DVArchive is a Java application written by Gerry Duprey 
(https://web.archive.org/web/20171203221550/http://www.dvarchive.org/) 
that emulates a ReplayTV and allows the download of files from a ReplayTV unit.
* ReplayTV provides three files for each video:
  * An MP4 file containing the video stream
  * An NDX file containing time reference information
  * An XML file containing episode information (series name, episode name, channel, time recorded, 
  etc.)




## Dependancies
* The application must be run on a Windows platform to support DOS utility calls.
* The following DOS utilities must be installed in the following directories (they are current hard-coded into constants in PrepFiles.java):
  * C:\Programs\Timefix\timefix.exe
  * C:\Programs\Gsplit\gsplit.exe
* Two directories are hard-coded into constants in PrepFiles.java:
  * C:\Data\Videos (raw videos and supporting files)
  * C:\Data\Videos\Timefixed (output directory for Timefix utility)
* The application uses the xstream library to read and write XML files.