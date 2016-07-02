#GeoTrellis - Geotiff not processed correctly when ingested using FileWriter

This project is a modified version of the Geotrellis-Landsat-Tutorial used to demonstrate a bug with processing
aa particular Geotiff file that was read correctly by gdal and QGIS.

The IngestImage and ServeNDVI files where modified to deal with Single Tile .vs. Multiband Tile.
The ServeNDVI was modified to do a simple aspect operation on the ingested tile.

To receate clone this project and download the file cf8b850a-c3fb-4596-ac46-f553c216078f-1.tif using the 
link https://drive.google.com/file/d/0B1YZbN2gCsmSYV9YbkJnaWNrYTA/view Copy the file to the data directory in the project
 
Then sbt run:

Select option 2 to Ingest the Image.
 
sbt run again:

Select option 4 to run the server.

Server runs on port 8090.  Run index.html to see random blocks being displayed for ingested Geotiff.
