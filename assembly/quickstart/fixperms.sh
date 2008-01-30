#! /bin/bash
find . -name '*.sh' -exec chmod 755 {} \;
chmod 755 ./@ant.name@/bin/*
chmod 755 ./@maven.name@/bin/*
chmod 755 ./@tomcat.name@/bin/*
