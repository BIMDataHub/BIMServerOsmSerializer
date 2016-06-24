# OSMSerializer

## Introduction

OSMSerializer is a BIMserver plugin that translates Industry Foundation Classes (IFC) building model into OpenStudio file (.osm) format, which can be used to perform energy and lighting analysis in OpenStudio. 

## Features

OSMSerializer currently extracts the following information from IFC file:

* Geometry information of the following IFC entities:
	
	* IfcWall
	* IfcSlab
	* IfcRoof
	* IfcWindow
	* IfcDoor

* Material information of the following IFC entities:

	* IfcWall 
	* IfcSlab
	* IfcRoof
	* IfcWindow
	* IfcDoor

* Lighting fixtures and lighting control zones infromation  

## Installation

OSMserializer is a plugin specifically developed for **BIMserver 1.3.4**. 
To use this plugin, please: 

1. Download [BIMserver version 1.3.4](https://github.com/opensourceBIM/BIMserver/releases/download/1.3.4-FINAL-2014-10-17/bimserver-1.3.4-FINAL-2014-10-17.jar) and rename the .jar file to bimserver. 
2. When you first run the server, it will create two folders, which are home and bimserver. 
3. Download our serializer (.jar file) from our [github release page](https://github.com/BIMDataHub/OsmSerializer/releases) and put it under bimserver/plugins.
4. If the server is running when you import the serializer, please restart the server before you perform any tasks. 

## Usage

There are two ways to use our serializer to generate OSM file:

1. You can use our [OpenStudio plugin]() which provides a graphical user interface to interact with BIMServer directly within OpenStudio. This plugin offers a set of useful functionalites, such as create project, upload IFC file and download IFC file in OSM format. 
2. You can also use the API page in Admin GUI page provided by BIMserver. 

## FAQ
### Why the geometry information of my IFC file is missing in the output OSM file?

### Why the output OSM file cannot be opened in OpenStudio?



## Contact
If you have any questions, please contact Dr. Issa Ramaji at <ramaji.issa@gmail.com>. 

## Contributors

## License

