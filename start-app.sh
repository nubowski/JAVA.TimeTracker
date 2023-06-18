#!/bin/bash
java -jar /app.jar | awk '{print $0,"\n"}'