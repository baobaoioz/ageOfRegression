# Report

## Roles
Protocol designer: Yanning Zang, Zhuoyi Nie
Programmer: Yijie Lin, Yanning Zang, Zhuoyi Nie, Jizi Shangguan

## Summary

This system, according to the protocol our classmate made, can add, remove and lookup IP address and port of certain conversion type. The discovery server is used to keep the record of IP address and port of conversion server.

## Protocol specification

ConvServer:

ConvServers send "add unit1 unit2 IP_Address Port" to Discovery Server to register itself. ConvServers also send "remove IP_Address Port" to Discover Server to delete its record when ConvServers shut down.

ProxyServer:

ProxyServer receives client's telnet command and send "lookup unit1 unit2" to Discovery Server to get corresponding IPAddress and port. If the conversion type is supported, ProxyServer connects to the ConvServer and do type converse. Then it will return the result to the client.

DiscoveryServer:

DiscoveryServer receives add, remove and lookup command.
If add command is received, it first check if there exists a record with the same IP address. If existed, then return failure. Otherwise, put the type and IP address into record list.
If remove command is received, it will iterate the record list to delete corresponding record.
If lookup command is received, it will iterate the record list to return the first record that meets the requirement.


## Test plan
1. use "javac filename" to complie these files: DiscoveryServer.java ConvServer.java,ConvServer_1.java,ConvServer_2.java, ConvServer_3.java, ProxyServer.java
2. Use "java filename" to run complied files.
3. DiscoveryServer must run on port 23456. Other server can run on arbitrary port(not 23456, of course).


