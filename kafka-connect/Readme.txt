connect-standalone.bat ..\..\config\connect-standalone.properties connector1.properties [connector2.properties ...]

connect-standalone.bat ..\..\config\connect-standalone.properties ..\..\config\connect-file-source.properties  ..\..\config\connect-file-sink.properties


adding the data to test.txt
===========================
echo first > test.txt
echo second >> test.txt


Kafka Connect : test.sink.txt is not getting updated
====================================================

The Kafka file source connect keeps the last read location from file. When you deleted the file without resetting the offsets to 0,
connector waits for new data to show starting at a specific character count from the beginning.

If you are using stand alone mode deleting /tmp/connect.offsets will solve the issue.
